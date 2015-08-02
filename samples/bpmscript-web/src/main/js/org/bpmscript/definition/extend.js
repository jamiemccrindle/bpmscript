require("/org/bpmscript/bpmscript.js");

function Extend(input, channel, request) {

}
Extend.prototype = {
  index: function() {
    return {message: "Hello World!"};
  }
}
