function resolveUrl(url) {
  return new URL(url, $("base").attr("href")).href;
}
exports.resolveUrl = resolveUrl;

exports.setInnerHtml = function(props) {
  return function() {
    $(props.node).html(props.html);
    if (props.script) {
      window.eval(props.script);
    }
    if (props.afterHtml) {
      props.afterHtml();
    }
  };
};

exports.globalEval = function(script) {
  return function() {
    window.eval(script);
  };
};

exports.clearInnerHtml = function(node) {
  return function() {
    $(node).empty();
  };
};

function collectParams(form, command, args) {
  var vals = {};
  if (command) {
    vals["event__"] = [command];
  }
  args.forEach(function(c, i) {
    var outval = c;
    switch (typeof c) {
      case "array":
      case "object":
        if (c != null) {
          outval = JSON.stringify(c);
        }
    }
    vals["eventp__" + i] = [outval];
  });
  form.querySelectorAll("input,textarea").forEach(function(v) {
    if (v.type) {
      switch (v.type) {
        case "button":
          return;
        case "checkbox":
        case "radio":
          if (!v.checked || v.disabled) return;
      }
    }
    var ex = vals[v.name];
    if (ex) {
      ex.push(v.value);
    } else vals[v.name] = [v.value];
  });
  form.querySelectorAll("select").forEach(function(v) {
    for (i = 0; i < v.length; i++) {
      var o = v[i];
      if (o.selected) {
        var ex = vals[v.name];
        if (ex) {
          ex.push(o.value);
        } else vals[v.name] = [o.value];
      }
    }
  });
  return vals;
}

exports.setupLegacyHooks_ = function(ps) {
  function stdSubmit(validate) {
    return function(command) {
      if (window._trigger) {
        _trigger("presubmit");
        if (validate) {
          if (!_trigger("validate")) {
            return false;
          }
        }
      }
      var vals = collectParams(
        document.getElementById("eqpageForm"),
        command,
        [].slice.call(arguments, 1)
      );
      ps.submit({ vals: vals, callback: null });
      return false;
    };
  }

  return function() {
    window.EQ = {
      event: stdSubmit(true),
      eventnv: stdSubmit(false),
      postAjax: function(form, name, params, callback, errorcallback) {
        var vals = collectParams(form, name, params);
        ps.submit({ vals: vals, callback: callback });
      },
      updateIncludes: ps.updateIncludes,
      updateForm: ps.updateForm
    };
  };
};

exports.updateStylesheets_ = function(replace) {
  return function(_sheets) {
    const sheets = _sheets.map(resolveUrl);
    return function(onError, onSuccess) {
      const doc = window.document;
      const insertPoint = doc.getElementById("_dynamicInsert");
      const head = doc.getElementsByTagName("head")[0];
      var current = insertPoint.previousElementSibling;
      const existingSheets = {};
      while (current != null && current.tagName == "LINK") {
        existingSheets[current.href] = current;
        current = current.previousElementSibling;
      }
      const lastSheet = sheets.reduce(function(lastLink, cssUrl) {
        if (existingSheets[cssUrl]) {
          delete existingSheets[cssUrl];
          return lastLink;
        } else {
          var newCss = doc.createElement("link");
          newCss.rel = "stylesheet";
          newCss.href = cssUrl;
          head.insertBefore(newCss, insertPoint);
          return newCss;
        }
      }, null);
      const deleteSheets = function() {
        if (replace) {
          for (key in existingSheets) {
            head.removeChild(existingSheets[key]);
          }
        }
      };
      if (!lastSheet) onSuccess(deleteSheets);
      else {
        const loaded = function(w, e) {
          onSuccess(deleteSheets);
        };
        lastSheet.addEventListener("load", loaded, false);
      }
    };
  };
};

exports.loadMissingScripts_ = function(_scripts) {
  return function(onError, onSuccess) {
    const scripts = _scripts.map(resolveUrl);
    const doc = window.document;
    const head = doc.getElementsByTagName("head")[0];
    const scriptTags = doc.getElementsByTagName("script");
    const scriptSrcs = {};
    for (let i = 0; i < scriptTags.length; i++) {
      const scriptTag = scriptTags[i];
      if (scriptTag.src) {
        scriptSrcs[scriptTag.src] = true;
      }
    }
    const lastScript = scripts.reduce(function(lastScript, scriptUrl) {
      if (scriptSrcs[scriptUrl]) {
        return lastScript;
      } else {
        let newScript = doc.createElement("script");
        newScript.src = scriptUrl;
        newScript.async = false;
        head.appendChild(newScript);
        console.log(scriptUrl);
        return newScript;
      }
    }, null);
    if (!lastScript) onSuccess();
    else lastScript.addEventListener("load", onSuccess, false);
  };
};
