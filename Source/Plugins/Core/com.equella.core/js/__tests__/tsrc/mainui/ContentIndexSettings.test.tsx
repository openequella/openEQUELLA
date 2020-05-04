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
import { mount } from "enzyme";
import ContentIndexSettings from "../../../tsrc/settings/Search/ContentIndexSettings";
import * as SearchSettingsModule from "../../../tsrc/settings/Search/SearchSettingsModule"; // eslint-disable-line
import SettingsList from "../../../tsrc/components/SettingsList";
import WebPageIndexSetting from "../../../tsrc/settings/Search/components/WebPageIndexSetting";
import { Slider } from "@material-ui/core";
import { NavigationGuardProps } from "../../../tsrc/components/NavigationGuard";

/**
 * Mock NavigationGuard as there is no need to include it in this test.
 */

jest.mock("../../../tsrc/components/NavigationGuard", () => ({
  NavigationGuard: (props: NavigationGuardProps) => {
    return <div />;
  },
}));

describe("Content Index Settings Page", () => {
  jest
    .spyOn(SearchSettingsModule, "getSearchSettingsFromServer")
    .mockImplementation(() =>
      Promise.resolve(SearchSettingsModule.defaultSearchSettings)
    );

  const renderComponent = () =>
    mount(<ContentIndexSettings updateTemplate={jest.fn()} />);

  describe("When content indexing settings page page is loaded", () => {
    const component = renderComponent();
    const listControls = component.find(SettingsList);
    const defaultVals = SearchSettingsModule.defaultSearchSettings;

    it("Should fetch the search settings", () => {
      expect(
        SearchSettingsModule.getSearchSettingsFromServer
      ).toHaveBeenCalledTimes(1);
    });

    it("Should display the default values", () => {
      expect(listControls.length).toBeGreaterThanOrEqual(2);

      //content indexing stuff
      const contentIndex = listControls.find(WebPageIndexSetting);

      expect(contentIndex.prop("value")).toEqual(defaultVals.urlLevel);

      //boosting
      const titleBoostSlider = listControls
        .findWhere((c) => c.prop("primaryText") === "Title")
        .find(Slider);

      expect(titleBoostSlider.prop("value")).toEqual(defaultVals.titleBoost);

      const metadataBoostSlider = listControls
        .findWhere((c) => c.prop("primaryText") === "Other metadata")
        .find(Slider);

      expect(metadataBoostSlider.prop("value")).toEqual(
        defaultVals.descriptionBoost
      );

      const attachmentBoostSlider = listControls
        .findWhere((c) => c.prop("primaryText") === "Attachment content")
        .find(Slider);
      expect(attachmentBoostSlider.prop("value")).toEqual(
        defaultVals.attachmentBoost
      );
    });
  });
});
