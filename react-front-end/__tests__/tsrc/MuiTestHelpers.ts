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
import { screen, SelectorMatcherOptions } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

/**
 * Provides the method to click on a MUI select element, where it is not suitable to simply click
 * on the 'text' currently within the select.
 *
 * @param container The base container from which to start a search
 * @param selector A CSS selector to pass to `HTMLElement.querySelector()` to find the `<Select>`
 */
export const clickSelect = async (
  container: HTMLElement,
  selector: string,
): Promise<Element> => {
  const muiSelect = container.querySelector(selector);
  if (!muiSelect) {
    throw Error("Unable to find MUI Select.");
  }

  await userEvent.click(muiSelect);

  return muiSelect;
};

const selectFinderOptions: SelectorMatcherOptions = {
  selector: 'li[role="option"]',
}; // could add aria-labelledby, but maybe that's over engineering it

/**
 * MUI Selects when expanded simply add their content to the tail of the document. As a result, to
 * find the option to interact with can be a bit trickier than simply `container.getByText`. The
 * intention is that this function is used alongside `clickSelect`.
 *
 * @param optionText The text which represents the option you wish to find - user visible.
 */
export const getSelectOption = (optionText: string): HTMLElement =>
  screen.getByText(optionText, selectFinderOptions);

/**
 * Same as `findSelectOption`, but rather than `throw` when element not found will simple return
 * a null.
 */
export const querySelectOption = (optionText: string): HTMLElement | null =>
  screen.queryByText(optionText, selectFinderOptions);

/**
 * Use to 'select' an option from a MUI `<Select>`. This is done through a series of
 * userEvent.click() calls.
 *
 * @param container The base container from which to start a search
 * @param selector A CSS selector to pass to `HTMLElement.querySelector()` to find the `<Select>`
 * @param optionText The text which represents the option you wish to select - user visible.
 */
export const selectOption = async (
  container: HTMLElement,
  selector: string,
  optionText: string,
) => {
  // Click the <Select>
  await clickSelect(container, selector);
  // Click the option in the list
  await userEvent.click(getSelectOption(optionText));
};

/**
 * Helper to get Mui text field by aria label.
 *
 * @param container The container element.
 * @param label The aria label of the text field.
 */
export const getMuiTextFieldByAriaLabel = (
  container: HTMLElement,
  label: string,
): Element => {
  const input = container.querySelector(`div[aria-label='${label}'] input`);
  if (!input) {
    throw new Error(`Unable to get text field: ${label}`);
  }
  return input;
};

/**
 * Helper to type in text field input.
 *
 * @param container The container element.
 * @param label The aria label of the text field.
 * @param value The value to type in.
 */
export const fillMuiTextFieldByAriaLabel = async (
  container: HTMLElement,
  label: string,
  value: string,
): Promise<void> => {
  const input = getMuiTextFieldByAriaLabel(container, label);
  await userEvent.type(input, `${value}`);
};
