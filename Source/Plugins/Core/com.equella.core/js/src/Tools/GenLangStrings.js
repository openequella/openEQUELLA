exports.courseString = require("course/SearchCourse").strings;
exports.courseEditString = require("course/EditCourse").strings;
exports.entityStrings = require("entity").entityStrings;
exports.uiThemeSettingStrings = require("theme/ThemePage").strings;
exports.genStringsDynamic = function(t)
{
    var strings = [];
    var recurse = function(pfx)
    {
        return function(val)
        {
            if (typeof val == "object")
            {
                var newOut = {};
                for (var key in val) {
                    if (val.hasOwnProperty(key)) {
                        recurse(pfx+"."+key)(val[key]);
                    }
                }
            }
            else {
                strings.push(t(pfx)(val));
            }
            return strings;
        }
    }
    return recurse;
}
