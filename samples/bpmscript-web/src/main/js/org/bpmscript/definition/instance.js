require("/org/bpmscript/bpmscript.js");
require("/org/bpmscript/library/web.js");

function Instance(configuration) {
  this.controller = delegate(this, new Controller());
}
Instance.prototype = {
  "index": function(command) {
    var query = new Paging().query(command ? command.instances : null);
    var instances = this.instanceManager.getInstances(query);
    return {instances: {results: instances, query: query}};
  },
  "instance": function(command) {
    var id = command.id;
    var instance = this.instanceManager.getInstance(id);
    var entries = this.continuationJournal.getEntriesForPid(id);
    return {value: instance, entries: entries};
  }
}
