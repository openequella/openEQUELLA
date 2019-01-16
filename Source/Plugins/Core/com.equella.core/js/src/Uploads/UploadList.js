exports.updateCtrlErrorText = function(ctrlId, text) {
  var contElem = document.querySelector("DIV#" + ctrlId + " > DIV.control");
  if (text == "") contElem.classList.remove("ctrlinvalid");
  else contElem.classList.add("ctrlinvalid");
  contElem.querySelector("P.ctrlinvalidmessage").textContent = text;
};

exports.simpleFormat = function(format) {
  return function(args) {
    return format.replace(/{(\d+)}/g, function(match, number) {
      return typeof args[number] != "undefined" ? args[number] : match;
    });
  };
};
