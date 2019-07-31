const md5 = require("js-md5");

exports.md5AndBase64 = function(s) {
  return md5.base64(s);
};
