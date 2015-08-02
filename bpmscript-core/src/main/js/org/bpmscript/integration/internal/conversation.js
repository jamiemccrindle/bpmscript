importClass(org.bpmscript.InvalidQueueException);

function ResubmitHandler(channel, correlatorAddress) {
  this.channel = channel;
  this.correlatorAddress = correlatorAddress;
}

ResubmitHandler.prototype = {
  "process": function(message) {
    var content = this.channel.getContent(message);
    this.channel.send({address: this.correlatorAddress, args: [content.getReplyTo(), content.getCorrelationId(), new InvalidQueueException()]});
    return false;
  }
}

/** 
 * @param {Object} input
 * @param {Channel} channel
 * @param {String} correlatorAddress
 * @param {Number} timeout
 */
function Conversation(input, channel, correlatorAddress, timeout) {
  this.resubmitHandler = new ResubmitHandler(channel, correlatorAddress);
  this.replyTo = input.getReplyTo();
  this.correlationId = input.getCorrelationId();
  this.channel = channel;
  this.correlatorAddress = correlatorAddress;
  this.timeout = timeout;
}

Conversation.prototype = {
  "respond": function(data) {
    var queue = this.channel.send({address: this.correlatorAddress, args: [this.replyTo, this.correlationId, data]});
    var result = queue.receive(this.timeout);
    queue.handler =  this.resubmitHandler;
    if(result != null) {
      var content = this.channel.getContent(result);
      this.replyTo = content.getReplyTo();
      this.correlationId = content.getCorrelationId();
      return content.getArgs()[0];
    } else {
      return null;
    }
  },
  "end": function(data) {
    this.channel.sendOneWay({address: this.correlatorAddress, args: [this.replyTo, this.correlationId, data]});
    // could close open queues here
  }
}
