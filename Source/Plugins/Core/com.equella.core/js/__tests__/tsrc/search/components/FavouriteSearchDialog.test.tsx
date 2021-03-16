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
import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom/extend-expect";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import { act } from "react-dom/test-utils";
import {
  FavouriteSearchDialog,
  FavouriteSearchDialogProps,
} from "../../../../tsrc/search/components/FavouriteSearchDialog";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import { queryMuiButtonByText, queryMuiTextField } from "../../MuiQueries";

describe("<FavouriteSearchDialog/>", () => {
  const closeDialog = jest.fn();
  const onConfirm = jest.fn().mockImplementation(() => Promise.resolve());
  const commonProps: FavouriteSearchDialogProps = {
    open: true,
    closeDialog,
    onConfirm,
  };

  const getConfirmButton = (): HTMLElement => {
    const dialog = screen.getByRole("dialog");
    const confirmButton = queryMuiButtonByText(
      dialog,
      languageStrings.common.action.ok
    );
    if (!confirmButton) {
      throw new Error(
        "Failed to find the Confirm button in FavouriteSearchDialog"
      );
    }
    return confirmButton;
  };

  const getTextField = (): HTMLElement => {
    const dialog = screen.getByRole("dialog");
    const searchNameInput = queryMuiTextField(
      dialog,
      languageStrings.searchpage.favouriteSearch.text
    );
    if (!searchNameInput) {
      throw new Error("Failed to find the TextField in FavouriteSearchDialog");
    }
    return searchNameInput;
  };

  it("disables the Confirm button by default", () => {
    render(<FavouriteSearchDialog {...commonProps} />);
    expect(getConfirmButton()).toBeDisabled();
  });

  it("enables the Confirm button if a non-empty text is used as the search name", async () => {
    render(<FavouriteSearchDialog {...commonProps} />);
    userEvent.type(getTextField(), "test");

    const confirmButton = getConfirmButton();
    expect(confirmButton).not.toBeDisabled();
    await act(async () => {
      await userEvent.click(confirmButton);
    });

    expect(onConfirm).toHaveBeenCalledTimes(1);
    expect(closeDialog).toHaveBeenCalledTimes(1);
  });
});
