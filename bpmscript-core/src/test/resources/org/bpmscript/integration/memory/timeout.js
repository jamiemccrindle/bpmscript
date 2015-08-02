require("/org/bpmscript/bpmscript.js");

function Test() {}
Test.prototype = {

  /** call test **/
  test: function(total) {
    log.info("starting timeout");
  
    var future = this.channel.send({"address": "spring", 
      methodName: "pause", 
      args: ["pausingService"]});
      
    // get the result from the future
    var exchange = future.get(10);
    
    log.info("should be completed and exchange should be null : " + exchange);
  }

}
