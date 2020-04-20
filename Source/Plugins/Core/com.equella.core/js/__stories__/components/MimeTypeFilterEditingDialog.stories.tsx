import * as React from "react";
import { action } from "@storybook/addon-actions";
import { boolean } from "@storybook/addon-knobs";
import MimeTypeFilterEditingDialog from "../../tsrc/settings/Search/searchfilter/MimeTypeFilterEditingDialog";
import { MimeTypeFilter } from "../../tsrc/settings/Search/searchfilter/SearchFilterSettingsModule";

export default {
  title: "SearchResult",
  component: MimeTypeFilterEditingDialog
};
export const mimeTypeFilter: MimeTypeFilter = {
  id: "f8eab6cf-98bc-4c5f-a9a2-8ecdd07533d0",
  name: "Image filter",
  mimeTypes: ["IMAGE/PNG", "IMAGE/JPEG"]
};

export const withFilterProvided = () => (
  <MimeTypeFilterEditingDialog
    open={boolean("open", false)}
    onClose={action("close the dialog")}
    mimeTypeFilter={mimeTypeFilter}
    addOrUpdate={action("Add or update a filter")}
    handleError={action("handle errors")}
  />
);

export const withFilterNotProvided = () => (
  <MimeTypeFilterEditingDialog
    open={boolean("open", false)}
    onClose={action("close the dialog")}
    addOrUpdate={action("Add or update a filter")}
    handleError={action("handle errors")}
  />
);
