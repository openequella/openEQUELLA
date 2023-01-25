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
import "@testing-library/jest-dom/extend-expect";
import { act, render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import { mockedTaxonomyTerms } from "../../../../__mocks__/TaxonomyTerms.mock";
import { WizardSimpleTermSelector } from "../../../../tsrc/components/wizard/WizardSimpleTermSelector";
import { languageStrings } from "../../../../tsrc/util/langstrings";

describe("<WizardSimpleTermSelector/>", () => {
  // WizardSimpleTermSelector uses a debounce, so we need to be able to advanced
  // the timer to trigger a search.
  const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });

  const label = languageStrings.termSelector.placeholder;
  const termProvider = jest.fn().mockResolvedValue({
    start: 0,
    length: 6,
    available: 6,
    results: mockedTaxonomyTerms,
  });
  const onSelect = jest.fn();

  const renderWizardSimpleTermSelector = (
    isAllowMultiple: boolean = false,
    values = new Set<string>()
  ) =>
    render(
      <WizardSimpleTermSelector
        isAllowMultiple={isAllowMultiple}
        mandatory={false}
        onSelect={onSelect}
        selectedTaxonomy=""
        selectionRestriction="UNRESTRICTED"
        termProvider={termProvider}
        termStorageFormat="FULL_PATH"
        values={values}
        id="test"
      />
    );

  const searchTerms = async (input: HTMLElement, query: string) =>
    await act(async () => {
      await user.type(input, query);
      jest.advanceTimersByTime(1000);
    });

  beforeEach(() => {
    // WizardSimpleTermSelector uses a debounce, so we need to be able to advanced
    // the timer to trigger a search.
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it("debounces the search", async () => {
    const { getByLabelText } = renderWizardSimpleTermSelector();
    const input = getByLabelText(label);
    await searchTerms(input, "ring");
    // Putting 4 chars in the input should trigger one search.
    expect(termProvider).toHaveBeenCalledTimes(1);
  });

  it("displays provided values", async () => {
    const terms: string[] = ["term1", "term2"];
    const { queryByText } = await renderWizardSimpleTermSelector(
      true,
      new Set(terms)
    );

    expect(terms.every((t) => queryByText(t))).toBeTruthy();
  });

  it("removes a selected term", async () => {
    const term = "Jest";
    const values = new Set([term]);
    const { getByLabelText } = await renderWizardSimpleTermSelector(
      true,
      values
    );

    await user.click(
      getByLabelText(`${languageStrings.common.action.delete} ${term}`)
    );
    // The only value has been deleted so the handler should be called with an empty set.
    expect(onSelect).toHaveBeenLastCalledWith(new Set());
  });
});
