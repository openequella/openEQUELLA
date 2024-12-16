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
import { Meta, StoryFn } from "@storybook/react";
import * as React from "react";
import HierarchyKeyResourceDialog, {
  HierarchyKeyResourceDialogProps,
} from "../../tsrc/hierarchy/components/HierarchyKeyResourceDialog";

export default {
  title: "Hierarchy/HierarchyKeyResourceDialog",
  component: HierarchyKeyResourceDialog,
  argTypes: {
    closeDialog: { action: "on close dialog" },
    updateKeyResource: {
      action: "on click confirm",
    },
  },
} as Meta<HierarchyKeyResourceDialogProps>;

const commonProps = {
  open: true,
  isKeyResource: false,
  isLatestVersion: false,
  isAlwaysLatest: true,
};

export const AddKeyResourceItemOnOlderVersion: StoryFn<
  HierarchyKeyResourceDialogProps
> = (args) => <HierarchyKeyResourceDialog {...args} />;

AddKeyResourceItemOnOlderVersion.args = { ...commonProps };

export const AddKeyResourceItemOnLatestVersion: StoryFn<
  HierarchyKeyResourceDialogProps
> = (args) => <HierarchyKeyResourceDialog {...args} />;

AddKeyResourceItemOnLatestVersion.args = {
  ...commonProps,
  isLatestVersion: true,
};

export const RemoveKeyResource: StoryFn<HierarchyKeyResourceDialogProps> = (
  args,
) => <HierarchyKeyResourceDialog {...args} />;

RemoveKeyResource.args = {
  ...commonProps,
  isKeyResource: true,
};
