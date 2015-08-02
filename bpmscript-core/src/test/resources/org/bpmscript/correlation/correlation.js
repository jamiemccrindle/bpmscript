require("/org/bpmscript/bpmscript.js");

function CorrelationEngine(channel) {
	this.channel = channel;
}

CorrelationEngine.prototype = {
	"register": function(criteria) {
	  var instanceCorrelation = new Packages.org.bpmscript.correlation.memory.InstanceCorrelation();
		for each (var criterion in criteria) {
			instanceCorrelation.addCriteria(criterion[0], criterion[1]);
		}
    var queue = this.channel.createQueue();
		correlationService.addInstanceCorrelation(this.channel.pid, queue.id, instanceCorrelation);
		return queue;
	}
}
