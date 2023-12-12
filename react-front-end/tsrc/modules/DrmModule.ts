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
import { API_BASE_URL } from "../AppConfig";

export const defaultDrmStatus: OEQ.Search.DrmStatus = {
  termsAccepted: true,
  isAuthorised: true,
  isAllowSummary: true,
};

/**
 * Retrieve an Item's DRM terms.
 *
 * @param uuid UUID of the Item.
 * @param version Version of the Item.
 */
export const listDrmTerms = (
  uuid: string,
  version: number,
): Promise<OEQ.Drm.ItemDrmDetails> =>
  OEQ.Drm.listDrmTerms(API_BASE_URL, uuid, version);

/**
 * Accept an Item's DRM terms
 *
 * @param uuid UUID of the Item.
 * @param version Version of the Item.
 */
export const acceptDrmTerms = (uuid: string, version: number): Promise<void> =>
  OEQ.Drm.acceptDrmTerms(API_BASE_URL, uuid, version);

/**
 * List an Item's DRM violations. Due to the limitation on server side, this function returns only one
 * violation for each call.
 *
 * @param uuid UUID of the Item.
 * @param version Version of the Item.
 */
export const listDrmViolations = (
  uuid: string,
  version: number,
): Promise<OEQ.Drm.DrmViolation> =>
  OEQ.Drm.listDrmViolations(API_BASE_URL, uuid, version);
