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
import { render } from "@testing-library/react";
import "@testing-library/jest-dom";
import * as React from "react";
import FavouriteItemDialog from "../../../../tsrc/search/components/FavouriteItemDialog";
import { languageStrings } from "../../../../tsrc/util/langstrings";

const { selectVersion, toThisVersion, versionOptions } =
  languageStrings.selectItemVersionDialog;

describe("<FavouriteItemDialog />", () => {
  const commonProps = {
    open: true,
    closeDialog: jest.fn(),
    updateFavouriteItem: jest.fn(),
  };

  it("shows a RadioGroup of two options for adding an Item that's on the latest version", () => {
    const { queryByLabelText, queryByText } = render(
      <FavouriteItemDialog {...commonProps} isAdded={false} isLatestVersion />,
    );
    expect(queryByText(selectVersion)).toBeInTheDocument();
    expect(queryByLabelText(versionOptions.useThisVersion)).toBeInTheDocument();
    expect(
      queryByLabelText(versionOptions.useLatestVersion),
    ).toBeInTheDocument();
  });

  it("shows a text saying the favourite only points current version if Item is on older version", () => {
    const { queryByText } = render(
      <FavouriteItemDialog
        {...commonProps}
        isAdded={false}
        isLatestVersion={false}
      />,
    );
    expect(queryByText(toThisVersion)).toBeInTheDocument();
  });

  it("shows an alert for deleting a favourite Item", () => {
    const { queryByText } = render(
      <FavouriteItemDialog {...commonProps} isAdded isLatestVersion={false} />,
    );
    expect(
      queryByText(languageStrings.searchpage.favouriteItem.removeAlert),
    ).toBeInTheDocument();
  });
});
