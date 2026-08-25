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
import { decode } from "js-base64";
import { matchPath } from "react-router";
import { routes } from "../../../tsrc/mainui/routes";

const { path, to } = routes.EditLti13Platform;

/**
 * Pulls the encoded platform ID out of a path the same way the app does.
 */
const encodedIdFromRoute = (platformId: string): string | undefined =>
  matchPath<{ platformIdBase64: string }>(to(platformId), { path })?.params
    .platformIdBase64;

describe("routes.EditLti13Platform.to", () => {
  // A platform ID is supplied by the user, so plain `btoa`/`atob` is not enough to carry it in a
  // URL: it neither handles characters outside Latin-1 nor produces a URL-safe result. The cases
  // below make sure the route can handle each kind of ID a user may provide.
  it.each([
    ["a plain ASCII ID", "http://localhost:8200"],
    // Validates URL-safe encoding: standard base64 of "https://lms.edu/a?x" contains "/"
    ["an ID whose standard base64 contains a slash", "https://lms.edu/a?x"],
    // Validates Unicode handling beyond Latin-1 character set
    ["an ID containing non-Latin-1 characters", "https://测试.example"],
    // Edge case: Latin-1 accented characters (still in Latin-1 range but not ASCII)
    ["an ID with Latin-1 accented characters", "https://lms.édu.fr"],
  ])("round-trips %s through the route parameter", (_, platformId) => {
    const encoded = encodedIdFromRoute(platformId);

    expect(encoded).toBeDefined();
    expect(decode(encoded!)).toEqual(platformId);
  });
});
