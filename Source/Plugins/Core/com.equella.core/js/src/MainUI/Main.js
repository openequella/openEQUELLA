require("@material-ui/styles").install();

exports.setupBridge = function(b) {
  window.bridge = b;
};
