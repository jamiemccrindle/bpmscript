require("/org/bpmscript/bpmscript.js");
require("/org/bpmscript/library/web.js");

function ContactValidator() {
  this.validator = delegate(this, new WebValidator()); 
}
ContactValidator.prototype = {
  "validate": function(form) {
    log.info("in contact validator");
    return this.required(form.name, "form.name");
  }
}
function AddressValidator() {
  this.validator = delegate(this, new WebValidator()); 
}
AddressValidator.prototype = {
  "validate": function(form) {
    var errors = new WebErrors();
    errors.addErrors(this.required(form.street, "form.street"));
    errors.addErrors(this.required(form.house, "form.house"));
    errors.addErrors(this.required(form.postcode, "form.postcode"));
    return errors;
  }
}

function CardValidator() {
  this.validator = delegate(this, new WebValidator()); 
}
CardValidator.prototype = {
  "validate": function(form) {
    var errors = new WebErrors();
    errors.addErrors(this.required(form.cardnumber, "form.cardnumber"));
    errors.addErrors(this.matches(form.cardnumber, /\d+/, "form.cardnumber", "error.cardnumber.match"));
    return errors;
  }
}

function Checkout(channel) {
  this.controller = delegate(this, new ConversationController());
}
Checkout.prototype = {
  index: function(browser, command) {
  
    // create and show the first contact form
    var contact1 = browser.showAndValidateForm({view: "checkout/contact",
      form: {name: "", country: "uk"}, 
      message: "Please enter the first persons contact details", 
      countries: {uk: "UK", za: "South Africa"}}, new ContactValidator());
    var name = contact1.name;
    
    // create and show the first contact form
    var address = browser.showAndValidateForm({view: "checkout/address",
      form: {house:"", postcode:"", street:""}, 
      message: "Please enter your address"}, new AddressValidator());
    
    // create and show the second contact form
    var contact2 = browser.showAndValidateForm({view: "checkout/contact",
      form: {name: "", country: "uk"}, 
      message: "Please enter the second persons contact details", 
      countries: {uk: "UK", za: "South Africa"}}, new ContactValidator());
    name += " and " + contact2.name;
  
    // create and show the credit card form
    var card = browser.showAndValidateForm({
      view: "checkout/payment",
      form: {cardtype: "mastercard", cardnumber: ""},
      message: "Please enter your card details", 
      cardtypes: {mastercard: "Master Card", visa: "Visa"}}, new CardValidator());
      
    // show the final page
    browser.end({view: "checkout/confirmation", message: "Bla blah blah for registering " + name});
  }
}

// wah gen how (tone drops in how)