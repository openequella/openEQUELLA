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
  if (command) { 
    vals.push({name: "event__", value:command});
  }
  args.forEach(function (c, i){
    vals.push({name: "eventp__" + i, value: c})
  });
  form.querySelectorAll("input,select,textarea").forEach(
    function (v) { 
      vals.push({name: v.name, value: v.value}); 
    }
  );
  return vals;
}

exports.setupLegacyHooks = function(ps) {
  return function() {
    window.EQ = {
      event: function(command) {
        var vals = collectParams(document.getElementById("eqpageForm"), command, [].slice.call(arguments, 1));
        ps.submit({vals:vals, callback:null});
      },
      postAjax: function(form, name, params, callback, errorcallback) {
        var vals = collectParams(form, name, params);
        ps.submit({vals:vals, callback: callback});
      },
      updateIncludes: ps.updateIncludes
    }
  }
}
