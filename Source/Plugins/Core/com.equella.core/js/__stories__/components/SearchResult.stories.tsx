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
import * as mockData from "../../__mocks__/searchresult_mock_data";
import SearchResult from "../../tsrc/search/components/SearchResult";
import { action } from "@storybook/addon-actions";
import { object } from "@storybook/addon-knobs";

export default {
  title: "SerachResult",
  component: SearchResult,
};

export const BasicSearchResultComponent = () => (
  <SearchResult
    resultData={object("resultData", mockData.basicSearchObj)}
    onClick={action("onClick")}
  />
);

export const AttachmentSearchResultComponent = () => (
  <SearchResult
    resultData={object("resultData", mockData.attachSearchObj)}
    onClick={action("onClick")}
  />
);

export const CustomMetadataSearchResultComponent = () => (
  <SearchResult
    resultData={object("resultData", mockData.customMetaSearchObj)}
    onClick={action("onClick")}
  />
);
