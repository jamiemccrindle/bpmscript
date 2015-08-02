require("/org/bpmscript/bpmscript.js");

function Test() {}
Test.prototype = {

  /** call test **/
  test: function(total) {
    for(var i = 0; i < total; i++) {
      // send a message to the sub process
      var future = this.channel.send({"address": "bpmscript-first", 
        methodName: "sendFirst", 
        args: [this.channel.parentVersion, "test", "sub", ["amessage"]]});
      // get the result from the future
      var exchange = future.get(1000);
    }
    this.channel.reply({content: "sub"});
  },

  /** sub function **/
  sub: function(message) {
    this.channel.reply({content: "blahblah"});
  }

}
