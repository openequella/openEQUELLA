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
import * as E from "fp-ts/Either";
import {
  everyoneRecipient,
  everyoneRecipientHumanReadableExpression,
  everyoneRecipientRawExpression,
  groupStudentRecipient,
  groupStudentRecipientHumanReadableExpression,
  groupStudentRecipientRawExpression,
  ipRecipient,
  ownerRecipient,
  ownerRecipientHumanReadableExpression,
  ownerRecipientRawExpression,
  referRecipient,
  roleGuestRecipient,
  roleGuestRecipientHumanReadableExpression,
  roleGuestRecipientRawExpression,
  ssoMoodleRecipient,
  ssoMoodleRecipientHumanReadableExpression,
  ssoMoodleRecipientRawExpression,
  user100Recipient,
  user100RecipientHumanReadableExpression,
  user100RecipientRawExpression,
} from "../../../__mocks__/ACLRecipientModule.mock";
import { findGroupById } from "../../../__mocks__/GroupModule.mock";
import { findRoleById } from "../../../__mocks__/RoleModule.mock";
import { findUserById } from "../../../__mocks__/UserModule.mock";
import {
  showRecipient,
  showRecipientHumanReadable,
} from "../../../tsrc/modules/ACLRecipientModule";

describe("showRecipient", () => {
  it.each([
    ["owner", ownerRecipient, ownerRecipientRawExpression],
    ["everyone", everyoneRecipient, everyoneRecipientRawExpression],
    ["user", user100Recipient, user100RecipientRawExpression],
    ["role", roleGuestRecipient, roleGuestRecipientRawExpression],
    ["group", groupStudentRecipient, groupStudentRecipientRawExpression],
    ["sso", ssoMoodleRecipient, ssoMoodleRecipientRawExpression],
    ["ip", ipRecipient("192.168.0.1"), "I:192.168.0.1"],
    ["refer", referRecipient("www.edalex.com"), "F:www.edalex.com"],
  ])(
    "should return the raw expression for %s",
    (_, recipient, expectedExpression) => {
      expect(showRecipient(recipient)).toEqual(expectedExpression);
    }
  );
});

describe("showRecipientHumanReadable", () => {
  /**
   * `showRecipientHumanReadable` setup with mocked versions of entity look-up functions for users, groups and roles.
   */
  const showRecipientHumanReadableWithMocks = showRecipientHumanReadable({
    resolveUserProvider: findUserById,
    resolveGroupProvider: findGroupById,
    resolveRoleProvider: findRoleById,
  });

  it.each([
    ["owner", ownerRecipient, ownerRecipientHumanReadableExpression],
    ["everyone", everyoneRecipient, everyoneRecipientHumanReadableExpression],
    ["user", user100Recipient, user100RecipientHumanReadableExpression],
    ["role", roleGuestRecipient, roleGuestRecipientHumanReadableExpression],
    [
      "group",
      groupStudentRecipient,
      groupStudentRecipientHumanReadableExpression,
    ],
    ["sso", ssoMoodleRecipient, ssoMoodleRecipientHumanReadableExpression],
    ["ip", ipRecipient("192.168.0.1"), "From 192.168.0.1"],
    ["refer", referRecipient("www.edalex.com"), "From www.edalex.com"],
  ])(
    "should return a human-readable expression for %s",
    async (_, recipient, expectedExpression) => {
      await expect(
        showRecipientHumanReadableWithMocks(recipient)()
      ).resolves.toEqual(E.right(expectedExpression));
    }
  );

  it.each([
    ["user", user100Recipient, "Can't find user: nonexistent-id"],
    ["group", groupStudentRecipient, "Can't find group: nonexistent-id"],
    ["role", roleGuestRecipient, "Can't find role: nonexistent-id"],
  ])(
    "should return error message it failed to fetch the name of %s recipient",
    async (_, recipient, errorMessage) => {
      await expect(
        showRecipientHumanReadableWithMocks({
          ...recipient,
          expression: "nonexistent-id",
        })()
      ).resolves.toEqual(E.left(errorMessage));
    }
  );
});
