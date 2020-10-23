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
import Lightbox, { LightboxProps } from "../../tsrc/components/Lightbox";

export default {
  title: "component/Lightbox",
  component: Lightbox,
  argTypes: {
    onClose: { action: "onClose triggered" },
  },
} as Meta<LightboxProps>;

export const displayImage: Story<LightboxProps> = (args: LightboxProps) => (
  <Lightbox {...args} />
);
displayImage.args = {
  mimeType: "image/png",
  open: true,
  src: "https://avatars2.githubusercontent.com/u/54074368",
  title: "openEQUELLA GitHub avatar",
};

export const displayAudio: Story<LightboxProps> = (args: LightboxProps) => (
  <Lightbox {...args} />
);
displayAudio.args = {
  mimeType: "audio/ogg",
  open: true,
  src:
    "https://archive.org/download/Sleep_Music-5629/junior85_-_01_-_Birdsong.ogg",
  title: "Sleep Music (Tony Higgins) - Birdsong",
};

export const displayVideo: Story<LightboxProps> = (args: LightboxProps) => (
  <Lightbox {...args} />
);
displayVideo.args = {
  mimeType: "video/ogg",
  open: true,
  src: "https://archive.org/download/JoanAvoi1947/JoanAvoi1947.ogv",
  title: "Joan Avoids a Cold (Coronet Instructional Films)",
};

export const unsupportedContent: Story<LightboxProps> = (
  args: LightboxProps
) => <Lightbox {...args} />;
unsupportedContent.args = {
  mimeType: "blah/blah",
  open: true,
  src: "not-relevant",
  title: "A Title",
};
