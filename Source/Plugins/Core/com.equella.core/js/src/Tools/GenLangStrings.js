exports.courseString = require("~/../tsrc/course/SearchCourse").strings;
exports.courseEditString = require("~/../tsrc/course/EditCourse").strings;
exports.entityStrings = require("~/../tsrc/entity").entityStrings;
exports.uiThemeSettingStrings = require("~/../tsrc/theme/ThemePage").strings;
exports.templateStrings = require("~/../tsrc/mainui/Template").strings;
exports.templateCoreStrings = require("~/../tsrc/mainui/Template").coreStrings;

exports.genStringsDynamic = function(t) {
  var strings = [];
  var recurse = function(pfx) {
    return function(val) {
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
