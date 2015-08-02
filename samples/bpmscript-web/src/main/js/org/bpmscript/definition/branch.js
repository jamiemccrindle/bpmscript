require("/org/bpmscript/bpmscript.js");
require("/org/bpmscript/library/web.js");

function Branch(configuration) {
  this.controller = delegate(this, new Controller());
}
Branch.prototype = {
  "branch": function(command) {
    var id = command.id;
    var branches = this.continuationJournal.getBranchesForPid(id);
    for(var branch in Iterator(branches.iterator())) {
      branchesAndStatus.push({branch: branch, state: this.continuationJournal.getProcessStateLatest(branch).name()});
    }
    return {value: instance, branches: branches, branchesAndStatus: branchesAndStatus};
  }
}
