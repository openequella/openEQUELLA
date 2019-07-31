exports.setupBridge = function(b) {
  window["bridge"] = b;
  return function() {};
};
