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
import type { Meta, StoryFn } from "@storybook/react-vite";
import { dialogDocsParameters } from "../storyUtils";
import SelectItemVersionDialog, {
  SelectItemVersionDialogProps,
} from "../../tsrc/components/SelectItemVersionDialog";

export default {
  title: "Component/SelectItemVersionDialog",
  component: SelectItemVersionDialog,
  parameters: dialogDocsParameters,
  argTypes: {
    closeDialog: { action: "on close dialog" },
    onConfirm: {
      action: "on click confirm",
    },
  },
} as Meta<SelectItemVersionDialogProps>;

const commonProps = {
  title: "This is title",
  open: true,
  isAdded: false,
  isOnLatestVersion: false,
};

export const AddItemOnOlderVersion: StoryFn<SelectItemVersionDialogProps> = (
  args,
) => <SelectItemVersionDialog {...args} />;

AddItemOnOlderVersion.args = { ...commonProps };

export const AddItemOnLatestVersion: StoryFn<SelectItemVersionDialogProps> = (
  args,
) => <SelectItemVersionDialog {...args} />;

AddItemOnLatestVersion.args = {
  ...commonProps,
  isLatestVersion: true,
};

export const AddItemWithTag: StoryFn<SelectItemVersionDialogProps> = (args) => (
  <SelectItemVersionDialog {...args} />
);

AddItemWithTag.args = {
  ...commonProps,
  tagDescription: "This is tag description",
  isLatestVersion: true,
};
