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
import { validateUsernameClaim } from "../../../../../../tsrc/settings/Integrations/lti13/components/LtiUsernameClaimParser";

describe("validateUsernameClaim", () => {
  const FIRST_PATH = "https://lti13.org";
  const SECOND_PATH = "username";
  const THIRD_PATH = "ID";

  it("returns `true` if the username claim is constructed in the correct format", () => {
    const claim = `[${FIRST_PATH}][${SECOND_PATH}][${THIRD_PATH}]`;
    const result = validateUsernameClaim(claim);

    expect(result).toBe(true);
  });

  it("returns `false` if the username claim is constructed in the incorrect format", () => {
    const badClaims = [
      `[[${FIRST_PATH}][${SECOND_PATH}][${THIRD_PATH}]]`,
      `[${FIRST_PATH}]${SECOND_PATH}][${THIRD_PATH}]`,
      `[${FIRST_PATH}[${SECOND_PATH}][${THIRD_PATH}]`,
      `[${FIRST_PATH}]${SECOND_PATH}[${THIRD_PATH}]`,
      `[${FIRST_PATH}]]]]`,
      `[[[[${FIRST_PATH}]`,
      `[${FIRST_PATH}`,
      `${FIRST_PATH}]`,
      "[]",
      "",
    ];

    const result = badClaims
      .map(validateUsernameClaim)
      .every((r) => r === false);

    // Every validation should return false.
    expect(result).toBe(true);
  });
});
