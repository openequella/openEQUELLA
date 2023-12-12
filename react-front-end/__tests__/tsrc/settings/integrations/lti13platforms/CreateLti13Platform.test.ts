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
import "@testing-library/jest-dom";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as SET from "fp-ts/Set";
import { groups } from "../../../../../__mocks__/GroupModule.mock";
import { roles } from "../../../../../__mocks__/RoleModule.mock";
import { users } from "../../../../../__mocks__/UserModule.mock";
import { languageStrings } from "../../../../../tsrc/util/langstrings";
import {
  commonCreateLti13PlatformProps,
  configureCustomRoles,
  configureGeneralDetails,
  configureInstructorRoles,
  configureUnknownRoles,
  configureUnknownUserHandling,
  configureUsableBy,
  getGeneralDetailsInputOutline,
  renderCreateLti13Platform,
  savePlatform,
} from "./CreateLti13PlatformTestHelper";

const {
  ltiRoles,
  generalDetails: {
    platformId: platformIdLabel,
    name: nameLabel,
    clientId: clientIdLabel,
    platformKeysetURL: platformKeysetURLLabel,
    platformAuthenticationRequestURL: platformAuthenticationRequestURLLabel,
    usernamePrefix: usernamePrefixLabel,
    usernameSuffix: usernameSuffixLabel,
  },
  accessControl: { unknownUserHandlingCreate: unknownUserHandlingCreateLabel },
} = languageStrings.settings.integration.lti13PlatformsSettings.createPage;

describe("CreateLti13Platform", () => {
  const errorOutlineClass = "Mui-error";

  it("creates a new platform when provided with valid platform details", async () => {
    const usableByUserId = users[0].id;
    const usableByUser = users[0].username;
    const unknownUserDefaultGroupId = groups[0].id;
    const unknownUserDefaultGroup = groups[0].name;
    const defaultRoleId = roles[0].id;
    const defaultRole = roles[0].name;
    const expectedResult: OEQ.LtiPlatform.LtiPlatform = {
      platformId: "www.test.com",
      name: "test",
      clientId: "client name",
      keysetUrl: "http://www.platformKeyset.com",
      authUrl: "https://www.test.com",
      usernamePrefix: "prefix",
      usernameSuffix: "suffix",
      unknownUserHandling: "CREATE",
      allowExpression: `* U:${usableByUserId} OR`,
      instructorRoles: SET.singleton(defaultRoleId),
      unknownRoles: SET.singleton(defaultRoleId),
      customRoles: new Map([
        [
          "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Guest",
          SET.singleton(defaultRoleId),
        ],
      ]),
      unknownUserDefaultGroups: SET.singleton(unknownUserDefaultGroupId),
      enabled: true,
    };
    const createPlatform = jest.fn();

    const renderResult = await renderCreateLti13Platform({
      ...commonCreateLti13PlatformProps,
      createPlatformProvider: createPlatform,
    });
    const { container } = renderResult;

    await configureGeneralDetails(
      container,
      new Map([
        [platformIdLabel, expectedResult.platformId],
        [nameLabel, expectedResult.name],
        [clientIdLabel, expectedResult.clientId],
        [platformKeysetURLLabel, expectedResult.keysetUrl],
        [platformAuthenticationRequestURLLabel, expectedResult.authUrl],
        [usernamePrefixLabel, expectedResult.usernamePrefix!],
        [usernameSuffixLabel, expectedResult.usernameSuffix!],
      ]),
    );
    await configureUsableBy(renderResult, usableByUser);
    await configureUnknownUserHandling(
      renderResult,
      unknownUserHandlingCreateLabel,
      unknownUserDefaultGroup,
    );
    await configureInstructorRoles(renderResult, defaultRole);
    await configureCustomRoles(
      renderResult,
      ltiRoles.institution.Guest,
      defaultRole,
    );
    await configureUnknownRoles(renderResult, defaultRole);

    await savePlatform(container);

    const result = createPlatform.mock.lastCall[0];
    expect(result).toEqual(expectedResult);
  }, 45000);

  it("highlights any required fields whose value is empty", async () => {
    const createPlatform = jest.fn();
    const { container } = await renderCreateLti13Platform({
      ...commonCreateLti13PlatformProps,
      createPlatformProvider: createPlatform,
    });

    await savePlatform(container);

    pipe(
      [
        platformIdLabel,
        nameLabel,
        clientIdLabel,
        platformKeysetURLLabel,
        platformAuthenticationRequestURLLabel,
      ],
      A.map((label) =>
        expect(getGeneralDetailsInputOutline(container, label)).toHaveClass(
          errorOutlineClass,
        ),
      ),
    );
    expect(createPlatform).not.toHaveBeenCalled();
  });

  it("highlights URL fields if URL value misses the protocol", async () => {
    const controls = new Map([
      [platformKeysetURLLabel, "www.platformKeyset.com"],
      [platformAuthenticationRequestURLLabel, "httpstest://www.test.com"],
    ]);
    const createPlatform = jest.fn();
    const { container } = await renderCreateLti13Platform({
      ...commonCreateLti13PlatformProps,
      createPlatformProvider: createPlatform,
    });

    await configureGeneralDetails(container, controls);

    await savePlatform(container);

    pipe(
      Array.from(controls.keys()),
      A.map((label) =>
        expect(getGeneralDetailsInputOutline(container, label)).toHaveClass(
          errorOutlineClass,
        ),
      ),
    );
    expect(createPlatform).not.toHaveBeenCalled();
  });
});
