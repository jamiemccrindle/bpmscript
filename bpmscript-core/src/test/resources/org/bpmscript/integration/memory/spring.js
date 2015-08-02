require("/org/bpmscript/bpmscript.js");
require("/org/bpmscript/integration/internal/spring.js");

function Test() {}
Test.prototype = {

  /** call test **/
  test: function(data, loopcount) {
    var recorder = new SpringService(this.channel, "recorder", 1000);
    for(var i = 0; i < loopcount; i++) {
      var result = recorder.send("record", [data]);
    }
    this.channel.reply({content: result});
  }

}
