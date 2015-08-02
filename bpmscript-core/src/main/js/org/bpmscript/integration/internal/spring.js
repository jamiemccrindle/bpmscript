function SpringService(channel, address, defaultTimeout) {
  this.channel = channel;
  this.address = address;
  this.defaultTimeout = defaultTimeout;
}

SpringService.prototype = {
  "sendSync": function(methodName, args, timeout) {
    var future = this.channel.send({"address": this.address, 
      methodName: methodName, 
      args: args});
    return future.get(timeout ? timeout : this.defaultTimeout);
  },
  "send": function(methodName, args) {
    var future = this.channel.send({"address": this.address, 
      methodName: methodName, 
      args: args});
    return future.get(this.defaultTimeout);
  }
}
