require("/org/bpmscript/bpmscript.js");

function BpmScriptService(channel, definitionName) {
  this.channel = channel;
  this.address = "bpmscript-first";
  this.definitionName = definitionName;
}
BpmScriptService.prototype = {
  send: function(operation, args) {
    var future = new ContentMessageQueue(this.channel.send({"address": this.address, 
      methodName: "sendFirst", 
      args: [this.channel.parentVersion, this.definitionName, operation, args]}));
    return future;
  },
  sendSync: function(operation, args, timeout) {
    return this.send(operation, args).get(timeout);
  }
}

function LoanBroker() {}
LoanBroker.prototype = {

  /** call test **/
  requestBestRate: function(request) {

    // create a new service for calling other bpmscripts
    var loanBrokerService = new BpmScriptService(this.channel, "loanBroker");
    
    // get the credit score
    var creditScore = loanBrokerService.sendSync(
      "creditBureauGetCreditScore", [request.ssn], time.minutes(2));

    // find out which banks can service this loan
    var banks = loanBrokerService.sendSync(
      "lenderGateway", [request.amount, creditScore.score, creditScore.hlength]);
    
    // go through can call each bank in parallel
    var futures = [];

    // go through each of the banks
    for(var i = 0; i < banks.length; i++) {
      futures.push(loanBrokerService.send("bankGetLoanQuote", 
        [banks[i], request.amount, creditScore.score, 
        creditScore.hlength, request.ssn, request.term]));
    }
    
    var best = {};

    // get the responses from each bank and work out
    // which one is best
    for(var i = 0; i < futures.length; i++) {
      var rate = futures[i].get();
      if(best.rate == null || best.rate < rate) {
          best.rate = rate;
          best.bank = banks[i];
      }
    }

    // send the best rate and bank back to the customer
    this.channel.reply({content: best});
  },
  
  creditBureauGetCreditScore: function(ssn) {
    this.channel.reply({content: {score: 10, hlength:10}});
  },
  
  lenderGateway: function(amount, score, hlength) {
    this.channel.reply({content: ["bank1", "bank2", "bank3"]});
  },
  
  bankGetLoanQuote: function(bank, amount, score, hlength, ssn, term) {
    this.channel.reply({content: 5});
  }

}
