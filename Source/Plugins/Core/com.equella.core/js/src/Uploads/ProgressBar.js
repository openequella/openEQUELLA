exports.runProgress = function (p) {
  return function (el) {
    return function () {
      if (el) {
        $(el).progression({ Current: p });
      }
    };
  };
};
