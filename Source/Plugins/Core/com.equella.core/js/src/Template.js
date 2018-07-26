
exports.renderData = function()
{
    return typeof renderData != "undefined" ? renderData : {newUI:false};
}();

exports.preventUnload = function(e) {
    e.returnValue = "Are you sure?";
    return "Are you sure?";
}