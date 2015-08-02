var __errorthrower__ = new Packages.org.bpmscript.exec.ContinuationErrorThrower();

function pause() {
	__errorthrower__.throwContinuation(new Continuation());
}

function MessageQueue(id, channel, handler) {

	this.id = id;
	this.channel = channel;
	this.responses = new Packages.java.util.LinkedList();
	this.handler = handler;

	this.receive = function(timeout) {
		channel.waitFor(this, timeout);
		return this.responses.poll();
	}
	
	this.hasMessage = function() {
		return this.responses.size() > 0;
	}
	
	this.isDone = function() {
		return this.responses.size() > 0;
	}
	
	this.close = function() {
		channel.close(this.id);
	}

	this.addResponse = function(response) {
		this.responses.add(response);
	}

	this.get = function(timeout) {
		if(this.hasMessage()) {
			this.close();
			return this.responses.get(0);
		} else {
			channel.waitFor(this, timeout);
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
  
  this.close = function(id) {
  	delete this.queues[id];
  }
  
  this.createQueue = function() {
  	var queueId = Packages.java.util.UUID.randomUUID().toString();
    var messageQueue = new MessageQueue(queueId, this);
    this.queues[messageQueue.id] = messageQueue;
    return messageQueue;
  }
  
  this.send = function(message) {
  	var queueId = Packages.java.util.UUID.randomUUID().toString();
    var messageQueue = new MessageQueue(queueId, this);
    this.queues[messageQueue.id] = messageQueue;
  	scriptChannel.send(this.pid, queueId, message);
    return messageQueue;
  };

  this.sendSync = function(message, timeout) {
  	var queue = this.send(message);
  	if(timeout) {
  		return queue.receive(timeout);
  	} else {
  		return queue.receive();
  	}
  }

  this.sendTimeout = function(timeout) {
  	var queueId = Packages.java.util.UUID.randomUUID().toString();
  	scriptChannel.sendTimeout(this.pid, queueId, message, timeout);
    var messageQueue = new MessageQueue(queueId, this);
    this.queues[messageQueue.id] = messageQueue;
    return messageQueue;
  };

  this.reply = function(message) {
  	scriptChannel.reply(this.pid, input, message);
  }

  this.waitFor = function(queue, timeout) {
  	if(timeout) {
  		var timeoutQueue = this.sendTimeout(timeout);
  		this.waitForOne(queue, timeoutQueue);
  	} else {
  		this.waitForOne(queue);
  	}
  }

  /* Wait for one of the queues listed in the arguments
   */
  this.waitForOne = function() {
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
  };

  /* Wait for all of the queues listed in the arguments
   */
  this.waitForAll = function() {
  	for(var argument in arguments) {
  		argument.get();
  	}
  };

  /* Get the next queue to come back (ignores queues that
   * have already returned)
   */
  this.getNext = function() {
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
  };

}

function process(func, pid, input, parentPid) {
  var channel = new Channel(pid, input, parentPid);
 	return func(input, channel);
}
