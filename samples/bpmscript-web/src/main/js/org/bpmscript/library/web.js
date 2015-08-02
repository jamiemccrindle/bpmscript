importClass(org.bpmscript.paging.OrderBy);
importClass(org.bpmscript.paging.Query);

function ResubmitHandler(channel, correlatorAddress) {
  this.channel = channel;
  this.correlatorAddress = correlatorAddress;
}

ResubmitHandler.prototype = {
  "process": function(message) {
    var content = this.channel.getContent(message);
    this.channel.send({address: this.correlatorAddress, args: [content.getReplyTo(), content.getCorrelationId(), new Packages.org.bpmscript.InvalidQueueException()]});
    return false;
  }
}

function WebConversation(input, channel, correlatorAddress, timeout) {
  this.resubmitHandler = new ResubmitHandler(channel, correlatorAddress);
  this.replyTo = input.getReplyTo();
  this.correlationId = input.getCorrelationId();
  this.channel = channel;
  this.correlatorAddress = correlatorAddress;
  this.timeout = timeout;
}

WebConversation.prototype = {
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
  "showForm": function(data) {
    var request = this.respond(data);
    return org.bpmscript.web.ParamToJsConverter.DEFAULT_INSTANCE.convert(this, request.getParameterMap());
  },
  "showAndValidateForm": function(response, validator) {
    if(!response.errors) response.errors = {};
    do {
      response.form = this.showForm(response).form;
      response.errors = new WebErrors();
      log.info("before validation, with form of " + toJSON(response.form));
      response.errors.addErrors(validator.validate(response.form));
    } while(response.form.__next && response.errors.hasErrors());
    return response.form; 
  },
  "end": function(data) {
    this.channel.sendOneWay({address: this.correlatorAddress, args: [this.replyTo, this.correlationId, data]});
    // TODO: could close open queues here
  }
}

function WebErrors() {
  this.__global = [];
}

WebErrors.prototype = {
  "hasErrors": function() {
    for each (var item in this) {
      if(item instanceof Array && item.length > 0) {
        return true;
      }
    }
    return false;
  },
  "addErrors": function(errors) {
    if(errors) {
      log.info(toJSON(errors));
      for (var item in errors) {
        if(!this[item]) {
          this[item] = new Array();
        }
        if(this[item] instanceof Array) {
          for each(var value in errors[item]) this[item].push(value);
        }
      }
    }
  }
}

function WebErrorMessage(code, message, args) {
  this.code = code;
  if(message) this.message = message;
  if(args) this.args = args;
}

function WebValidator() {}
WebValidator.prototype = {
  "required": function(value, name, message) {
    if(value == null || value.length == 0) {
      var result = {};
      result[name] = [new WebErrorMessage("error.required", message)]; 
      return result;
    }
    return [];
  },
  "matches": function(value, regex, name, message) {
    if(!regex.test(value)) {
      var result = {};
      result[name] = [new WebErrorMessage("error.nomatch", message, [regex.toString()])]; 
      return result;
    }
    return [];
  }
}

function Controller() {
}
Controller.prototype = {
  "process": function(definitionName, operation, args) {
    var [request] = args;
    var command = org.bpmscript.web.ParamToJsConverter.DEFAULT_INSTANCE.convert(this, request.getParameterMap());
    var result = this[operation](command);
    if(result) {
      if(!result.view) { result.view = definitionName.toLowerCase() + "/" + operation; }
      var response = {content:result};
      this.channel.reply(response);
    }
  }
}

function ConversationController(conversationAddress) {
  this.conversationAddress = conversationAddress || "conversation";
}
ConversationController.prototype = {
  process: function(definitionName, operation, args) {
    var [request] = args;
    var command = org.bpmscript.web.ParamToJsConverter.DEFAULT_INSTANCE.convert(this, request.getParameterMap());
    var browser = new WebConversation(this.message, this.channel, this.conversationAddress, time.minutes(30));
    var result = this[operation](browser, command);
  }
}

function Paging(defaults) { this.defaults = defaults || {}; }
Paging.prototype = {
  query: function(command) {
    command = command ? command : {};
    var max = command.max || this.defaults.maxResults || 10;
    var first = command.first || this.defaults.firstResult || 0;
    var sort = command.sort;
    var direction = command.direction || "asc";
    var orderBy = null;
    if(sort) {
      orderBy = new OrderBy(sort, direction == "asc");
    }
    var query = new Query(orderBy != null ? [orderBy] : [], first, max);
    return query;
  }
}

