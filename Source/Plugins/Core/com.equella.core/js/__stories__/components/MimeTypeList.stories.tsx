import * as React from "react";
import { action } from "@storybook/addon-actions";
import { array, object } from "@storybook/addon-knobs";
import MimeTypeList from "../../tsrc/settings/Search/searchfilter/MimeTypeList";
import { MimeTypeEntry } from "../../tsrc/settings/Search/searchfilter/SearchFilterSettingsModule";

export default {
  title: "MimeTypeList",
  component: MimeTypeList,
};

const defaultMimeTypes: MimeTypeEntry[] = [
  { mimeType: "image/png", desc: "This is a Image filter" },
  { mimeType: "image/jpeg", desc: "This is a Image filter" },
];

export const listOfMimeTypes = () => (
  <MimeTypeList
    entries={object("mimetypes", defaultMimeTypes)}
    onChange={action("values of checkboxes changed")}
    selected={array("selected MIME types", ["image/png", "image/jpeg"])}
  />
);
