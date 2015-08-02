require("/org/bpmscript/bpmscript.js");

require("/org/bpmscript/bpmscript.js");

function Test() {}
Test.prototype = {

  /** call test **/
  test: function() {
    // send a message to the echo component
    var future = this.channel.send({"test": "test"});
    
    // get the result from the future
    var exchange = future.get();

    // echo the reply to the calling component
    this.channel.reply({"test":"test"});
  }

}
