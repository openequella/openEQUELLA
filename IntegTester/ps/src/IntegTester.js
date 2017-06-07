const md5 = require("blueimp-md5");

exports.md5 = function(s) {
    return md5(s, null, true);
}
