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
import * as React from "react";
import { mount, ReactWrapper } from "enzyme";
import ContentIndexSettings from "../../../tsrc/settings/Search/ContentIndexSettings";
import * as SearchSettingsModule from "../../../tsrc/settings/Search/SearchSettingsModule"; // eslint-disable-line
//import SettingsList from "../../../tsrc/components/SettingsList";
//import WebPageIndexSetting from "../../../tsrc/settings/Search/components/WebPageIndexSetting";
import { Slider } from "@material-ui/core";
import { NavigationGuardProps } from "../../../tsrc/components/NavigationGuard";
import { act } from "react-dom/test-utils";

/**
 * Mock NavigationGuard as there is no need to include it in this test.
 */

jest.mock("../../../tsrc/components/NavigationGuard", () => ({
  NavigationGuard: (props: NavigationGuardProps) => {
    return <div />;
  },
}));

const mockGetSearchSettingsFromServer = jest.spyOn(
  SearchSettingsModule,
  "getSearchSettingsFromServer"
);

const mockSaveSearchSettingsToServer = jest.spyOn(
  SearchSettingsModule,
  "saveSearchSettingsToServer"
);

describe("<ContentIndexSettingsPage />", () => {
  let component: ReactWrapper;
  beforeEach(async () => {
    const getSearchSettingsPromise = mockGetSearchSettingsFromServer.mockImplementation(
      () => Promise.resolve(SearchSettingsModule.defaultSearchSettings)
    );
    component = mount(<ContentIndexSettings updateTemplate={jest.fn()} />);
    await act(async () => {
      await getSearchSettingsPromise;
    });
  });
  afterEach(() => jest.clearAllMocks());

  const getSaveButton = () => component.find("#_saveButton").hostNodes();

  let modifiedSearchSettings = {
    ...SearchSettingsModule.defaultSearchSettings,
  };
  modifiedSearchSettings.titleBoost = 3;
  modifiedSearchSettings.descriptionBoost = 4;
  modifiedSearchSettings.attachmentBoost = 6;

  it("Should fetch the default search settings", () => {
    expect(
      SearchSettingsModule.getSearchSettingsFromServer
    ).toHaveBeenCalledTimes(1);
  });

  it("Should save the changes made", () => {
    const makeChanges = async () => {
      const titleBoostSlider = component.find(Slider).at(0);

      const metaBoostSlider = component.find(Slider).at(1);

      const attachBoostSlider = component.find(Slider).at(2);
      act(
        async () =>
          await attachBoostSlider.props().onChange!(
            {} as any,
            modifiedSearchSettings.attachmentBoost
          )
      );
      act(
        async () =>
          await metaBoostSlider.props().onChange!(
            {} as any,
            modifiedSearchSettings.descriptionBoost
          )
      );
      act(
        async () =>
          await titleBoostSlider.props().onChange!(
            {} as any,
            modifiedSearchSettings.titleBoost
          )
      );
    };

    const save = async (errorsReturned: boolean) => {
      const updatePromise = mockSaveSearchSettingsToServer.mockResolvedValueOnce(
        errorsReturned ? ["Failed to update"] : []
      );
      getSaveButton().simulate("click");
      await act(async () => {
        await updatePromise;
      });
    };
    makeChanges();
    save(false);

    expect(mockSaveSearchSettingsToServer).toHaveBeenCalledWith(
      modifiedSearchSettings
    );
  });
});
