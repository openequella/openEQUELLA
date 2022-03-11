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
import * as OEQ from "@openequella/rest-api-client";

export const fileDetails: OEQ.Search.ThumbnailDetails = {
  attachmentType: "file",
  mimeType: "image/png",
  link: "./thumb.jpg",
};

export const brokenFileDetails: OEQ.Search.ThumbnailDetails = {
  attachmentType: "file",
  mimeType: "image/png",
};

export const resourceFileDetails: OEQ.Search.ThumbnailDetails = {
  attachmentType: "custom/resource",
  mimeType: "image/jpeg",
  link: "./thumb.jpg",
};

export const linkDetails: OEQ.Search.ThumbnailDetails = {
  attachmentType: "link",
};

export const resourceLinkDetails: OEQ.Search.ThumbnailDetails = {
  attachmentType: "custom/resource",
  mimeType: "equella/link",
};

export const equellaItemDetails: OEQ.Search.ThumbnailDetails = {
  attachmentType: "custom/resource",
  mimeType: "equella/item",
};

export const htmlDetails: OEQ.Search.ThumbnailDetails = {
  attachmentType: "html",
  mimeType: "text/html",
};

export const resourceHtmlDetails: OEQ.Search.ThumbnailDetails = {
  attachmentType: "custom/resource",
  mimeType: "text/html",
};
