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
import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

/**
 * Use to 'select' an option from a MUI `<Select>`. This is done through a series of
 * userEvent.click() calls.
 *
 * @param container The base container from which to start a search
 * @param selector A CSS selector to pass to `HTMLElement.querySelector()` to find the `<Select>`
 * @param optionText The text which represents the option you wish to select - user visible.
 */
export const selectOption = (
  container: HTMLElement,
  selector: string,
  optionText: string
) => {
  const muiSelect = container.querySelector(selector);
  if (!muiSelect) {
    throw Error("Unable to find MUI Select.");
  }

  // Click the <Select>
  userEvent.click(muiSelect);
  // .. then click the option in the list
  userEvent.click(
    screen.getByText(
      optionText,
      { selector: 'li[role="option"]' } // could add aria-labelledby, but maybe that's over engineering it
    )
  );
};
