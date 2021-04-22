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
import Lightbox, {
  LightboxConfig,
  LightboxProps,
} from "../../tsrc/components/Lightbox";
import { CustomMimeTypes } from "../../tsrc/modules/MimeTypesModule";

export default {
  title: "component/Lightbox",
  component: Lightbox,
  argTypes: {
    onClose: { action: "onClose triggered" },
  },
} as Meta<LightboxProps>;

const imageConfig: LightboxConfig = {
  src: "https://avatars2.githubusercontent.com/u/54074368",
  title: "openEQUELLA GitHub avatar",
  mimeType: "image/png",
};

const videoConfig: LightboxConfig = {
  src: "https://archive.org/download/JoanAvoi1947/JoanAvoi1947.ogv",
  title: "Joan Avoids a Cold (Coronet Instructional Films)",
  mimeType: "video/ogg",
};

const audioConfig: LightboxConfig = {
  src:
    "https://archive.org/download/Sleep_Music-5629/junior85_-_01_-_Birdsong.ogg",
  title: "Sleep Music (Tony Higgins) - Birdsong",
  mimeType: "audio/ogg",
};

const youTubeConfig: LightboxConfig = {
  src: "https://www.youtube.com/watch?v=x0SgN92HP_k",
  title: "1788-L - N U / V E R / K A",
  mimeType: CustomMimeTypes.YOUTUBE,
};
export const displayImage: Story<LightboxProps> = (args: LightboxProps) => (
  <Lightbox {...args} />
);
displayImage.args = {
  open: true,
  config: imageConfig,
};

export const displayAudio: Story<LightboxProps> = (args: LightboxProps) => (
  <Lightbox {...args} />
);
displayAudio.args = {
  open: true,
  config: audioConfig,
};

export const displayVideo: Story<LightboxProps> = (args: LightboxProps) => (
  <Lightbox {...args} />
);
displayVideo.args = {
  open: true,
  config: videoConfig,
};

export const displayYouTube: Story<LightboxProps> = (args: LightboxProps) => (
  <Lightbox {...args} />
);
displayYouTube.args = {
  open: true,
  config: youTubeConfig,
};

export const unsupportedContent: Story<LightboxProps> = (
  args: LightboxProps
) => <Lightbox {...args} />;
unsupportedContent.args = {
  open: true,
  config: {
    src: "not-relevant",
    title: "A Title",
    mimeType: "blah/blah",
  },
};

export const NavigateAttachments: Story<LightboxProps> = (
  args: LightboxProps
) => <Lightbox {...args} />;
NavigateAttachments.args = {
  ...displayImage.args,
  config: {
    ...imageConfig,
    onNext: () => audioConfig,
    onPrevious: () => videoConfig,
  },
};
