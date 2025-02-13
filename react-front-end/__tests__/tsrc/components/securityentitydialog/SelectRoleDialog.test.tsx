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
import * as SET from "fp-ts/Set";
import { LOGGED_IN_USER_ROLE_NAME } from "../../../../__mocks__/ACLRecipientModule.mock";
import { roles } from "../../../../__mocks__/RoleModule.mock";
import {
  clickCancelButton,
  clickDeleteIconForEntity,
  clickOkButton,
  testRemoveAll,
  testRemoveEntity,
} from "./SelectEntityDialogTestHelper";
import {
  commonSelectRoleDialogProps,
  renderSelectRoleDialog,
  searchAndSelectRole,
} from "./SelectRoleDialogTestHelper";

describe("SelectRoleDialog", () => {
  it("Should be able to add a role", async () => {
    const selectedRoleName = LOGGED_IN_USER_ROLE_NAME;
    const onClose = jest.fn();
    const { getByRole } = await renderSelectRoleDialog({
      ...commonSelectRoleDialogProps,
      onClose,
    });

    const dialog = getByRole("dialog");

    await searchAndSelectRole(dialog, selectedRoleName, selectedRoleName);
    await clickOkButton(dialog);

    const result = onClose.mock.lastCall[0];
    expect(result).toEqual(SET.singleton(roles[0]));
  });

  it("Should be able to remove selected role", async () => {
    const result = await testRemoveEntity(
      (onClose: jest.Mock) =>
        renderSelectRoleDialog({
          ...commonSelectRoleDialogProps,
          value: SET.singleton(roles[0]),
          onClose,
        }),
      roles[0].name,
    );
    expect(result).toEqual(new Set());
  });

  it("Should be able to cancel the action", async () => {
    const selectedRoleName = "Guest User Role";
    const onClose = jest.fn();
    const { getByRole } = await renderSelectRoleDialog({
      ...commonSelectRoleDialogProps,
      value: SET.singleton(roles[0]),
      onClose,
    });

    const dialog = getByRole("dialog");

    await searchAndSelectRole(dialog, selectedRoleName, selectedRoleName);
    await clickDeleteIconForEntity(dialog, roles[0].name);

    await clickCancelButton(dialog);

    const result = onClose.mock.lastCall[0];
    expect(result).toBeUndefined();
  }, 15000);

  it("Should be able to remove all selections by clicking the remove all button", async () => {
    const result = await testRemoveAll((onClose: jest.Mock) =>
      renderSelectRoleDialog({
        ...commonSelectRoleDialogProps,
        value: new Set(roles),
        onClose,
      }),
    );
    expect(result).toEqual(new Set());
  });
});
