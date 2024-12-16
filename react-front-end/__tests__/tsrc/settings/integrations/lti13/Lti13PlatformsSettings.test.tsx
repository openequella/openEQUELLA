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
import { getAllByRole } from "@testing-library/react";
import "@testing-library/jest-dom";
import userEvent from "@testing-library/user-event";
import {
  blackboard,
  canvas,
  moodle,
} from "../../../../../__mocks__/Lti13PlatformsModule.mock";
import { languageStrings } from "../../../../../tsrc/util/langstrings";
import {
  clickDeleteButtonForPlatform,
  clickEnabledSwitchForPlatform,
  getPlatformsSection,
  renderLti13PlatformsSettings,
} from "./Lti13PlatformSettingsTestHelper";

const { save: saveLabel } = languageStrings.common.action;

describe("Lti13PlatformsSettings", () => {
  it("Should be able to show a list of platforms", async () => {
    const { container } = await renderLti13PlatformsSettings();

    const listItems = getAllByRole(getPlatformsSection(container), "listitem");
    // platforms are displayed in div element
    const platforms = listItems.filter(({ tagName }) => tagName === "div");

    expect(platforms).toHaveLength(platforms.length);
  });

  it("Should be able to disable platforms", async () => {
    const onUpdatePlatform = jest.fn();
    const expectedStatus = [
      {
        platformId: canvas.platformId,
        enabled: false,
      },
      { platformId: moodle.platformId, enabled: false },
    ];
    const { container, getByText } =
      await renderLti13PlatformsSettings(onUpdatePlatform);

    await clickEnabledSwitchForPlatform(container, canvas.platformId);
    await clickEnabledSwitchForPlatform(container, moodle.platformId);
    // click save button
    await userEvent.click(getByText(saveLabel));

    const updatedStatus = onUpdatePlatform?.mock.lastCall[0];
    expect(updatedStatus).toEqual(expectedStatus);
  });

  it("Should be able to enable platforms", async () => {
    const onUpdateEnabledPlatforms = jest.fn();
    const expectedStatus = [
      {
        platformId: blackboard.platformId,
        enabled: true,
      },
    ];
    const { container, getByText } = await renderLti13PlatformsSettings(
      onUpdateEnabledPlatforms,
    );

    await clickEnabledSwitchForPlatform(container, blackboard.platformId);
    // click save button
    await userEvent.click(getByText(saveLabel));

    const updatedStatus = onUpdateEnabledPlatforms.mock.lastCall[0];
    expect(updatedStatus).toEqual(expectedStatus);
  });

  it("When user clicked delete button for a platform, it should be removed form list", async () => {
    const { container, queryByText } = await renderLti13PlatformsSettings();

    await clickDeleteButtonForPlatform(container, moodle.name);

    expect(queryByText(moodle.name)).not.toBeInTheDocument();
  });

  it("Should be able to delete platforms", async () => {
    const onDeletePlatforms = jest.fn().mockResolvedValue([
      {
        id: moodle.platformId,
        status: 200,
        message: "Success",
      },
      {
        id: canvas.platformId,
        status: 200,
        message: "Success",
      },
    ]);
    const { container, getByText } = await renderLti13PlatformsSettings(
      jest.fn(),
      onDeletePlatforms,
    );

    await clickDeleteButtonForPlatform(container, moodle.name);
    await clickDeleteButtonForPlatform(container, canvas.name);
    // click save button
    await userEvent.click(getByText(saveLabel));

    const deletePlatforms = onDeletePlatforms.mock.lastCall[0];
    expect(onDeletePlatforms.mock.calls).toHaveLength(1);
    expect(deletePlatforms).toEqual([canvas.platformId, moodle.platformId]);
  });
});
