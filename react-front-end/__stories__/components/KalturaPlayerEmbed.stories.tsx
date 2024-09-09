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
import {
  KalturaPlayerEmbed,
  KalturaPlayerEmbedProps,
} from "../../tsrc/components/KalturaPlayerEmbed";

export default {
  title: "component/KalturaEmbed",
  component: KalturaPlayerEmbed,
} as Meta<KalturaPlayerEmbedProps>;

export const EmbeddedKalturaVideoPlayerV2: StoryFn<KalturaPlayerEmbedProps> = (
  args,
) => <KalturaPlayerEmbed {...args} />;
EmbeddedKalturaVideoPlayerV2.args = {
  // These video details were figured out from the publicly accessible demo video at:
  // http://player.kaltura.com/modules/KalturaSupport/tests/AutoEmbed.html
  partnerId: 243342,
  uiconfId: 21099702,
  entryId: "1_sf5ovm7u",
  dimensions: {
    width: 400,
    height: 333,
  },
  version: "V2",
};

export const EmbeddedKalturaVideoPlayerV7: StoryFn<KalturaPlayerEmbedProps> = (
  args,
) => <KalturaPlayerEmbed {...args} />;
EmbeddedKalturaVideoPlayerV7.args = {
  // These video details were figured out from the publicly accessible demo video at:
  // https://knowledge.kaltura.com/help/player-embed
  partnerId: 4834032,
  uiconfId: 50952692,
  entryId: "1_fwzaeesq",
  dimensions: {
    width: 560,
    height: 395,
  },
  version: "V7",
};

export const EmbeddedKalturaVideoPlayerLarge: StoryFn<
  KalturaPlayerEmbedProps
> = (args) => <KalturaPlayerEmbed {...args} />;
EmbeddedKalturaVideoPlayerLarge.args = {
  ...EmbeddedKalturaVideoPlayerV7.args,
  dimensions: {
    width: 1120,
    height: 630,
  },
};
