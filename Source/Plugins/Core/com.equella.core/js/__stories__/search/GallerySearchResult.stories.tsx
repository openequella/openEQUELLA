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
import { range } from "lodash";
import * as React from "react";
import { v4 as uuidV4 } from "uuid";
import {
  GalleryEntry,
  GallerySearchResultItem,
} from "../../tsrc/modules/GallerySearchModule";
import GallerySearchResult, {
  GallerySearchResultProps,
} from "../../tsrc/search/components/GallerySearchResult";

export default {
  title: "Search/GallerySearchResult",
  component: GallerySearchResult,
} as Meta<GallerySearchResultProps>;

const buildGalleryEntry = (name: string): GalleryEntry => ({
  mimeType: "image/png",
  name,
  imagePathSmall: "./placeholder-135x135.png",
  imagePathMedium: "./placeholder-500x500.png",
  imagePathFull: "./placeholder-500x500.png",
});

const buildItems = (howMany: number): GallerySearchResultItem[] =>
  range(howMany).map(
    (i: number): GallerySearchResultItem => ({
      uuid: uuidV4(),
      version: 1,
      name: `Test item ${i}`,
      links: {
        self: `link-to-self-${i}`,
        view: `link-to-view-${i}`,
      },
      mainEntry: buildGalleryEntry(`Main Entry #${i}`),
      additionalEntries: range([0, 1, 2, 3, 5][i % 5]).map((_, idx) =>
        buildGalleryEntry(`Additional Entry #${i}/${idx}`)
      ),
    })
  );

export const TenItems: Story<GallerySearchResultProps> = (args) => (
  <GallerySearchResult {...args} />
);
TenItems.args = {
  items: buildItems(10),
};
