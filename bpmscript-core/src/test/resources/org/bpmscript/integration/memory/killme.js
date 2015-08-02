require("/org/bpmscript/bpmscript.js");

function KillHook() {
}
KillHook.prototype = {
  process: function(message) {
    log.info("kill hook called " + message);
  }
}

function Test() {}
Test.prototype = {

  /** call test **/
  test: function(data) {
    this.channel.addKillHook(new KillHook());
    this.channel.reply({content: "STARTED"});
    while(true) {
      log.info("Sleeping");
      this.channel.sleep(time.seconds(10));
    }
  }

}
