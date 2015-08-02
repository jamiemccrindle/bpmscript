require("/org/bpmscript/bpmscript.js");

function Test() {}
Test.prototype = {

  /** call test **/
  doSomething: function(one, two) {
    this.channel.reply({content: "Hello World! " + one + " " + two});
  },

}
