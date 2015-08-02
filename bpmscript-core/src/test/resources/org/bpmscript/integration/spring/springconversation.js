require("/org/bpmscript/bpmscript.js");
require("/org/bpmscript/integration/spring/conversation.js");

function Test() {}
Test.prototype = {
  /** call test **/
  test: function(total) {
    var conversation = new SpringConversation(this.message, this.channel, "channel-conversation", time.minutes(30));
    var request = null;
    request = conversation.respond({step: "one"});
    request = conversation.respond({step: "two"});
    request = conversation.end({step: "three"});
  }
}
