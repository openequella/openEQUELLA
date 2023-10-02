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
import type { Meta, StoryFn } from "@storybook/react";
import SimpleConfirmDialog, {
  SimpleConfirmDialogProps,
} from "../../tsrc/components/SimpleConfirmDialog";

export default {
  title: "Component/SimpleConfirmDialog",
  component: SimpleConfirmDialog,
  argTypes: {
    onCancel: { action: "onCancel" },
    onConfirm: { action: "onConfirm" },
  },
} as Meta<SimpleConfirmDialogProps>;

export const ShowDialog: StoryFn<SimpleConfirmDialogProps> = (args) => (
  <SimpleConfirmDialog {...args} />
);
ShowDialog.args = {
  open: true,
  title: "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
};
