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
import "@testing-library/jest-dom";
import { findByText } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import { platforms } from "../../../../../../__mocks__/Lti13PlatformsModule.mock";
import { generateWarnMsgForMissingIds } from "../../../../../../tsrc/settings/Integrations/lti13/components/EditLti13PlatformHelper";
import { languageStrings } from "../../../../../../tsrc/util/langstrings";
import { savePlatform } from "./CreateLti13PlatformTestHelper";
import {
  commonEditLti13PlatformProps,
  renderEditLti13Platform,
} from "./EditLti13PlatformTestHelper";

const {
  security: { rotateKeyPair: rotateKeyPairLabel },
} = languageStrings.settings.integration.lti13PlatformsSettings.editPage;
const { unknownUserHandlingCreate } =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage
    .accessControl;
const { ok: okLabel } = languageStrings.common.action;

describe("EditLti13Platform", () => {
  it("loads the existing platform by platformID from URL", async () => {
    const expectedPlatform = platforms[2];
    const updatePlatform = jest.fn();

    const renderResult = await renderEditLti13Platform(
      {
        ...commonEditLti13PlatformProps,
        updatePlatformProvider: updatePlatform,
      },
      // http://blackboard:8200
      "aHR0cDovL2JsYWNrYm9hcmQ6ODIwMA==",
    );
    const { container, findByDisplayValue, findByText } = renderResult;

    // check the UI if it displays the correct value for platform
    const generalDetailsChecks = pipe(
      [
        expectedPlatform.name,
        expectedPlatform.authUrl,
        expectedPlatform.clientId,
        expectedPlatform.keysetUrl,
        expectedPlatform.usernameClaim!,
        expectedPlatform.platformId,
      ],
      A.map(async (expectedValue) =>
        expect(await findByDisplayValue(expectedValue)).toBeInTheDocument(),
      ),
    );
    // other text which are not displayed in input, textarea, or select element
    const otherChecks = pipe(
      [
        //unknown user handling option,
        unknownUserHandlingCreate,
        // ACL Expression
        "Racheal Carlyle [user200]",
      ],
      A.map(async (expectedValue) =>
        expect(await findByText(expectedValue)).toBeInTheDocument(),
      ),
    );
    const uiChecks = [...generalDetailsChecks, ...otherChecks];
    await Promise.all(uiChecks);

    // click save button to see if it can still generate the correct result from initial platform
    await savePlatform(container);

    const result = updatePlatform.mock.lastCall[0];

    expect(result).toEqual(expectedPlatform);
  });

  it.each<[string, "role" | "group", Set<string>]>([
    [
      "UnknownUserDefaultGroups",
      "group",
      new Set(["deletedGroup1", "deletedGroup2"]),
    ],
    ["InstructorRoles", "role", new Set(["deletedRole1"])],
    ["UnknownRoles", "role", new Set(["deletedRole2"])],
  ])(
    "shows warning messages for %s if any %s has been deleted but still selected in platform",
    async (_, entityType, missingIds) => {
      const renderResult = await renderEditLti13Platform(
        commonEditLti13PlatformProps,
        // http://localhost:8100
        "aHR0cDovL2xvY2FsaG9zdDo4MTAw",
      );
      const { getByText } = renderResult;
      const expectedWarnMsg = generateWarnMsgForMissingIds(
        missingIds,
        entityType,
      );

      expect(getByText(expectedWarnMsg)).toBeInTheDocument();
    },
  );

  it("shows warning messages for CustomRoles if any role has been deleted but still selected in platform", async () => {
    const renderResult = await renderEditLti13Platform(
      commonEditLti13PlatformProps,
      // http://localhost:8100
      "aHR0cDovL2xvY2FsaG9zdDo4MTAw",
    );
    const { getByText } = renderResult;
    const expectedWarnMsg = `LTI role: http://purl.imsglobal.org/vocab/lis/v2/system/person#Administrator - ${generateWarnMsgForMissingIds(
      new Set(["deletedRole3"]),
      "role",
    )}`;

    expect(getByText(expectedWarnMsg)).toBeInTheDocument();
  });

  it("execute rotateKeyPair function if user confirm the rotate key pair action", async () => {
    const rotateKeyPair = jest.fn();

    const { getByText, getByRole } = await renderEditLti13Platform(
      {
        ...commonEditLti13PlatformProps,
        rotateKeyPairProvider: rotateKeyPair,
      },
      // http://blackboard:8200
      "aHR0cDovL2JsYWNrYm9hcmQ6ODIwMA==",
    );

    // click rotate key pair button
    await userEvent.click(getByText(rotateKeyPairLabel));

    const dialog = getByRole("dialog");
    const okButton = await findByText(dialog, okLabel);
    // click ok button
    await userEvent.click(okButton);

    expect(rotateKeyPair).toHaveBeenCalled();
  });
});
