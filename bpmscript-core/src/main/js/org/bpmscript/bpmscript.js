function delegate(current, extension) {
  for(var name in extension) {
    var item = current[name];
    if(!item) {
      current[name] = extension[name];
    }
  }
  return extension;
}

var __errorthrower__ = new Packages.org.bpmscript.exec.ContinuationErrorThrower();

function pause() {
  __errorthrower__.throwContinuation(new Continuation());
}

function die(message) {
  __errorthrower__.throwKill(message);
}

function assertNotNull(value, message) {
  if(!value) throw new org.bpmscript.AssertionFailureException(message
    || "value is required but is null");
}

/**
 * @constructor
 */
function MessageQueue(id, channel, handler) {
	this.id = id;
	this.channel = channel;
	this.responses = new Packages.java.util.LinkedList();
	this.handler = handler;
}

MessageQueue.prototype = {

	"receive": function(timeout) {
		this.channel.waitFor(this, timeout);
		return this.responses.poll();
	},
	
	"hasMessage": function() {
		return this.responses.size() > 0;
	},
	
	"isDone": function() {
		return this.responses.size() > 0;
	},
	
	"close": function() {
		this.channel.closeQueue(this.id);
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

/**
 * @constructor
 */
function Channel(pid, branch, version, input, parentVersion, definitionName) {
  this.queues = new Packages.java.util.HashMap();
  this.killHooks = new Packages.java.util.LinkedList();
  this.input = input;
  this.pid = pid;
  this.branch = branch;
  this.version = version;
  this.parentVersion = parentVersion;
  this.definitionName = definitionName;
}

function ClosingQueueHandler(queue) {
	this.queue = queue;
}

ClosingQueueHandler.prototype = {
	"process": function() {
	  this.queue.close();
		return false;
	}
}

/**
 * @constructor
 */
Channel.prototype = {

  "closeQueue": function(id) {
  	this.queues.remove(id);
  },
  
  "createQueue": function() {
  	var queueId = java.util.UUID.randomUUID().toString();
    var messageQueue = new MessageQueue(queueId, this);
    this.queues.put(queueId, messageQueue);
    return messageQueue;
  },
  
  "send": function(message) {
    var queueId = Packages.java.util.UUID.randomUUID().toString();
    var messageQueue = new MessageQueue(queueId, this);
    this.queues.put(queueId, messageQueue);
    __scriptChannel.send(this.pid, this.branch, this.version, queueId, message);
    return messageQueue;
  },

  "callback": function(message) {
    var queueId = Packages.java.util.UUID.randomUUID().toString();
    var messageQueue = new MessageQueue(queueId, this);
    this.queues.put(queueId, messageQueue);
    var message = __scriptChannel.createCallback(this.pid, this.branch, this.version, queueId, message);
    return [messageQueue, message];
  },

  "sendSync": function(message, timeout) {
    var queue = this.send(message);
    if(timeout) {
      return queue.receive(timeout);
    } else {
      return queue.receive();
    }
  },

  "sendOneWay": function(message) {
    __scriptChannel.sendOneWay(message);
  },

  "getContent": function(message) {
    return __scriptChannel.getContent(this, message);
  },

  "sendTimeout": function(timeout) {
  	var queueId = Packages.java.util.UUID.randomUUID().toString() + "timeout";
    var messageQueue = new MessageQueue(queueId, this);
    messageQueue.handler = new ClosingQueueHandler(messageQueue);
    this.queues.put(queueId, messageQueue);
  	__scriptChannel.sendTimeout(this.pid, this.branch, this.version, queueId, timeout);
    return messageQueue;
  },

  "sleep": function(timeout) {
    this.sendTimeout(timeout).get();
  },

  "reply": function(in1, in2) {
    if(in2) {
      __scriptChannel.reply(this.pid, in1, in2);
    } else {
      __scriptChannel.reply(this.pid, this.input, in1);
    }
  },

  "getConfiguration": function() {
    return __scriptChannel.getDefinitionConfiguration(this.definitionName).getProperties();
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
    while(result == null || !argQueues[result.id]) {
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
  
  "addKillHook": function(hook) {
    this.killHooks.add(hook);
  },  
  
  "kill": function(message) {
    var iterator = this.killHooks.iterator();
    while(iterator.hasNext()) {
      var hook = iterator.next();
      try {
        hook.process(message);
      } catch(error) {
        // could log something out here...
        log.error("error calling kill hook: " + error);
      }
    }
    die(message);
  },

  /* Get the next queue to come back (ignores queues that
   * have already returned)
   */
  "getNext": function() {
      var aresult = pause();
      if(aresult.getType() == "KILL") {
        log.info("got kill message");
        this.kill(aresult.getMessage());
        return null;
      } else {
        // inbound message to this queue
        var queueId = aresult.getQueueId();
         var queue = this.queues.get(queueId);
         if(queue != null) {
           var message = aresult.getMessage();
           this.branch = aresult.getBranch();
           this.version = aresult.getVersion();
           if(queue.handler != null) {
             if(queue.handler.process(message)) {
               queue.addResponse(message);
             }
           } else {
             queue.addResponse(message);
           }
         return queue;
       } else {
         throw "invalid queue " + queueId;
       }
     }
   }
}

function Service(channel, name) {
  this.name = name;
  this.channel = channel;
}
Service.prototype = {
  __noSuchMethod__: function(methodName, args) {
    return this.channel.invoke(this.name, methodName, args);
  }
}

function ContentMessageQueue(messageQueue) {
  this.messageQueue = delegate(this, messageQueue);
}
ContentMessageQueue.prototype = {
  get: function(timeout) {
    var result = this.messageQueue.get(timeout);
    if(result != null) {
      return this.channel.getContent(result);
    } else {
      return null;
    }
  }
}

var time = new org.bpmscript.timeout.TimeCalculator();

function uuid() {
  return java.util.UUID.randomUUID().toString();
}

function wireService(serviceDefinition) {
  if(serviceDefinition.getSource()) {
    require(serviceDefinition.getSource());
  }
  var constructor = topLevelScope()[serviceDefinition.name];
  var serviceInstance = new constructor();
  var properties = serviceDefinition.properties;
  if(properties) {
    var entries = properties.entrySet().iterator();
    while(entries.hasNext()) {
      var entry = entries.next();
      if(entry.value instanceof org.bpmscript.js.IJavascriptService) {
        serviceInstance[entry.key] = wireService(entry.value);
      } else {
        serviceInstance[entry.key] = entry.value;
      }
    }
  }
  serviceInstance.init();
  return serviceInstance;
}

function process(constructorFunc, definitionName, operation, parentVersion, pid, branch, version, message) {
  var channel = new Channel(pid, branch, version, message, parentVersion, definitionName);
  var content = channel.getContent(message);
  var args = new Array();
  if(content instanceof Array) {
    for(var i = 0; i < content.length; i++) {
      args.push(content[i]);
    }
  } else {
    args.push(content);
  }
  var instance = new constructorFunc();
  instance.channel = channel;
  instance.message = message;
  instance.operation = operation;
  {
    var properties = channel.getConfiguration();
    var entries = properties.entrySet().iterator();
    while(entries.hasNext()) {
      var entry = entries.next();
      instance[entry.key] = entry.value;
      if(entry.value instanceof org.bpmscript.js.IJavascriptService) {
        instance[entry.key] = wireService(entry.value);
      } else {
        instance[entry.key] = entry.value;
      }
      entry = null;
    }
    entries = null;
  }
  if(instance.init) {
    instance.init();
  }
  if(instance.process) {
    return instance.process(definitionName, operation, args);
  } else {
    return instance[operation].apply(instance, args);
  }
}
