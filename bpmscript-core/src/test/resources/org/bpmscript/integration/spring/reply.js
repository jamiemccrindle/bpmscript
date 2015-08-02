require("/org/bpmscript/bpmscript.js");

function Test() {}
Test.prototype = {

  /** call test **/
  test: function(total) {
  
    for(var i = 0; i < total; i++) {
      // send a message to the sub process
      var future = this.channel.send({
        address: "channel-bpmscript-first",
        attributes: { 
          parentVersion: this.channel.version, 
          definitionName: "test", 
          operation: "sub"
        }, 
        payload: ["amessage"]});
      // get the result from the future
      var exchange = future.get(time.minutes(1));
      log.debug(this.channel.getContent(exchange));
    }
    this.channel.reply({payload: "sub"});
  },

  /** sub function **/
  sub: function(message) {
    log.debug("here " + message);
    this.channel.reply({payload: "blahblah"});
  }

}
