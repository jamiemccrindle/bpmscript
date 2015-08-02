function delegate(source, target) {
  for(var name in target) {
    var item = source[name];
    if(!item) {
      source[name] = target[name];
    }
  }
  return target;
}

function lookup(name) {
  var subnames = name.split("\.");
  return recLookupValue(subnames, 0, topLevelScope());
}

function recLookupValue(subnames, index, current) {
  var subname = subnames[index];
  if(index == subnames.length - 1) {
    // last value
    if(subname.match(/\[\]$/) != null) {
      return current[subname.substring(0, subname.length - 2)];
    } else {
      return current[subname];
    }
  } else {
    // intermediate value
    var matches = /(.*?)\[(\d+)\]/.exec(subname);
    if(matches != null) {
      var [arraySubname, index] = matches;
      var array = current[arraySubname];
      if(array != null) {
        var arrayValue = array[index];
        return recLookupValue(subnames, index + 1, arrayValue);
      } else {
        return null;
      }
    } else {
      var object = current[subname];
      if(object != null) {
        return recLookupValue(subnames, index + 1, object);
      } else {
        return null;
      }
    }
  }
}

function link(definition, operation, params) {
  var url = base + "/bpmscript/" + definition + "/" + operation + ".html";
  if(params) {
    url += "?";
    for (var key in params) {
      url += key + "=" + params[key] + "&";
    }
  }
  return url;
}

function getMessage(code, message, args) {
    return args != null 
     ? requestContext.getMessage(message != null 
       ? message : code, args, message != null ? message : "")
     : requestContext.getMessage(message != null 
       ? message : code, message != null ? message : "");
}

/**
 * e.g. "id=xxx&sort=yyy&m=ddd", {sort:"asc"}
 */
function QueryString() {}
QueryString.prototype = {
  mergeQueryString: function(request, values) {
    var result = values; // should probably copy here
    var params = request.getParameterMap();
    var iterator = params.entrySet().iterator();
    while(iterator.hasNext()) {
      var entry = iterator.next();
      if(!result[entry.getKey()]) {
        result[entry.getKey()] = entry.getValue();
      }
    }
    var queryString = "?";
    for (var key in result) {
      queryString += key + "=";
      for (var count = 0; count < result[key].length; count++) {
        queryString += result[key][count];
        if(count < result[key].length - 1) { queryString += ","; }
      }
      queryString += "&";
    }
    return queryString.substring(0, queryString.length - 1);
  }
}

function Paging() {
  this.queryString = delegate(this, new QueryString());
}
Paging.prototype = {
  makeQueryObject: function(name, key, value) {
    var result = {};
    result[name + "." + key] = [value];
    return result;
  },
  pagedUrls: function(name) {
    var results = lookup(name).results;
    var query = lookup(name).query;
    var result = {};
    var maxResults = query.maxResults || 10;
    result.first = this.mergeQueryString(request, this.makeQueryObject(name, "first", 0));
    result.previous = this.mergeQueryString(request, this.makeQueryObject(name, "first", (query.firstResult - maxResults)));
    result.next = this.mergeQueryString(request, this.makeQueryObject(name, "first", (query.firstResult + maxResults)));
    if(results.totalResults > maxResults) {
      result.last = this.mergeQueryString(request, this.makeQueryObject(name, "first", (results.totalResults - maxResults)));
    }
    return result;
  }
}

function PropertiesTable() { }
PropertiesTable.prototype = {
  propertiesTableRow: function(value, header) {
    var result = new XMLList();
    result += <th>{header.description}</th>
    if(header.renderer != null) {
      result += <td>{header.renderer.render(header, value)}</td>;
    } else {
      result += <td>{value[header.name]}</td>
    }
    return result;
  },
  propertiesTableContent: function(value, headers) {
    var result = new XMLList();
      for each (var header in headers) {
        if(!(header instanceof Function)) {
          result += <tr>{this.propertiesTableRow(value, header)}</tr>;
        }
      }
    return result;
  },
  propertiesTable: function(value, headers) {
    return <table>
      {this.propertiesTableContent(value, headers)}
    </table>;
  }
}

function AbstractTable() { }
AbstractTable.prototype = {
  abstractTableRow: function(item, headers, i) {
    var result = new XMLList();
      for each (var header in headers) {
        if(!(header instanceof Function)) {
          if(header.renderer != null) {
            result += <td class={i % 2 == 0 ? "tablerow_even" : "tablerow_odd"}>{header.renderer.render(header, item, i)}</td>;
          } else {
            result += <td class={i % 2 == 0 ? "tablerow_even" : "tablerow_odd"}>{item[header.name]}</td>
          }
        }
      }
    return result;
  },
  abstractTableContent: function(name, headers) {
    var results = lookup(name).results;
    var result = new XMLList();
    for(var i = 0; i < results.results.size(); i++) {
      var item = results.results.get(i);
      result += <tr>{this.abstractTableRow(item, headers, i)}</tr>;
    }
    return result;
  }
}

function ListTable() {
  this.abstractTable = delegate(this, new AbstractTable()); 
}
ListTable.prototype = {
  listTableHeaders: function(headers) {
    var result = new XMLList();
    for each (var header in headers) {
      if(!(header instanceof Function)) {
        result += <th>{header.description}</th>;
      }
    }
    return result;
  },
  listTable: function(list, headers) {
    return <table class="listtable">
      <thead>
        <tr>
          {this.listTableHeaders(headers)}
        </tr>
      </thead>
      <tbody>
        {this.listTableContent(list, headers)}
      </tbody>
    </table>;
  },
  listTableContent: function(list, headers) {
    var result = new XMLList();
    for(var i = 0; i < list.length; i++) {
      var item = list[i];
      result += <tr>{this.abstractTableRow(item, headers, i)}</tr>;
    }
    return result;
  }
}

function PagedTable() {
  this.abstractTable = delegate(this, new AbstractTable()); 
  this.paging = delegate(this, new Paging());
}
PagedTable.prototype = {
  pagedTableHeaders: function(name, headers) {
    var query = lookup(name).query;
    var result = new XMLList();
    for each (var header in headers) {
      if(!(header instanceof Function)) {
        var orderBy = {};
        if(query.orderBys && query.orderBys.size() > 0) {
          orderBy = query.orderBys.get(0);
        }
        var direction = orderBy.field == header.name && orderBy.asc ? "desc" : "asc";
        var params = {};
        params[name + ".sort"] = [header.name];
        params[name + ".direction"] = [direction];
        params[name + ".first"] = [0];
        var sort = this.mergeQueryString(request, params);
        result += <th><a href={sort}>{header.description}</a></th>;
      }
    }
    return result;
  },
  pagedTableNavigation: function(name) {
    var results = lookup(name).results;
    var query = lookup(name).query;
    var result = new XMLList();
    var urls = this.pagedUrls(name);
    result += query.firstResult != 0 
      ? <a href={urls.first}><img src={base + "/images/icons/resultset_first.png"}/></a> 
      : <span><img src={base + "/images/icons/resultset_first_disabled.png"}/></span>;
    result += query.firstResult != 0 
      ? <a href={urls.previous}><img src={base + "/images/icons/resultset_previous.png"}/></a> 
      : <span><img src={base + "/images/icons/resultset_previous_disabled.png"}/></span>;
    result += results.more 
      ? <a href={urls.next}><img src={base + "/images/icons/resultset_next.png"}/></a> 
      : <span><img src={base + "/images/icons/resultset_next_disabled.png"}/></span>;
    result += results.totalResults > query.maxResults 
      ? <a href={urls.last}><img src={base + "/images/icons/resultset_last.png"}/></a> 
      : <span><img src={base + "/images/icons/resultset_last_disabled.png"}/></span>;
    return result;
  },
  pagedTable: function(name, headers) {
    return <table class="pagedtable">
    <thead>
    <tr>
    {this.pagedTableHeaders(name, headers)}
    </tr>
    </thead>
    <tbody>
    {this.pagedTableContent(name, headers)}
    </tbody>
    <tfoot>
    <tr>
      <td colspan={headers.length}>
        {this.pagedTableNavigation(name)}
      </td>
    </tr>
    </tfoot>
    </table>;
  },
  pagedTableContent: function(name, headers) {
    var results = lookup(name).results;
    var result = new XMLList();
    if(results.results.size() == 0) {
      result += <tr><td colspan={headers.length}></td></tr>
    } else {
      for(var i = 0; i < results.results.size(); i++) {
        var item = results.results.get(i);
        result += <tr>{this.abstractTableRow(item, headers, i)}</tr>;
      }
    }
    return result;
  }
}

function Form() {}
Form.prototype = {
  formOptions: function(selected,map) {
    var result = new XMLList();
    for(var item in map) {
      if(item == selected) {
        result += <option selected="true" value={item}>{map[item]}</option>
      } else {
        result += <option value={item}>{map[item]}</option>
      }
    }
    return result;
  },
  addAttributes: function(xml, attributes) {
    for(item in attributes) {
      var item = iterator.next();
      xml["@" + item] = attributes[item];
    }
    return xml;
  },
  formSelect: function(name, map, attributes) {
    var xml = <select name={name}>{this.formOptions(lookup(name), map)}</select>
    return attributes != null ? this.addAttributes(xml, attributes) : xml;
  },
  formText: function(name, attributes) {
    var xml = <input type="text" name={name} value={lookup(name)}/>
    return attributes != null ? this.addAttributes(xml, attributes) : xml;
  },
  formErrors: function(name) {
    return typeof(errors) != "undefined" && errors && errors[name] ? errors[name] : [];
  },
  formGlobalErrors: function(name) {
    if(typeof(errors) != "undefined") {
      var result = errors.__global;
      return result == null ? [] : result;
    } else { return []; }
  }
}
function BpmScriptTheme() {
  this.pagedTableDelegate = delegate(this, new PagedTable());
  this.listTableDelegate = delegate(this, new ListTable());
  this.propertiesTableDelegate = delegate(this, new PropertiesTable());
  this.form = delegate(this, new Form());
}
BpmScriptTheme.prototype = {
  box: function(title, content, style) {
    return <table class={"boxtable " + style}>
        <tr class={"boxheader " + style}>
          <td>{title}</td>
        </tr>
        <tr class={"boxcontent " + style}>
          <td>
            {content}
          </td>
        </tr>
      </table>;
  },
  source: function(source) {
    return <pre name="code" class="javascript">{source}</pre>
  },
  errors: function(errors) {
    var result = new Array();
    for each (var error in errors) {
      result.push(getMessage(error.code, error.message, error.args));
    }
    return errors.length > 0 ? <span style="color:red">{result.join(",")}</span> : "";
  }
}
