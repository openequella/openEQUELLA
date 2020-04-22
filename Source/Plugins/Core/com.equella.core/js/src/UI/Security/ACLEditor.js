const RD = require("react-dom");

exports.renderToPortal = function (portal) {
  return function (child) {
    return RD.createPortal(child, portal);
  };
};
