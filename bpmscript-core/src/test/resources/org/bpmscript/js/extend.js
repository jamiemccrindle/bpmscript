Object.prototype.delegate = function(delegate) {
  for(var name in delegate) {
    var item = this[name];
    if(!item) {
      this[name] = delegate[name];
    }
  }
  return delegate;
}

function Processor() {
  this.type = "Process ";
}
Processor.prototype = {
  process: function(func) {
    this[func].apply(this, ["World!"]);
  },
}

function Controller() {
  this.processor = this.delegate(new Processor());
  this.type = "Controller ";  
}
Controller.prototype = {
  index: function(arg) {
    log.info(this.type + " " + arg);
  } 
}

var controller = new Controller();

controller.process("index");

function Helper() {
}
Helper.prototype = {
  blah: function() {
    log.info("in blah " + this.type + " ");
  }
}


function Crud() {
 this.type = "Crud";
 this.controller = this.delegate(new Controller());
 this.helper = this.delegate(new Helper());
}
Crud.prototype = {
  index: function() {
    log.info("in index " + this.type + " ");
  }
}

var xxx = {crud: Crud};

var crud = new xxx["crud"]();

crud.process("index");
crud.process("blah");

