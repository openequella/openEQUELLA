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

export const drmTerms: OEQ.Drm.ItemDrmDetails = {
  title: "Terms of use",
  subtitle: "Licence agreement",
  description:
    "The following rights agreement specifies the rights granted to you the end users of this item. The terms and conditions are as follows:",
  agreements: {
    regularPermission:
      "This item may freely be displayed, executed, played and/or printed",
    additionalPermission:
      "Additionally, you as the user may modify, remove excerpts from, annotate and/or aggregate this item",
    educationSector:
      "Use of this item is strictly restricted to the educational sector",
    parties: {
      title:
        "On reuse of this item you must attribute ownership to the original owners listed below",
      partyList: ["Automated auto@test.com"],
    },
    customTerms: {
      title: "Users must agree to the following terms and conditions",
      terms: "this is the first DRM term\nthis is the second DRM term",
    },
  },
};

const withDelay = async () => {
  await new Promise((resolve) => setTimeout(resolve, 500));
};

// Mock function for listing DRM terms after 500 ms.
export const drmTermsResolved = async () => {
  await withDelay();
  return Promise.resolve(drmTerms);
};

// Mock function for failing to retrieve DRM terms after 500 ms.
export const drmTermsRejected = async () => {
  await withDelay();
  return Promise.reject("network error");
};

export const DRM_VIOLATION =
  "User is viewing this item outside of the dates that it is restricted to.";
