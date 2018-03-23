
exports.renderData = function()
{
    require('es6-object-assign').polyfill();
    return typeof renderData != "undefined" ? renderData : {newUI:false};
}();
