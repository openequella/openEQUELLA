
exports.renderData = function()
{
    return typeof renderData != "undefined" ? renderData : {newUI:false};
}();

exports.logoPath = function(){
  return isCustomLogo ? "api/themeresource/newLogo.png" : "p/r/6.7.r88/com.equella.core/images/new-equella-logo.png"
}();
exports.preventUnload = function(e) {
    e.returnValue = "Are you sure?";
    return "Are you sure?";
}

exports.setDocumentTitle = function(t) {
    return function() {
        window.document.title = t;
    }
}
