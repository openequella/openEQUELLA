const Luxon = require("luxon");

exports.parseIsoToLuxon = function(s) {
  return Luxon.DateTime.fromISO(s);
};

exports.luxonDateToIso = function(d) {
  return d.toISO();
};

exports._dateToLuxon = function(o) {
  return Luxon.DateTime.local(o.y, o.m, o.d);
};

exports.luxonFormats = Luxon.DateTime;

exports.luxonFormat = function(dt) {
  return function(format) {
    return dt.toLocaleString(format);
  };
};
