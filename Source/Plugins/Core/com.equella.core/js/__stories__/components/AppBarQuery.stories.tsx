import * as React from "react";
import { action } from "@storybook/addon-actions";
import AppBarQuery from "../../tsrc/components/AppBarQuery";

export default {
  title: "AppBarQuery",
  component: AppBarQuery,
};

export const QueryText = () => (
  <AppBarQuery query="" onChange={action("change")} />
);
