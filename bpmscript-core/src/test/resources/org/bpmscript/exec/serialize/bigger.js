require("/org/bpmscript/bpmscript.js");
require("/org/bpmscript/integration/internal/conversation.js");

function serialize(input) {
  var toserialize = input;
  pause();
}

function println(value) {
  Packages.java.lang.System.out.println(value);
}

function three() {
  pause();
}

function two() {
  three();
}

function one() {
  var channel = new Channel();
  for(var counter = 1; counter < 1000; counter++) {
    channel.createQueue();
    // println("counter " + counter);
    pause();
  }
}
