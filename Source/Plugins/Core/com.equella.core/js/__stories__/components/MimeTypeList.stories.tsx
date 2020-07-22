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
import { action } from "@storybook/addon-actions";
import { array, object } from "@storybook/addon-knobs";
import MimeTypeList from "../../tsrc/settings/Search/searchfilter/MimeTypeList";
import { MimeTypeEntry } from "../../tsrc/modules/SearchFilterSettingsModule";

export default {
  title: "MimeTypeList",
  component: MimeTypeList,
};

const defaultMimeTypes: MimeTypeEntry[] = [
  { mimeType: "image/png", desc: "This is a Image filter" },
  { mimeType: "image/jpeg", desc: "This is a Image filter" },
];

export const listOfMimeTypes = () => (
  <MimeTypeList
    entries={object("mimetypes", defaultMimeTypes)}
    onChange={action("values of checkboxes changed")}
    selected={array("selected MIME types", ["image/png", "image/jpeg"])}
  />
);
