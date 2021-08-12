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
import "@testing-library/jest-dom/extend-expect";
import { act, render, screen } from "@testing-library/react";
import { getMimeTypesFromServer } from "../../../__mocks__/MimeTypes.mock";
import MimeTypeFilterEditingDialog from "../../../tsrc/settings/Search/searchfilter/MimeTypeFilterEditingDialog";
import { MimeTypeFilter } from "../../../tsrc/modules/SearchFilterSettingsModule";

describe("<MimeTypeFilterEditingDialog />", () => {
  const onClose = jest.fn();
  const addOrUpdate = jest.fn();
  const handleError = jest.fn();
  const renderDialog = async (filter: MimeTypeFilter | undefined = undefined) =>
    await act(async () => {
      await render(
        <MimeTypeFilterEditingDialog
          open
          onClose={onClose}
          addOrUpdate={addOrUpdate}
          mimeTypeFilter={filter}
          handleError={handleError}
          mimeTypeSupplier={jest.fn().mockResolvedValue(getMimeTypesFromServer)}
        />
      );
    });
  const getSaveButton = () =>
    screen.queryByTestId("MimeTypeFilterEditingDialog_save");

  describe("when filter is defined", () => {
    it("should display 'OK' as the Save button text", () => {
      renderDialog({
        id: "testing ID",
        name: "image filter",
        mimeTypes: ["IMAGE/PNG", "IMAGE/JPEG"],
      });

      expect(getSaveButton()).toHaveTextContent("OK");
    });
  });

  describe("when filter is undefined", () => {
    it("should display 'Add' as the Save button text", () => {
      renderDialog();
      expect(getSaveButton()).toHaveTextContent("Add");
    });
  });

  describe("when filter name is empty", () => {
    it("should disable the Save button", () => {
      renderDialog();
      expect(getSaveButton()).toHaveAttribute("disabled");
    });
  });
});
