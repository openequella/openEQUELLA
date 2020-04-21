import * as React from "react";

import { shallow } from "enzyme";
import MimeTypeFilterEditingDialog from "../../../tsrc/settings/Search/searchfilter/MimeTypeFilterEditingDialog";
import { MimeTypeFilter } from "../../../tsrc/settings/Search/searchfilter/SearchFilterSettingsModule";

describe("<MimeTypeFilterEditingDialog />", () => {
  const onClose = jest.fn();
  const addOrUpdate = jest.fn();
  const handleError = jest.fn();
  const renderDialog = (filter: MimeTypeFilter | undefined = undefined) =>
    shallow(
      <MimeTypeFilterEditingDialog
        open={true}
        onClose={onClose}
        addOrUpdate={addOrUpdate}
        mimeTypeFilter={filter}
        handleError={handleError}
      />
    );

  describe("when filter is defined", () => {
    it("should display 'OK' as the Save button text", () => {
      const component = renderDialog({
        id: "testing ID",
        name: "image filter",
        mimeTypes: ["IMAGE/PNG", "IMAGE/JPEG"]
      });
      const saveButton = component.find("#MimeTypeFilterEditingDialog_save");
      expect(saveButton.text()).toBe("OK");
    });
  });

  describe("when filter is undefined", () => {
    it("should display 'Add' as the Save button text", () => {
      const component = renderDialog();
      const saveButton = component.find("#MimeTypeFilterEditingDialog_save");
      expect(saveButton.text()).toBe("Add");
    });
  });

  describe("when filter name is empty", () => {
    it("should disable the Save button", () => {
      const component = renderDialog();
      const saveButton = component.find("#MimeTypeFilterEditingDialog_save");
      expect(saveButton.is("[disabled]")).toBeTruthy();
    });
  });
});
