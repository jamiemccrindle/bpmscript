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
  autoStart: function(total) {
    this.channel.addKillHook(new KillHook());
    this.channel.reply({content: "STARTED"});
    while(true) {
      log.info("Sleeping Two");
      this.channel.sleep(time.seconds(10));
    }
  }

}
