import React from "react";
import { action } from "@storybook/addon-actions";
import { text } from "@storybook/addon-knobs";
import RichTextEditor from "../tsrc/components/RichTextEditor";

/**
 * FIXME: to get the tinyMCE skin styles with the current setup
 * the entire node modules folder needed to be included as a static source
 * in the future only the skin styles should be served as a static folder
 *
 * When an alternative is ready update the `storybook` and `build-storybook` scripts
 * in package.json with the new `-s` option
 */
export default {
  title: "RichTextEditor",
  component: RichTextEditor
};

export const WithHTMLInput = () => (
  <RichTextEditor
    htmlInput={text("htmlInput", "<p>example</p>")}
    onStateChange={action("stateChange")}
  />
);

export const WithoutHTMLInput = () => (
  <RichTextEditor onStateChange={action("stateChange")} />
);
