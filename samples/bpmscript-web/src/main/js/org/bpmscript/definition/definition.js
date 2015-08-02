require("/org/bpmscript/bpmscript.js");
require("/org/bpmscript/library/web.js");

function Definition() {
  this.controller = delegate(this, new Controller());
  this.versionedDefinitionManager;
  this.instanceManager;
}
Definition.prototype = {
  index: function() {
    var definitions = this.versionedDefinitionManager.getPrimaryDefinitions();
    return {list: definitions};
  },
  definition: function(command) {
    var definition = this.versionedDefinitionManager.getDefinition(command.id);
    var versions = this.versionedDefinitionManager.getDefinitionsByName(definition.name);
    var query = new Paging().query(command.instances);
    var instances = this.instanceManager.getInstancesForDefinition(query, command.id);
    return {value: definition, versions: versions, instances: {results: instances, query: query}};
  }
}
