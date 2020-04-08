import React from "react";
import { action } from "@storybook/addon-actions";
import { boolean, select, text } from "@storybook/addon-knobs";
import MessageInfo from "../tsrc/components/MessageInfo";

export default {
  title: "MessageInfo",
  component: MessageInfo
};

export const DynamicVariant = () => (
  <MessageInfo
    open={boolean("open", true)}
    onClose={action("close")}
    title={text(
      "title",
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    )}
    variant={select(
      "variant",
      { success: "success", error: "error", info: "info", warning: "warning" },
      "success"
    )}
  />
);

export const VarientSuccess = () => (
  <MessageInfo
    open={boolean("open", true)}
    onClose={action("close")}
    title={text(
      "title",
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    )}
    variant="success"
  />
);

export const VariantError = () => (
  <MessageInfo
    open={boolean("open", true)}
    onClose={action("close")}
    title={text(
      "title",
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    )}
    variant="error"
  />
);

export const VariantInfo = () => (
  <MessageInfo
    open={boolean("open", true)}
    onClose={action("close")}
    title={text(
      "title",
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    )}
    variant="info"
  />
);

export const VariantWarning = () => (
  <MessageInfo
    open={boolean("open", true)}
    onClose={action("close")}
    title={text(
      "title",
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    )}
    variant="warning"
  />
);
