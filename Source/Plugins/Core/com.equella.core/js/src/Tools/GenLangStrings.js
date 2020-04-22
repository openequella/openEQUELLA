exports.tsStrings = require("~/../tsrc/util/langstrings").languageStrings;

exports.genStringsDynamic = function (t) {
  var strings = [];
  var recurse = function (pfx) {
    return function (val) {
      if (typeof val == "object") {
        var newOut = {};
        for (var key in val) {
          if (val.hasOwnProperty(key)) {
            recurse(pfx + "." + key)(val[key]);
          }
        }
      } else {
        strings.push(t(pfx)(val));
      }
      return strings;
    };
  };
  return recurse;
};
