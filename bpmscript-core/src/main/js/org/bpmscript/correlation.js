function CorrelationEngine(channel, correlationService, correlationAddress, defaultTimeout) {
  this.correlationId = java.util.UUID.randomUUID().toString();
	this.channel = channel;
  this.correlationService = correlationService;
	this.correlationAddress = correlationAddress || "correlation";
  this.defaultTimeout = defaultTimeout || 0;
}

function CorrelationKillHook(queue) {
  this.queue = queue;
}
CorrelationKillHook.prototype = {
  process: function(message) {
    this.queue.close();
  }
}

function correlationQueueCloser() {
  this.correlationService.removeCorrelation(this.correlationId);
}

CorrelationEngine.prototype = {
  "send": function(channelName, message) {
    return this.correlationService.send(channelName, message);
  },
  "call": function(channelName, message, timeout) {
    // set up a callback
    var [callbackFuture, callbackMessage] = this.channel.callback({
      args:[message]
    });
    // call the correlation service
    var count = this.correlationService.send(channelName, callbackMessage);
    // check whether any orders were returned
    if(count == 0) {
      return null;
    } else {
      return this.channel.getContent(callbackFuture.get(timeout || this.defaultTimeout));
    }
  },
  "register": function(channelName, criteria, timeout) {
    var actualTimeout = timeout || this.defaultTimeout;
    var correlation = new org.bpmscript.correlation.memory.Correlation();
    for each (var criterion in criteria) {
      correlation.addCriteria(criterion[0], criterion[1]);
    }
    var queue = this.channel.send({address: this.correlationAddress, 
      "args": [channelName, this.channel.pid, this.correlationId, correlation, actualTimeout]});
    queue.correlationId = this.correlationId;
    queue.correlationService = this.correlationService;
    queue.close = correlationQueueCloser;
    this.channel.addKillHook(new CorrelationKillHook(queue));
    return queue;
  },
  "registerHandler": function(channelName, criteria, handler, timeout) {
    var queue = this.register(channelName, criteria, timeout);
    queue.handler = handler;
    return queue;
  }
}
