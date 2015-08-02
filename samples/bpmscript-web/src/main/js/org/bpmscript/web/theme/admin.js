function HeaderRenderer(definition, operation, paramName, idName, textName) {
  this.definition = definition;
  this.operation = operation;
  this.paramName = paramName;
  this.idName = idName;
  this.textName = textName || paramName;
}
HeaderRenderer.prototype = {
  render: function(header, item, i) {
    params = {};
    params[this.paramName] = item[this.idName];
    return <a href={link(this.definition, this.operation, params)}>{item[this.textName]}</a>;
  }
}

function BpmScriptAdminTheme(theme) {
  this.theme = delegate(this, theme);
}
BpmScriptAdminTheme.prototype = {
  definition: function(value) {
    return this.propertiesTable(value, [
      {name:"id", description:"Id"},
      {name:"name", description:"Name"},
      {name:"lastModified", description:"Last Modified"},
    ]);
  },
  versionsTable: function(list) {
    return this.listTable(list, [
      {name:"id", description:"Id", renderer:new HeaderRenderer("definition", "definition", "id", "id", "id")},
      {name:"lastModified", description:"Last Modified"},
    ]);
  },
  definitionsTable: function(list) {
    return this.listTable(list, [
      {name:"name", description:"Name", renderer:new HeaderRenderer("definition", "definition", "id", "id", "name")},
      {name:"lastModified", description:"Last Modified"},
    ]);
  },
  continuationEntriesTable: function(list) {
    return this.listTable(list, [
      {name:"branch", description:"Branch"},
      {name:"version", description:"Version"},
      {name:"lastModified", description:"Last Modified"},
      {name:"processState", description:"Process State"},
    ]);
  },
  instanceTable: function(name) { 
    return this.pagedTable(name, [
      {name:"id", description:"Id", renderer:new HeaderRenderer("instance", "instance", "id", "id", "id")},
      {name:"definitionType", description:"Type"},
      {name:"operation", description:"Operation"},
      {name:"lastModified", description:"Last Modified"},
    ]);
  },
  instance: function(value) {
    return this.propertiesTable(value, [
      {name:"id", description:"Id"},
      {name:"definitionId", description:"Definition", renderer:new HeaderRenderer("definition", "definition", "id", "definitionId", "definitionName")},
      {name:"definitionType", description:"Type"},
      {name:"operation", description:"Operation"},
      {name:"lastModified", description:"Last Modified"},
    ]);
  }
}
