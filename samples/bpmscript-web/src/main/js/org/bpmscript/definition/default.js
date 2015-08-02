require("/org/bpmscript/bpmscript.js");
require("/org/bpmscript/library/web.js");

function Default(configuration) {
  this.controller = delegate(this, new Controller());
}
Default.prototype = {
  index: function() {
    var definitions = this.versionedDefinitionManager.getPrimaryDefinitions();
    return {list: definitions};
  }
}
