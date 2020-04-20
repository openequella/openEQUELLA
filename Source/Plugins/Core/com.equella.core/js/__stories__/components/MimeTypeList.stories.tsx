import * as React from "react";
import { action } from "@storybook/addon-actions";
import { array } from "@storybook/addon-knobs";
import MimeTypeList from "../../tsrc/settings/Search/searchfilter/MimeTypeList";
import { MimeTypeEntry } from "../../tsrc/settings/Search/searchfilter/SearchFilterSettingsModule";

export default {
  title: "SearchResult",
  component: MimeTypeList
};

export const defaultMimeTypes: MimeTypeEntry[] = [
  { mimeType: "image/png", desc: "This is a Image filter" },
  { mimeType: "image/jpeg", desc: "This is a Image filter" }
];

export const listOfMimeTypes = () => (
  <MimeTypeList
    entries={defaultMimeTypes}
    onChange={action("values of checkboxes changed")}
    selected={array("selected MIME types", ["image/png"])}
  />
);
