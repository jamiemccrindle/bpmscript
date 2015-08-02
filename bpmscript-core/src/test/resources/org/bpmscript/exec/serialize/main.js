var __errorthrower__ = new Packages.org.bpmscript.exec.ContinuationErrorThrower();

function pause() {
	__errorthrower__.throwContinuation(new Continuation());
}

function serialize() {
  pause();
}

function println(value) {
  Packages.java.lang.System.out.println(value);
}

function one() {
  var i = 1;
  println(i + " one");
	pause();
  i = i + 1
  println(i + " two");
	pause();
  i = i + 1
  println(i + " three");
	pause();
}
