import * as ReactDOM from "react-dom";
import * as React from "react";

interface ControlParameters {
  vendorId: string;
  controlType: string;
  title: string;
  element: HTMLElement;
  config: {
    something: number;
  };
}
interface CloudRegister {
  register: (
    vendorId: string,
    controlId: string,
    callback: (ControlParameters) => void
  ) => void;
}

declare const CloudControl: CloudRegister;

function TestControl(p: ControlParameters) {
  return (
    <div className="control">
      <label>
        <h3>{p.title}</h3>
        {JSON.stringify(p.config)}
      </label>
    </div>
  );
}

CloudControl.register("oeq_autotest", "testcontrol", function(params) {
  ReactDOM.render(TestControl(params), params.element);
});
