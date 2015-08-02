var __errorthrower__ = new Packages.org.bpmscript.exec.ContinuationErrorThrower();

function pause() {
	__errorthrower__.throwContinuation(new Continuation());
}

function MessageQueue(id, channel, handler) {

	this.id = id;
	this.channel = channel;
	this.responses = new Packages.java.util.LinkedList();
	this.handler = handler;

}

MessageQueue.prototype = {
	"receive": function(timeout) {
		channel.waitFor(this, timeout);
		return this.responses.poll();
	},
	
	"hasMessage": function() {
		return this.responses.size() > 0;
	},
	
	"isDone": function() {
		return this.responses.size() > 0;
	},
	
	"close": function() {
		this.channel.close(this.id);
	},

	"addResponse": function(response) {
		this.responses.add(response);
	},

	"get": function(timeout) {
		if(this.hasMessage()) {
			return this.responses.get(0);
		} else {
			this.channel.waitFor(this, timeout);
			if(this.responses.size() == 0) {
				return null;
			}
			this.close();
			return this.responses.get(0);
		}
	}

}

function Channel(pid, input, parentPid) {

  this.queues = new Object();
  this.input = input;
  this.pid = pid;
  this.parentPid = parentPid;

}

Channel.prototype = {

  "close": function(id) {
  	delete this.queues[id];
  },
  
  "createQueue": function() {
  	var queueId = Packages.java.util.UUID.randomUUID().toString();
    var messageQueue = new MessageQueue(queueId, this);
    this.queues[messageQueue.id] = messageQueue;
    return messageQueue;
  },
  
  "send": function(message) {
  	var queueId = Packages.java.util.UUID.randomUUID().toString();
    var messageQueue = new MessageQueue(queueId, this);
    this.queues[messageQueue.id] = messageQueue;
  	scriptChannel.send(this.pid, queueId, message);
    return messageQueue;
  },

  "sendSync": function(message, timeout) {
  	var queue = this.send(message);
  	if(timeout) {
  		return queue.receive(timeout);
  	} else {
  		return queue.receive();
  	}
  },

  "sendTimeout": function(timeout) {
  	var queueId = Packages.java.util.UUID.randomUUID().toString();
  	scriptChannel.sendTimeout(this.pid, queueId, message, timeout);
    var messageQueue = new MessageQueue(queueId, this);
    this.queues[messageQueue.id] = messageQueue;
    return messageQueue;
  },

  "reply": function(message) {
  	scriptChannel.reply(this.pid, this.input, message);
  },

  "waitFor": function(queue, timeout) {
  	if(timeout) {
  		var timeoutQueue = this.sendTimeout(timeout);
  		this.waitForOne(queue, timeoutQueue);
  	} else {
  		this.waitForOne(queue);
  	}
  },

  /* Wait for one of the queues listed in the arguments
   */
  "waitForOne": function() {
  	var argQueues = new Object();
  	for(var i = 0; i < arguments.length; i++) {
  		var argument = arguments[i];
  		if(argument.hasMessage()) {
  			return argument;
  		}
  		argQueues[argument.id] = argument;
  	}
  	var result = this.getNext();
    while(!argQueues[result.id]) {
		  result = this.getNext();
		}
		return result;
  },

  /* Wait for all of the queues listed in the arguments
   */
  "waitForAll": function() {
  	for(var argument in arguments) {
  		argument.get();
  	}
  },

  /* Get the next queue to come back (ignores queues that
   * have already returned)
   */
  "getNext": function() {
      var aresult = pause();
      // inbound message to this queue
      var queueId = aresult.getQueueId();
   	  var queue = this.queues[queueId];
   	  var message = aresult.getMessage();
   	  if(queue.handler != null) {
   	  	if(queue.handler.process(message)) {
   	  		queue.addResponse(message);
   	  	}
   	  } else {
   	  	queue.addResponse(message);
   	  }
   	  return queue;
  }
}

function process(func, pid, input, parentPid) {
  var channel = new Channel(pid, input, parentPid);
 	return func(input, channel);
}
