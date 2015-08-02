function MapAdapter(map) {
  this.map = map;
}

MapAdapter.prototype = {
  __noSuchMethod__: function(methodName, args) {
    return this.transform(this.map.get(methodName));
  },
  "transform": function(value) {
    if(value == null) {
      return null;
    } else if(value instanceof java.util.Map) {
      return new MapAdapter(value);
    } else if (value instanceof java.util.List) {
      var arrayvalue = new Array();
      for(var i = 0; i < value.size(); i++) {
        var item = value.get(i);
        arrayvalue[i] = this.transform(item);
      }
      return arrayvalue;
    } else {
      return value;
    }
  }
}

log.info(array[0]);

var map = converter.convert(params);
log.info(map);
var addresses = map.get("customer").get("addresses");
log.info(addresses instanceof java.util.List);
var adapter = new MapAdapter(map);
log.info(adapter.customer().addresses()[0].houseNumber());
