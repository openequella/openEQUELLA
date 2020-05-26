/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import * as React from "react";
import { action } from "@storybook/addon-actions";
import { boolean, object } from "@storybook/addon-knobs";
import MimeTypeFilterEditingDialog from "../../tsrc/settings/Search/searchfilter/MimeTypeFilterEditingDialog";
import { MimeTypeFilter } from "../../tsrc/settings/Search/searchfilter/SearchFilterSettingsModule";

export default {
  title: "MimeTypeFilterDialog",
  component: MimeTypeFilterEditingDialog,
};
export const mimeTypeFilter: MimeTypeFilter = {
  id: "f8eab6cf-98bc-4c5f-a9a2-8ecdd07533d0",
  name: "Image filter",
  mimeTypes: ["image/png", "image/jpeg"],
};

export const withFilterProvided = () => (
  <MimeTypeFilterEditingDialog
    open={boolean("open", true)}
    onClose={action("close the dialog")}
    mimeTypeFilter={object("filter", mimeTypeFilter)}
    addOrUpdate={action("Add or update a filter")}
    handleError={action("handle errors")}
  />
);

export const withFilterNotProvided = () => (
  <MimeTypeFilterEditingDialog
    open={boolean("open", true)}
    onClose={action("close the dialog")}
    addOrUpdate={action("Add or update a filter")}
    handleError={action("handle errors")}
  />
);
