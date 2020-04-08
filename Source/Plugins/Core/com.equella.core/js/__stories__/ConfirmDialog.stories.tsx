import * as React from "react";
import { action } from "@storybook/addon-actions";
import { boolean, text } from "@storybook/addon-knobs";
import ConfirmDialog from "../tsrc/components/ConfirmDialog";

export default {
  title: "ConfirmDialog",
  component: ConfirmDialog
};

export const ShowDialog = () => (
  <ConfirmDialog
    open={boolean("open", true)}
    title={text(
      "title",
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    )}
    onCancel={action("cancel")}
    onConfirm={action("confirm")}
  />
);
