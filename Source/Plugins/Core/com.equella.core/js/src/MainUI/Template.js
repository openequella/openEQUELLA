exports.renderData = function()
{
    return typeof renderData != "undefined" ? renderData : {newUI:false};
}();

exports.logoPath = logoURL;
exports.preventUnload = function(e) {
    e.returnValue = "Are you sure?";
    return "Are you sure?";
}

exports.setDocumentTitle = function(t) {
    return function() {
        window.document.title = t;
    }
}
