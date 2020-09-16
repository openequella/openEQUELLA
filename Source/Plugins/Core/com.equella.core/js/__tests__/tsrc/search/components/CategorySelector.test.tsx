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
  render,
  queryByText,
  getByText,
  fireEvent,
  RenderResult,
  getAllByRole,
} from "@testing-library/react";
import type { SelectedCategories } from "../../../../tsrc/modules/SearchFacetsModule";
import { CategorySelector } from "../../../../tsrc/search/components/CategorySelector";
import * as CategorySelectorMock from "../../../../__mocks__/CategorySelector.mock";
import "@testing-library/jest-dom/extend-expect";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import { queryMuiButtonByText } from "../../MuiQueries";

describe("<CategorySelector />", () => {
  // Mocked callbacks
  const onSelectedCategoriesChange = jest.fn();

  // Mocked Classifications
  const CITY = "City";
  const LANGUAGE = "Language";
  const COLOR = "Color";
  // Mocked facet
  const HOBART = "Hobart";

  // The text of 'SHOW MORE' and 'SHOW LESS' buttons
  const SHOW_MORE = languageStrings.common.action.showMore;
  const SHOW_LESS = languageStrings.common.action.showLess;

  const renderCategorySelector = () =>
    render(
      <CategorySelector
        classifications={CategorySelectorMock.classifications}
        onSelectedCategoriesChange={onSelectedCategoriesChange}
      />
    );

  // Return a 'li' that represents a Classification.
  const getClassificationByName = (
    container: HTMLElement,
    name: string
  ): HTMLElement => {
    // All needed information of a Classification is contained inside a 'li'.
    const classification = queryByText(container, name)?.closest("li");
    if (!classification) {
      throw new Error(`Unable to find Classification ${name}`);
    }
    return classification;
  };

  let page: RenderResult;
  beforeEach(() => {
    page = renderCategorySelector();
  });

  it("should display a list of classifications that have categories", () => {
    // Language and City should be displayed.
    [CITY, LANGUAGE].forEach((name) => {
      expect(getByText(page.container, name)).toBeInTheDocument();
    });
    // Color should not be displayed because it does not have categories.
    expect(queryByText(page.container, COLOR)).not.toBeInTheDocument();
  });

  it("should display the 'SHOW MORE' button when the number of categories is more than maximum display number", () => {
    // City can show more categories.
    expect(
      queryMuiButtonByText(
        getClassificationByName(page.container, CITY),
        SHOW_MORE
      )
    ).toBeInTheDocument();
    // Language does not have more categories to show.
    expect(
      queryMuiButtonByText(
        getClassificationByName(page.container, LANGUAGE),
        SHOW_MORE
      )
    ).toBeNull();
  });

  it("should show all categories and 'SHOW LESS' button when 'SHOW MORE' button is clicked", () => {
    const classification = getClassificationByName(page.container, CITY);
    const showMoreButton = queryMuiButtonByText(classification, SHOW_MORE);
    if (!showMoreButton) {
      throw new Error("Unable to find 'SHOW MORE' button for Classification.");
    }
    expect(getAllByRole(classification, "checkbox")).toHaveLength(2);
    fireEvent.click(showMoreButton);
    expect(getAllByRole(classification, "checkbox")).toHaveLength(3);
    expect(queryMuiButtonByText(classification, SHOW_LESS)).toBeInTheDocument();
  });

  it("should sort Classifications based on their order indexes", () => {
    const classifications = getAllByRole(page.container, "heading");
    // The order should be: CITY, Language.
    expect(classifications[0].textContent).toBe(CITY);
    expect(classifications[1].textContent).toBe(LANGUAGE);
  });

  it("should call onSelectedCategoriesChange when a facet is selected", () => {
    const selectedCategories: SelectedCategories[] = [
      { id: 766943, categories: ["Hobart"] },
    ];
    const hobart = getByText(page.container, HOBART, { selector: "p" });
    fireEvent.click(hobart);
    expect(onSelectedCategoriesChange).toHaveBeenLastCalledWith(
      selectedCategories
    );
  });
});
