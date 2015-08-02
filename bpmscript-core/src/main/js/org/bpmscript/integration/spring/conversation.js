importClass(org.bpmscript.InvalidQueueException);

function SpringResubmitHandler(channel, correlatorAddress) {
  this.channel = channel;
  this.correlatorAddress = correlatorAddress;
}

SpringResubmitHandler.prototype = {
  "process": function(message) {
    var content = this.channel.getContent(message);
    this.channel.send({address: this.correlatorAddress,
      attributes: { 
        conversationReturnAddress: content.getHeader().getAttribute("serializedReturnAddress"), 
        conversationId: content.getHeader().getCorrelationId()
      }, 
      payload: new InvalidQueueException()});
    return false;
  }
}

/** 
 * @param {Object} input
 * @param {Channel} channel
 * @param {String} correlatorAddress
 * @param {Number} timeout
 */
function SpringConversation(input, channel, correlatorAddress, timeout) {
  this.resubmitHandler = new SpringResubmitHandler(channel, correlatorAddress);
  this.replyTo = input.getHeader().getAttribute("serializedReturnAddress");
  this.correlationId = input.getHeader().getCorrelationId();
  this.channel = channel;
  this.correlatorAddress = correlatorAddress;
  this.timeout = timeout;
}

SpringConversation.prototype = {
  "respond": function(data) {
    var queue = this.channel.send({address: this.correlatorAddress, 
      attributes: {
        conversationReturnAddress: this.replyTo, 
        conversationId: this.correlationId
      },
      payload: data});
    var result = queue.receive(this.timeout);
    queue.handler =  this.resubmitHandler;
    if(result != null) {
      var content = this.channel.getContent(result);
      this.replyTo = content.getHeader().getAttribute("serializedReturnAddress");
      this.correlationId = content.getHeader().getCorrelationId();
      return content.getPayload();
    } else {
      return null;
    }
  },
  "end": function(data) {
    this.channel.sendOneWay({address: this.correlatorAddress, 
      attributes: {
        conversationReturnAddress: this.replyTo, 
        conversationId: this.correlationId
      },
      payload: data});
    // could close open queues here
  }
}
