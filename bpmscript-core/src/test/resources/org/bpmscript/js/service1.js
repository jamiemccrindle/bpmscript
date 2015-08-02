function Service1() {
  this.service = null;
}
Service1.prototype = {
  test: function() {
    log.info("Service1");
    this.service2.test();
    this.service3.test();
  }
}
