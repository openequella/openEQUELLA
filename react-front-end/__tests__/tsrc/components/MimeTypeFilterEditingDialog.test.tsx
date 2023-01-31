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
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import "@testing-library/jest-dom/extend-expect";
import { render, screen, waitFor } from "@testing-library/react";
import { getMimeTypesFromServer } from "../../../__mocks__/MimeTypes.mock";
import MimeTypeFilterEditingDialog from "../../../tsrc/settings/Search/searchfilter/MimeTypeFilterEditingDialog";

describe("<MimeTypeFilterEditingDialog />", () => {
  const onClose = jest.fn();
  const addOrUpdate = jest.fn();
  const renderDialog = async (
    filter: OEQ.SearchFilterSettings.MimeTypeFilter | undefined = undefined
  ) => {
    render(
      <MimeTypeFilterEditingDialog
        open
        onClose={onClose}
        addOrUpdate={addOrUpdate}
        mimeTypeFilter={filter}
        mimeTypeSupplier={jest.fn().mockResolvedValue(getMimeTypesFromServer)}
      />
    );

    await waitFor(() => screen.getByRole("dialog"));
  };
  const getSaveButton = () =>
    screen.queryByTestId("MimeTypeFilterEditingDialog_save");

  it.each([
    [
      "OK",
      "filter is defined",
      {
        id: "testing ID",
        name: "image filter",
        mimeTypes: ["IMAGE/PNG", "IMAGE/JPEG"],
      },
    ],
    ["Add", "filter is undefined", undefined],
  ])(
    "should display %s as the Save button text when %s",
    async (
      text: string,
      condition: string,
      filter: OEQ.SearchFilterSettings.MimeTypeFilter | undefined
    ) => {
      await renderDialog(filter);

      expect(getSaveButton()).toHaveTextContent(text);
    }
  );

  it("should disable the Save button when filter name is empty", async () => {
    await renderDialog();
    expect(getSaveButton()).toHaveAttribute("disabled");
  });
});
