
exports.renderData = function()
{
    return typeof renderData != "undefined" ? renderData : {newUI:false};
}();

exports.setTitle = function(title) {
    return function() {
        document.title = title;
    }
}

exports.preventUnload = function(e) {
    e.returnValue = "Are you sure?";
    return "Are you sure?";
}