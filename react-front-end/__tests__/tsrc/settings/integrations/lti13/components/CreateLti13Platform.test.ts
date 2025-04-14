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
import type { RenderResult } from "@testing-library/react";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as SET from "fp-ts/Set";
import { groups } from "../../../../../../__mocks__/GroupModule.mock";
import { roles } from "../../../../../../__mocks__/RoleModule.mock";
import { users } from "../../../../../../__mocks__/UserModule.mock";
import { languageStrings } from "../../../../../../tsrc/util/langstrings";
import { mockRoleAndGroupApis } from "../../../../components/securityentitydialog/SelectEntityDialogTestHelper";
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
    usernameClaim: usernameClaimLabel,
    usernamePrefix: usernamePrefixLabel,
    usernameSuffix: usernameSuffixLabel,
  },
  accessControl: { unknownUserHandlingCreate: unknownUserHandlingCreateLabel },
} = languageStrings.settings.integration.lti13PlatformsSettings.createPage;

mockRoleAndGroupApis();

describe("CreateLti13Platform", () => {
  const errorOutlineClass = "Mui-error";

  const usableByUserId = users[0].id;
  const usableByUser = users[0].username;
  const unknownUserDefaultGroupId = groups[0].id;
  const unknownUserDefaultGroup = groups[0].name;
  const defaultRoleId = roles[0].id;
  const defaultRole = roles[0].name;

  const defaultPlatform: OEQ.LtiPlatform.LtiPlatform = {
    platformId: "www.test.com",
    name: "test",
    clientId: "client name",
    keysetUrl: "http://www.platformKeyset.com",
    authUrl: "https://www.test.com",
    usernameClaim: "",
    usernamePrefix: "prefix",
    usernameSuffix: "suffix",
    unknownUserHandling: "ERROR",
    allowExpression: "*",
    instructorRoles: SET.empty,
    unknownRoles: SET.empty,
    customRoles: new Map(),
    unknownUserDefaultGroups: SET.empty,
    enabled: true,
  };

  const usableBy = async (renderResult: RenderResult) =>
    await configureUsableBy(renderResult, usableByUser);

  const unknownUserHandling = async (renderResult: RenderResult) =>
    await configureUnknownUserHandling(
      renderResult,
      unknownUserHandlingCreateLabel,
      unknownUserDefaultGroup,
    );

  const instructorRoles = async (renderResult: RenderResult) =>
    await configureInstructorRoles(renderResult, defaultRole);

  const customRoles = async (renderResult: RenderResult) =>
    await configureCustomRoles(
      renderResult,
      ltiRoles.institution.Guest,
      defaultRole,
    );

  const unknownRoles = async (renderResult: RenderResult) =>
    await configureUnknownRoles(renderResult, defaultRole);

  it.each<
    [
      string,
      (renderResult: RenderResult) => Promise<void>,
      OEQ.LtiPlatform.LtiPlatform,
    ]
  >([
    [
      "usable by",
      usableBy,
      { ...defaultPlatform, allowExpression: `* U:${usableByUserId} OR` },
    ],
    [
      "unknown user handling",
      unknownUserHandling,
      {
        ...defaultPlatform,
        unknownUserHandling: "CREATE",
        unknownUserDefaultGroups: SET.singleton(unknownUserDefaultGroupId),
      },
    ],
    [
      "instructor roles",
      instructorRoles,
      {
        ...defaultPlatform,
        instructorRoles: SET.singleton(defaultRoleId),
      },
    ],
    [
      "custom roles",
      customRoles,
      {
        ...defaultPlatform,
        customRoles: new Map([
          [
            "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Guest",
            SET.singleton(defaultRoleId),
          ],
        ]),
      },
    ],
    [
      "unknown roles",
      unknownRoles,
      { ...defaultPlatform, unknownRoles: SET.singleton(defaultRoleId) },
    ],
  ])(
    "supports the configuration of %s when creating a new platform",
    async (_: string, configuration, expectedResult) => {
      const createPlatform = jest.fn();
      const renderResult = await renderCreateLti13Platform({
        ...commonCreateLti13PlatformProps,
        createPlatformProvider: createPlatform,
      });
      const { container } = renderResult;

      // General details are always required.
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

      await configuration(renderResult);
      await savePlatform(container);

      const result = createPlatform.mock.lastCall[0];
      expect(result).toEqual(expectedResult);
    },
    20000,
  );

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

  it("highlights any field where the value is invalid", async () => {
    const controls = new Map([
      [platformKeysetURLLabel, "www.platformKeyset.com"],
      [platformAuthenticationRequestURLLabel, "httpstest://www.test.com"],
      [usernameClaimLabel, "[[[username]"],
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
