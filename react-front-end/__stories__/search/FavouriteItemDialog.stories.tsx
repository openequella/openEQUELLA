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
import FavouriteItemDialog, {
  FavouriteItemDialogProps,
} from "../../tsrc/search/components//FavouriteItemDialog";

export default {
  title: "Search/FavouriteItemDialog",
  component: FavouriteItemDialog,
  argTypes: {
    closeDialog: { action: "on close dialog" },
    updateFavouriteItem: {
      action: "on click confirm",
    },
  },
} as Meta<FavouriteItemDialogProps>;

const commonProps = {
  open: true,
  isAdded: false,
  isOnLatestVersion: false,
};

export const AddFavouriteItemOnOlderVersion: StoryFn<
  FavouriteItemDialogProps
> = (args) => <FavouriteItemDialog {...args} />;

AddFavouriteItemOnOlderVersion.args = { ...commonProps };

export const AddFavouriteItemOnLatestVersion: StoryFn<
  FavouriteItemDialogProps
> = (args) => <FavouriteItemDialog {...args} />;

AddFavouriteItemOnLatestVersion.args = {
  ...commonProps,
  isLatestVersion: true,
};

export const RemoveFavouriteItem: StoryFn<FavouriteItemDialogProps> = (
  args,
) => <FavouriteItemDialog {...args} />;

RemoveFavouriteItem.args = {
  ...commonProps,
  isAdded: true,
};
