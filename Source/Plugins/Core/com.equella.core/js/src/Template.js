
exports.renderData = function()
{
    require('es6-object-assign').polyfill();
    require('jspolyfill-array.prototype.find');
    require('css-vars-ponyfill')({variables: {"--top-bar": "64px"}});
    require('promise/polyfill');
    require('sprintf-js');
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