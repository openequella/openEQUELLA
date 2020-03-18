/* global renderData */

exports.renderData = (function() {
  return typeof renderData != "undefined" ? renderData : { newUI: false };
})();

exports.templateClass = require("~/../tsrc/mainui/Template").Template;
