require("/org/bpmscript/bpmscript.js");

function NoSuch(channel) {
  this.channel = channel;
}
NoSuch.prototype = {
  constructor: NoSuch,
  __noSuchMethod__: function(methodName, args) {
    log.info("before sleep for " + methodName);
    this.channel.sleep(time.seconds(1));
    log.info("after sleep for " + methodName);
  }
}

function Test() {}
Test.prototype = {

  /** call test **/
  test: function(total) {
    var nosuch = new NoSuch(this.channel);
    nosuch.test();
    log.info("before nosuch");
    nosuch.test();
    log.info("after nosuch");
    this.channel.reply({content: "sub"});
  }

}
