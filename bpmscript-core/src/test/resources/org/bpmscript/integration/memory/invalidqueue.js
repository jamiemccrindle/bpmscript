require("/org/bpmscript/bpmscript.js");

function processTest(input, channel, total) {

	var result = channel.sendSync({"address": "badaddress", 
	  args: ["hi"]}, time.minutes(10));

	channel.reply({content: "sub"});

}
