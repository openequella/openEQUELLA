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
import { getMimeTypesFromServer } from "../../__mocks__/MimeTypes.mock";
import MimeTypeFilterEditingDialog, {
  MimeTypeFilterEditingDialogProps,
} from "../../tsrc/settings/Search/searchfilter/MimeTypeFilterEditingDialog";
import type { MimeTypeFilter } from "../../tsrc/modules/SearchFilterSettingsModule";

export default {
  title: "MimeTypeFilterDialog",
  component: MimeTypeFilterEditingDialog,
  argTypes: {
    addOrUpdate: { action: "addOrUpdate" },
    onClose: { action: "onClose" },
    handleError: { action: "handleError" },
  },
} as Meta<MimeTypeFilterEditingDialogProps>;

const mimeTypeFilter: MimeTypeFilter = {
  id: "f8eab6cf-98bc-4c5f-a9a2-8ecdd07533d0",
  name: "Image filter",
  mimeTypes: ["image/png", "image/jpeg"],
};

export const withFilterNotProvided: Story<MimeTypeFilterEditingDialogProps> = (
  args
) => <MimeTypeFilterEditingDialog {...args} />;
withFilterNotProvided.args = {
  open: true,
  mimeTypeSupplier: () => Promise.resolve(getMimeTypesFromServer),
};

export const withFilterProvided: Story<MimeTypeFilterEditingDialogProps> = (
  args
) => <MimeTypeFilterEditingDialog {...args} />;
withFilterProvided.args = {
  ...withFilterNotProvided.args,
  mimeTypeFilter,
};
