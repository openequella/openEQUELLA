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
import { groups } from "../../../../__mocks__/GroupModule.mock";
import { groupIds } from "../../../../tsrc/modules/GroupModule";
import {
  clickCancelButton,
  clickDeleteIconForEntity,
  clickOkButton,
  testRemoveAll,
  testRemoveEntity,
} from "./SelectEntityDialogTestHelper";
import {
  commonSelectGroupDialogProps,
  renderSelectGroupDialog,
  searchAndSelectGroup,
} from "./SelectGroupDialogTestHelper";

describe("SelectGroupDialog", () => {
  it("Should be able to add a group", async () => {
    const selectedGroupName = "Engineering & Computer Science Students";
    const onClose = jest.fn();
    const { getByRole } = await renderSelectGroupDialog({
      ...commonSelectGroupDialogProps,
      onClose,
    });

    const dialog = getByRole("dialog");

    await searchAndSelectGroup(dialog, selectedGroupName, selectedGroupName);
    await clickOkButton(dialog);

    const result = onClose.mock.lastCall[0];
    expect(result).toEqual(SET.singleton(groups[0].id));
  });

  it("Should be able to remove selected group", async () => {
    const result = await testRemoveEntity(
      (onClose: jest.Mock) =>
        renderSelectGroupDialog({
          ...commonSelectGroupDialogProps,
          value: SET.singleton(groups[0].id),
          onClose,
        }),
      groups[0].name,
    );
    expect(result).toEqual(new Set());
  });

  it("Should be able to cancel the action", async () => {
    const selectedGroupName = "group200";
    const onClose = jest.fn();
    const { getByRole } = await renderSelectGroupDialog({
      ...commonSelectGroupDialogProps,
      value: SET.singleton(groups[0].id),
      onClose,
    });

    const dialog = getByRole("dialog");

    await searchAndSelectGroup(dialog, selectedGroupName, selectedGroupName);
    await clickDeleteIconForEntity(dialog, groups[0].name);

    await clickCancelButton(dialog);

    const result = onClose.mock.lastCall[0];
    expect(result).toBeUndefined();
  }, 15000);

  it("Should be able to remove all selections by clicking the remove all button", async () => {
    const result = await testRemoveAll((onClose: jest.Mock) =>
      renderSelectGroupDialog({
        ...commonSelectGroupDialogProps,
        value: groupIds(new Set(groups)),
        onClose,
      }),
    );

    expect(result).toEqual(new Set());
  });
});
