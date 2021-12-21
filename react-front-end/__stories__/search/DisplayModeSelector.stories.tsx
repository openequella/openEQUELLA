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
import { Meta, Story } from "@storybook/react";
import * as React from "react";
import DisplayModeSelector, {
  DisplayModeSelectorProps,
} from "../../tsrc/search/components/DisplayModeSelector";

export default {
  title: "Search/DisplayModeSelector",
  component: DisplayModeSelector,
  argTypes: {
    onChange: { action: "onChange" },
  },
} as Meta<DisplayModeSelectorProps>;

export const ListSelection: Story<DisplayModeSelectorProps> = (args) => (
  <DisplayModeSelector {...args} />
);
ListSelection.args = {
  value: "list",
  disableVideoMode: false,
  disableImageMode: false,
};

export const ImageGallerySelection: Story<DisplayModeSelectorProps> = (
  args
) => <DisplayModeSelector {...args} />;
ImageGallerySelection.args = { ...ListSelection.args, value: "gallery-image" };

export const ImageGalleryDisabled: Story<DisplayModeSelectorProps> = (args) => (
  <DisplayModeSelector {...args} />
);
ImageGalleryDisabled.args = {
  ...ImageGallerySelection.args,
  disableImageMode: true,
  value: "list",
};

export const VideoGallerySelection: Story<DisplayModeSelectorProps> = (
  args
) => <DisplayModeSelector {...args} />;
VideoGallerySelection.args = { ...ListSelection.args, value: "gallery-video" };

export const VideoGalleryDisabled: Story<DisplayModeSelectorProps> = (args) => (
  <DisplayModeSelector {...args} />
);
VideoGalleryDisabled.args = {
  ...VideoGallerySelection.args,
  disableVideoMode: true,
  value: "list",
};

export const StandardListOnly: Story<DisplayModeSelectorProps> = (args) => (
  <DisplayModeSelector {...args} />
);
StandardListOnly.args = {
  ...ListSelection.args,
  disableVideoMode: true,
  disableImageMode: true,
};
