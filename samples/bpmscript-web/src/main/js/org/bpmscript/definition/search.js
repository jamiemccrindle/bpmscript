require("/org/bpmscript/bpmscript.js");
require("/org/bpmscript/library/web.js");

function Search(channel) {
  this.controller = delegate(this, new Controller());
}
Search.prototype = {
  "index": function(command) {
    return {};
  }
}
