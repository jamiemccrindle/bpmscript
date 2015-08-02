require("/org/bpmscript/bpmscript.js");

function Test() {}
Test.prototype = {

  /** call test **/
  test: function(total) {
  
    log.info("total is " + total);
  
    for(var i = 0; i < total; i++) {
      // send a message to the sub process
      log.info("sending message");
      var future = this.channel.send({"address": "bpmscript-first", 
        methodName: "sendFirst", 
        args: [this.channel.parentVersion, "test", "sub", ["amessage"]]});
      // get the result from the future
      log.info("before getting exchange");
      var exchange = future.get();
      log.info("after getting exchange");
    }
    this.channel.reply({content: "sub"});
  },

  /** sub function **/
  sub: function() {
    log.info("replying");
    this.channel.reply({content: "blahblah"});
    log.info("completing sub");
  }

}
