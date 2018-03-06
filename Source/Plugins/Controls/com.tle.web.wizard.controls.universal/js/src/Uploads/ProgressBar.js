
exports.runProgress = function(el) {
  return function(p) {
    return function() {
      if (el)
      {
        $(el).progression({Current:p});
      }
    }
  }
}
