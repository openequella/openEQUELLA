exports.baseUrl = $("base").attr("href");

const langStrings = require("util/langstrings").prepLangStrings;

exports.prepLangStrings = function(s) {
  return langStrings(s.prefix, s.strings);
};
