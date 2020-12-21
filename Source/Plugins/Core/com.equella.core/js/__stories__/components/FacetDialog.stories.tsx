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
import type { Meta, Story } from "@storybook/react";
import FacetDialog, {
  FacetDialogProps,
} from "../../tsrc/settings/Search/facetedsearch/FacetDialog";
import type { FacetWithFlags } from "../../tsrc/modules/FacetedSearchSettingsModule";

export default {
  title: "FacetDialog",
  component: FacetDialog,
  argTypes: {
    onClose: { action: "onClose" },
    addOrEdit: { action: "addOrEdit" },
    handleError: { action: "handleError" },
  },
} as Meta<FacetDialogProps>;
const facet: FacetWithFlags = {
  name: "mocked facet",
  schemaNode: "item/name",
  maxResults: 1,
  orderIndex: 1,
  updated: false,
  deleted: false,
};

export const withFacetProvided: Story<FacetDialogProps> = (args) => (
  <FacetDialog {...args} />
);
withFacetProvided.args = {
  open: true,
  facet,
};

export const withFacetNotProvided: Story<FacetDialogProps> = (args) => (
  <FacetDialog {...args} />
);
withFacetNotProvided.args = { open: true };
