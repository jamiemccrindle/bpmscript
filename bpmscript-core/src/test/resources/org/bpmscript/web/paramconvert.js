var form = converter.convert(this, params);
log.info(toJSON(form));
log.info(toJSON(form.customer));
log.info(form.customer.addresses[0].houseNumber);
