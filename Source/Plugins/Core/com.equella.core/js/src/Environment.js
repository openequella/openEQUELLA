exports.baseUrl = $("base").attr("href");

const langStrings = require("~/../tsrc/util/langstrings").prepLangStrings;

exports.prepLangStrings = function(s) {
  return langStrings(s.prefix, s.strings);
};
