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
import { FacetSelector } from "../../../../tsrc/search/components/FacetSelector";
import * as FacetSelectorMock from "../../../../__mocks__/FacetSelector.mock";
import "@testing-library/jest-dom/extend-expect";
import { languageStrings } from "../../../../tsrc/util/langstrings";

describe("<FacetSelector />", () => {
  // Mocked callbacks
  const onSelectTermsChange = jest.fn();
  const onShowMore = jest.fn();

  // Mocked Classifications
  const CITY = "City";
  const CITY_ID = 766943;
  const LANGUAGE = "Language";
  const COLOR = "Color";
  // Mocked facet
  const HOBART = "Hobart";
  const mockedSelectedTerms = new Map([[CITY_ID, [HOBART]]]);
  // The text of 'SHOW MORE' button
  const SHOW_MORE = languageStrings.searchpage.facetSelector.showMoreButton;

  const renderFacetSelector = () =>
    render(
      <FacetSelector
        classifications={FacetSelectorMock.classifications}
        onSelectTermsChange={onSelectTermsChange}
        onShowMore={onShowMore}
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

  // Return a Classification's 'SHOW MORE' button.
  const queryShowMoreButton = (
    container: HTMLElement,
    classificationName: string
  ) => {
    const classification = getClassificationByName(
      container,
      classificationName
    );
    return queryByText(classification, SHOW_MORE);
  };

  let page: RenderResult;
  beforeEach(() => {
    page = renderFacetSelector();
  });

  it("should display a list of classifications that have categories", () => {
    // Language and City should be displayed.
    [CITY, LANGUAGE].forEach((name) => {
      expect(getByText(page.container, name)).toBeInTheDocument();
    });
    // Color should not be displayed because it does not have categories.
    expect(queryByText(page.container, COLOR)).not.toBeInTheDocument();
  });

  it("should display the 'SHOW MORE' button when 'showMore' is true", () => {
    // City can show more categories.
    expect(queryShowMoreButton(page.container, CITY)).toBeInTheDocument();
    // Language does not have more categories to show.
    expect(queryShowMoreButton(page.container, LANGUAGE)).toBeNull();
  });

  it("should call 'onShowMore' when a 'SHOW MORE' button is clicked", () => {
    const showMoreButton = queryShowMoreButton(page.container, CITY);
    if (!showMoreButton) {
      throw new Error(
        "Unable to find 'SHOW MORE' button for Classification City."
      );
    }
    fireEvent.click(showMoreButton);
    expect(onShowMore).toHaveBeenLastCalledWith(CITY_ID);
  });

  it("should sort Classifications based on their order indexes", () => {
    const classifications = getAllByRole(page.container, "heading");
    // The order should be: CITY, Language.
    expect(classifications[0].textContent).toBe(CITY);
    expect(classifications[1].textContent).toBe(LANGUAGE);
  });

  it("should call onSelectTermsChange when a facet is selected", () => {
    // Select the facet of Hobart.
    const hobart = getByText(page.container, HOBART, { selector: "p" });
    fireEvent.click(hobart);
    expect(onSelectTermsChange).toHaveBeenLastCalledWith(mockedSelectedTerms);
  });
});
