exports.baseUrl = $("base").attr('href')

exports.prepLangStrings = function(t) {
  if (typeof bundle == "undefined")
    return t.value1;
  var overrideVal = function(prefix, val)
  {
    if (typeof val == "object")
    {
      var newOut = {};
      for (var key in val) {
        if (val.hasOwnProperty(key)) {
          newOut[key] = overrideVal(prefix+"."+key, val[key])
        }
      }
      return newOut;
    }
    else {
      var overriden = bundle[prefix];
      if (overriden != undefined)
      {
        return overriden;
      }
      return val;
    }
  }
  return overrideVal(t.value0, t.value1);
}
