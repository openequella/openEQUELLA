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
import {
  queryByText,
  render,
  RenderResult,
  queryByDisplayValue,
} from "@testing-library/react";
import ContentIndexSettings from "../../../tsrc/settings/Search/ContentIndexSettings";
import "@testing-library/jest-dom";
import * as SearchSettingsModule from "../../../tsrc/modules/SearchSettingsModule";
import { NavigationGuardProps } from "../../../tsrc/components/NavigationGuard";
import { languageStrings } from "../../../tsrc/util/langstrings";

/**
 * Mock NavigationGuard as there is no need to include it in this test.
 */

jest.mock("../../../tsrc/components/NavigationGuard", () => ({
  NavigationGuard: (_: NavigationGuardProps) => {
    return <div />;
  },
}));

describe("Content Index Settings Page", () => {
  const defaultVals = SearchSettingsModule.defaultSearchSettings;
  const contentIndexSettingsStrings =
    languageStrings.settings.searching.contentIndexSettings;
  jest
    .spyOn(SearchSettingsModule, "getSearchSettingsFromServer")
    .mockImplementation(() =>
      Promise.resolve(SearchSettingsModule.defaultSearchSettings),
    );

  let page: RenderResult;
  beforeEach(async () => {
    page = render(<ContentIndexSettings updateTemplate={jest.fn()} />);
  });

  it("Should fetch the search settings on page load", () => {
    expect(
      SearchSettingsModule.getSearchSettingsFromServer,
    ).toHaveBeenCalledTimes(1);
  });

  it("shows the default Content indexing value", () => {
    expect(
      page.queryByText(contentIndexSettingsStrings.general),
    ).toBeInTheDocument();
    const contentIndexingSetting = page
      .getByText(contentIndexSettingsStrings.name)
      .closest("li");
    if (contentIndexingSetting === null) {
      throw new Error("Failed to find the Content indexing setting");
    }
    expect(
      queryByText(
        contentIndexingSetting,
        contentIndexSettingsStrings.option.none,
      ),
    ).toBeInTheDocument();
    expect(
      queryByDisplayValue(contentIndexingSetting, defaultVals.urlLevel),
    ).toBeInTheDocument();
  });

  it.each([
    [contentIndexSettingsStrings.titleBoostingTitle, defaultVals.titleBoost],
    [
      contentIndexSettingsStrings.metaBoostingTitle,
      defaultVals.descriptionBoost,
    ],
    [
      contentIndexSettingsStrings.attachmentBoostingTitle,
      defaultVals.attachmentBoost,
    ],
  ])(
    "shows the default %s boosting value",
    (boostingType: string, defaultValue: number) => {
      const boosting = page.getByText(boostingType).closest("li");
      if (boosting === null) {
        throw new Error(`Failed to find the ${boostingType} boosting setting`);
      }
      expect(queryByDisplayValue(boosting, defaultValue)).toBeInTheDocument();
    },
  );
});
