require("/org/bpmscript/bpmscript.js");
require("/org/bpmscript/spring.js");
require("/org/bpmscript/correlation.js");

function Test() {}
Test.prototype = {

  /** call test **/
  test: function(data) {
    var correlator = new CorrelationEngine(this.channel, "correlator");
    var queue = correlator.register("achannel", [
      ["message.length", 12], 
      ["message", "Hello World!"]
    ]);
    var result = queue.receive();
    this.channel.reply({content: result});
    queue.close();
  }

}
