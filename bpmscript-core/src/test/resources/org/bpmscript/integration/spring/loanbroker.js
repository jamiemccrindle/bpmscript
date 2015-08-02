importPackage(org.bpmscript.integration.spring);
importPackage(org.bpmscript.loanbroker);
require("/org/bpmscript/bpmscript.js");

function SpringService(channel, address) {
  this.channel = channel;
  this.address = address;
}
SpringService.prototype = {
  send: function(payload) {
    var future = new ContentMessageQueue(this.channel.send({"address": this.address,  
      payload: payload}));
    return future;
  },
  sendSync: function(payload, timeout) {
    return this.send(payload).get(timeout);
  }
}

function LoanBroker() {}
LoanBroker.prototype = {

  /** call test **/
  requestBestRate: function(request) {

    // create a new service for calling other bpmscripts
    var creditBureauService = new SpringService(this.channel, "channel-credit-bureau");
    var lenderGatewayService = new SpringService(this.channel, "channel-lender-gateway");
    var bankService = new SpringService(this.channel, "channel-bank");
    
    // get the credit score
    var creditScore = creditBureauService.sendSync(request.ssn, time.minutes(2));

    // find out which banks can service this loan    
    var banks = lenderGatewayService.sendSync(new LenderGatewayRequest(request.amount, creditScore));
    
    // go through can call each bank in parallel
    var futures = [];

    // go through each of the banks
    for(var i = 0; i < banks.length; i++) {
      futures.push(bankService.send(new QuoteRequest(banks[i], request.amount, 
        creditScore, request.ssn, request.term)));
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
    this.channel.reply({payload: new LoanBrokerResponse(best.bank, best.rate)});
  },

}
