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
import { number, object, text } from "@storybook/addon-knobs";

export default {
  title: "SerachResult",
  component: SearchResult,
};

export const BasicSearchResultComponent = () => (
  <SearchResult
    name={text("name", mockData.basicSearchObj.name!)}
    version={number("version", mockData.basicSearchObj.version)}
    uuid={text("uuid", mockData.basicSearchObj.uuid)}
    description={text("description", mockData.basicSearchObj.description!)}
    displayFields={object("display fields", [
      ...mockData.basicSearchObj.displayFields,
    ])}
    modifiedDate={object("modified date", mockData.basicSearchObj.modifiedDate)}
    createdDate={object("created date", mockData.basicSearchObj.createdDate)}
    status={text("item status", mockData.basicSearchObj.status)}
    displayOptions={object(
      "display options",
      mockData.basicSearchObj.displayOptions
    )}
    attachments={object("attachments", [
      ...mockData.basicSearchObj.attachments,
    ])}
    links={object("links", mockData.basicSearchObj.links)}
    collectionId={text("collection ID", mockData.basicSearchObj.collectionId)}
    commentCount={number("comment count", mockData.basicSearchObj.commentCount)}
    thumbnail={text("thumbnail", mockData.basicSearchObj.thumbnail)}
  />
);

export const AttachmentSearchResultComponent = () => (
  <SearchResult
    name={text("name", mockData.attachSearchObj.name!)}
    version={number("version", mockData.attachSearchObj.version)}
    uuid={text("uuid", mockData.attachSearchObj.uuid)}
    description={text("description", mockData.attachSearchObj.description!)}
    displayFields={object("display fields", [
      ...mockData.attachSearchObj.displayFields,
    ])}
    modifiedDate={object(
      "modified date",
      mockData.attachSearchObj.modifiedDate
    )}
    createdDate={object("created date", mockData.attachSearchObj.createdDate)}
    status={text("item status", mockData.attachSearchObj.status)}
    displayOptions={object(
      "display options",
      mockData.attachSearchObj.displayOptions
    )}
    attachments={object("attachments", [
      ...mockData.attachSearchObj.attachments,
    ])}
    links={object("links", mockData.attachSearchObj.links)}
    collectionId={text("collection ID", mockData.attachSearchObj.collectionId)}
    commentCount={number(
      "comment count",
      mockData.attachSearchObj.commentCount
    )}
    thumbnail={text("thumbnail", mockData.attachSearchObj.thumbnail)}
  />
);

export const CustomMetadataSearchResultComponent = () => (
  <SearchResult
    name={text("name", mockData.customMetaSearchObj.name!)}
    version={number("version", mockData.customMetaSearchObj.version)}
    uuid={text("uuid", mockData.customMetaSearchObj.uuid)}
    description={text("description", mockData.customMetaSearchObj.description!)}
    displayFields={object("display fields", [
      ...mockData.customMetaSearchObj.displayFields,
    ])}
    modifiedDate={object(
      "modified date",
      mockData.customMetaSearchObj.modifiedDate
    )}
    createdDate={object(
      "created date",
      mockData.customMetaSearchObj.createdDate
    )}
    status={text("item status", mockData.customMetaSearchObj.status)}
    displayOptions={object(
      "display options",
      mockData.customMetaSearchObj.displayOptions
    )}
    attachments={object("attachments", [
      ...mockData.customMetaSearchObj.attachments,
    ])}
    links={object("links", mockData.customMetaSearchObj.links)}
    collectionId={text(
      "collection ID",
      mockData.customMetaSearchObj.collectionId
    )}
    commentCount={number(
      "comment count",
      mockData.customMetaSearchObj.commentCount
    )}
    thumbnail={text("thumbnail", mockData.customMetaSearchObj.thumbnail)}
  />
);
