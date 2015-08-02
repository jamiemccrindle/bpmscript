function Service2() {
}
Service2.prototype = {
  test: function() {
    log.info(this.message);
  }
}
