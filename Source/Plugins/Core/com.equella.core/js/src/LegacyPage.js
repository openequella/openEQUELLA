exports.setInnerHtml = function (html) {
  return function(ref)
  {
    return function()
    {
      if (ref)
      {
        $(ref).html(html);
      }
    }
  }
}

exports.setupLegacyHooks = function(cb) {
  return function() {
    window.EQ = {
      event: function(command) {
        var vals = [];
        vals.push({name: "event__", value:command});
        document.querySelector("#eqForm").querySelectorAll("input").forEach(
          function (v) { 
            vals.push({name: v.name, value: v.value}); 
          }
        );
        cb(vals)();
      }
    }
  }
}
const loadJsCss = require('load-js-css');
exports.loadResources = function(resources)
{
  return function() {
    loadJsCss.list(resources);
  }
}