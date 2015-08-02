require("/org/bpmscript/bpmscript.js");
require("/org/bpmscript/correlation/correlation.js");

function processTest(input, channel) {

	  var correlationEngine = new CorrelationEngine(channel);
    var queue = correlationEngine.register(
    	[
    		["new XML(message).invoiceId.text().toString()", "12345"],
    		["utils.className(message)", "java.lang.String"]
    	]);
		var response = queue.receive();
		channel.reply({"content": new XML(response).invoiceId.text().toString()});

}
