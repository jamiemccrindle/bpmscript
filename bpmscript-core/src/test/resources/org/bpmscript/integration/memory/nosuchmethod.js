require("/org/bpmscript/bpmscript.js");

function NoSuch(channel) {
  this.channel = channel;
  this.__noSuchMethod__ = function(methodName, args) {
    log.info("before sleep for " + methodName);
    this.channel.sleep(time.seconds(1));
    log.info("after sleep for " + methodName);
  };
}
NoSuch.prototype = {
  constructor: NoSuch
}

function one(name) {
  log.info("here " + name);
}

function two(name) {
  log.info("here " + name);
  one("one");
}

function Test() {}
Test.prototype = {

  /** call test **/
  runit: function(total) {
    two("two");
    var nosuch = new NoSuch(this.channel);
    nosuch.nosuch1("blah", 1, "mangle");
    log.info("before nosuch");
    nosuch.nosuch2();
    log.info("after nosuch");
    this.channel.reply({content: "sub"});
  }

}
