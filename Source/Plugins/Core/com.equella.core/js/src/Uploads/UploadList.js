exports.updateCtrlErrorText = function(ctrlId, text) {
  var contElem = document.querySelector("DIV#" + ctrlId + " > DIV.control");
  if (text == "") contElem.classList.remove("ctrlinvalid");
  else contElem.classList.add("ctrlinvalid");
  contElem.querySelector("P.ctrlinvalidmessage").textContent = text;
};

exports.updateDuplicateMessage = function(id, display) {
  // The div id of all duplicate warning messages automatically follows
  // this format: its parent div id concatenated with "_duplicateWarningMessage"
  var duplicateMessageDiv = document.querySelector(
    "#" + id + "_attachment_duplicate_warning"
  );
  if (duplicateMessageDiv != null) {
    if (display) {
      duplicateMessageDiv.setAttribute("style", "display:inline");
    } else {
      duplicateMessageDiv.setAttribute("style", "display:none");
    }
  }
};

exports.simpleFormat = function(format) {
  return function(args) {
    return format.replace(/{(\d+)}/g, function(match, number) {
      return typeof args[number] != "undefined" ? args[number] : match;
    });
  };
};

exports.register = function(exp) {
  window.UploadList = exp;
};
