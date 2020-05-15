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
import FacetDialog from "../../tsrc/settings/Search/facetedsearch/FacetDialog";
import { FacetWithFlags } from "../../tsrc/settings/Search/facetedsearch/FacetedSearchSettingsModule";

export default {
  title: "FacetDialog",
  component: FacetDialog,
};
const facet: FacetWithFlags = {
  name: "mocked facet",
  schemaNode: "item/name",
  maxResults: 1,
  orderIndex: 1,
  updated: false,
  deleted: false,
};

export const withFacetProvided = () => (
  <FacetDialog
    open={boolean("open", true)}
    onClose={action("close the dialog")}
    facet={object("facet", facet)}
    addOrEdit={action("Add or update a facet")}
    handleError={action("handle errors")}
  />
);

export const withFacetNotProvided = () => (
  <FacetDialog
    open={boolean("open", true)}
    onClose={action("close the dialog")}
    addOrEdit={action("Add or update a facet")}
    handleError={action("handle errors")}
  />
);
