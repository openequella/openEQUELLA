import * as React from "react";
import Error from "../../tsrc/components/Error";

export default {
  title: "Error",
  component: Error
};

export const WithWarningMessage = () => (
  <Error>
    <h1>Warning</h1>
    <p>Something has gone wrong.</p>
  </Error>
);
