exports.setInnerHtml = function (props) {
    return function()
    {
        $(props.node).html(props.html);
        if (props.script)
        {
          window.eval(props.script);
        }
        if (props.afterHtml)
        {
          props.afterHtml();
        }
    }
}

exports.globalEval = function(script)
{
  return function() {
    window.eval(script);
  }
}

exports.clearInnerHtml = function(node) {
  return function() {
    $(node).empty()
  }
}

function collectParams(form, command, args)
{
  var vals = {};
  if (command) { 
    vals["event__"] = [command];
  }
  args.forEach(function (c, i){
    var outval = c;
    switch(typeof c) {
      case 'array': 
      case 'object':
        if (c != null)
        {
          outval = JSON.stringify(c);
        }
    }
    vals["eventp__" + i] = [outval];
  });
  form.querySelectorAll("input,textarea").forEach(
    function (v) { 
      if (v.type)
      {
        switch (v.type)
        {
          case 'button': 
            return;
          case 'checkbox':
          case 'radio':
            if (!v.checked || v.disabled) 
              return;
        }
      }
      var ex = vals[v.name];
      if (ex) {
        ex.push(v.value)
      } else vals[v.name] = [v.value];
    }
  );
  form.querySelectorAll("select").forEach(
    function (v) { 
      var sel = v.selectedOptions;
      for (i=0; i<sel.length; i++) {
          var o = sel[i];
          var ex = vals[v.name];
          if (ex) {
            ex.push(o.value)
          } else vals[v.name] = [o.value];
      }
    }
  );
  return vals;
}


exports.setupLegacyHooks_ = function(ps) {
    function stdSubmit(validate) {
      return function(command) {
        if (window._trigger)
        {
          _trigger("presubmit");
          if (validate)
          {
            if (!_trigger("validate"))
            {
              return false;
            }
          }
        }
        var vals = collectParams(document.getElementById("eqpageForm"), command, [].slice.call(arguments, 1));
        ps.submit({vals:vals, callback:null});
        return false;
      }
    }
    return function() {
    window.EQ = {
      event: stdSubmit(true),
      eventnv: stdSubmit(false),
      postAjax: function(form, name, params, callback, errorcallback) {
        var vals = collectParams(form, name, params);
        ps.submit({vals:vals, callback: callback});
      },
      updateIncludes: ps.updateIncludes,
      updateForm: ps.updateForm
    }
  }
}
