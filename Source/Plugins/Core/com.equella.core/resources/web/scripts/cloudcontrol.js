var CloudControl = {
  register: function(vendorId, controlType, callback) {
    CloudControl.registrations[vendorId + "_" + controlType] = callback;
  },
  registrations: {},
  render: function(params) {
    CloudControl.registrations[params.vendorId + "_" + params.controlType](
      params
    );
  }
};
