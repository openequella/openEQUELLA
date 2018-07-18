exports.setInnerHtml = function (props) {
    return function()
    {
      if (props.node)
      {
        $(props.node).html(props.html);
        if (props.script)
        {
          window.eval(props.script);
        }
      }
    }
}

function collectParams(form, command, args)
{
  var vals = [];
  vals.push({name: "event__", value:command});
  args.forEach(function (c, i){
    vals.push({name: "eventp__" + i, value: c})
  });
  form.querySelectorAll("input").forEach(
    function (v) { 
      vals.push({name: v.name, value: v.value}); 
    }
  );
  return vals;
}

exports.setupLegacyHooks = function(cb) {
  return function() {
    window.EQ = {
      event: function(command) {
        var vals = collectParams(document.getElementById("eqpageForm"), command, [].slice.call(arguments, 1));
        cb({vals:vals, callback:null})();
      },
      postAjax: function(form, name, params, callback, errorcallback) {
        console.log(form);
        var vals = collectParams(form, name, params);
        cb({vals:vals, callback: callback})();
      }
    }
  }
}
