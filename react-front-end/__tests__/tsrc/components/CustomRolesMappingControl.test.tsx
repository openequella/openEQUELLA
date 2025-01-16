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
import { roles } from "../../../__mocks__/RoleModule.mock";
import { generateWarnMsgForMissingIds } from "../../../tsrc/components/securityentitydialog/SecurityEntityHelper";
import {
  clickDeleteIconForEntity,
  clickOkButton,
  testRemoveAllAsync,
  waitForEntityDialogToRender,
} from "./securityentitydialog/SelectEntityDialogTestHelper";
import {
  commonCustomRolesMappingControlProps,
  doSearchAndSelectRole,
  openDialog,
  renderCustomRolesMappingControl,
  inputCustomRoleName,
} from "./CustomRolesMappingControlTestHelper";
import * as S from "fp-ts/string";

describe("CustomRolesMappingControl", () => {
  const systemAdmin = "System Administrator";
  const sharedRoleMapping = new Map().set(
    systemAdmin,
    SET.singleton(roles[0].id),
  );

  it("Should be able to input custom roles", async () => {
    const guest = "Guest";
    const expectedResult = new Map().set(guest, SET.singleton(roles[0].id));
    const selectedRoleName = roles[0].name;
    const onChange = jest.fn();
    const { container, getByRole } = renderCustomRolesMappingControl({
      ...commonCustomRolesMappingControlProps,
      onChange,
    });

    await openDialog(container);
    const dialog = getByRole("dialog");
    await inputCustomRoleName(dialog, guest);
    await doSearchAndSelectRole(dialog, selectedRoleName, selectedRoleName);
    await clickOkButton(dialog);

    const result = onChange.mock.lastCall[0];
    expect(result).toEqual(expectedResult);
  });

  it("Should be able to remove the custom role row when user delete the last custom role", async () => {
    const onChange = jest.fn();
    const { container, getByRole } = renderCustomRolesMappingControl({
      ...commonCustomRolesMappingControlProps,
      onChange,
      initialMappings: sharedRoleMapping,
    });

    await openDialog(container);
    const dialog = getByRole("dialog");
    await clickDeleteIconForEntity(dialog, roles[0].name);
    await clickOkButton(dialog);

    const result = onChange.mock.lastCall[0];
    expect(result).toEqual(new Map());
  });

  it("Should be able to remove one oEQ role from an existing custom role row", async () => {
    const initialRoleMapping = new Map().set(
      systemAdmin,
      SET.fromArray(S.Eq)([roles[0].id, roles[1].id]),
    );
    const onChange = jest.fn();
    const renderResult = renderCustomRolesMappingControl({
      ...commonCustomRolesMappingControlProps,
      onChange,
      initialMappings: initialRoleMapping,
    });
    const { container, getByRole } = renderResult;

    await openDialog(container);
    await waitForEntityDialogToRender(renderResult);

    const dialog = getByRole("dialog");
    await clickDeleteIconForEntity(dialog, roles[1].name);
    await clickOkButton(dialog);

    const result = onChange.mock.lastCall[0];
    expect(result).toEqual(sharedRoleMapping);
  });

  it("Should be able to add a oEQ role", async () => {
    const selectedRoleName = roles[0].name;
    const onChange = jest.fn();
    const { container, getByRole } = renderCustomRolesMappingControl({
      ...commonCustomRolesMappingControlProps,
      onChange,
    });

    await openDialog(container);
    const dialog = getByRole("dialog");
    await inputCustomRoleName(dialog, systemAdmin);
    await doSearchAndSelectRole(dialog, selectedRoleName, selectedRoleName);
    await clickOkButton(dialog);

    const result = onChange.mock.lastCall[0];
    expect(result).toEqual(sharedRoleMapping);
  });

  it("Should be able to add a oEQ role to an existing custom role row", async () => {
    const expectedResult = new Map().set(
      systemAdmin,
      SET.fromArray(S.Eq)([roles[0].id, roles[1].id]),
    );
    const selectedRoleName = roles[1].name;
    const onChange = jest.fn();
    const { container, getByRole } = renderCustomRolesMappingControl({
      ...commonCustomRolesMappingControlProps,
      onChange,
      initialMappings: sharedRoleMapping,
    });

    await openDialog(container);
    const dialog = getByRole("dialog");
    await inputCustomRoleName(dialog, systemAdmin);
    await doSearchAndSelectRole(dialog, selectedRoleName, selectedRoleName);
    await clickOkButton(dialog);

    const result = onChange.mock.lastCall[0];
    expect(result).toEqual(expectedResult);
  });

  it("Should be able to remove all selections by clicking the remove all button", async () => {
    const result = await testRemoveAllAsync(async (onChange: jest.Mock) => {
      const renderResult = renderCustomRolesMappingControl({
        ...commonCustomRolesMappingControlProps,
        onChange,
        initialMappings: sharedRoleMapping,
      });
      await openDialog(renderResult.container);
      return renderResult;
    });

    expect(result).toEqual(new Map());
  });

  it("Shows warning messages for CustomRoles if any role has been deleted but still passed as initial value", async () => {
    const customRole =
      "http://purl.imsglobal.org/vocab/lis/v2/system/person#Administrator";
    const { findByText } = renderCustomRolesMappingControl({
      ...commonCustomRolesMappingControlProps,
      initialMappings: new Map([[customRole, new Set(["deletedRole"])]]),
    });
    const expectedWarnMsg = `Custom role: ${customRole} - ${generateWarnMsgForMissingIds(
      new Set(["deletedRole"]),
      "role",
    )}`;
    const message = await findByText(expectedWarnMsg);
    expect(message).toBeInTheDocument();
  });
});
