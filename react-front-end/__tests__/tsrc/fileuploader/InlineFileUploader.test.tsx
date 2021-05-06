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
import { fireEvent, render, RenderResult } from "@testing-library/react";
import * as React from "react";
import { act } from "react-dom/test-utils";
import { v4 } from "uuid";
import {
  failedUploadResponse,
  files,
  successfulUploadResponse,
  uploadedFileEntry,
} from "../../../__mocks__/FileUploader.mock";
import {
  InlineFileUploader,
  InlineFileUploaderProps,
} from "../../../tsrc/fileuploader/InlineFileUploader";
import * as FileUploaderModule from "../../../tsrc/modules/FileUploaderModule";

const mockNewUpload = jest.spyOn(FileUploaderModule, "newUpload");
mockNewUpload.mockImplementation(() =>
  Promise.resolve({
    ...successfulUploadResponse,
    entry: { ...uploadedFileEntry, id: v4() },
  })
);
const mockUpdateDuplicateMessage = jest.spyOn(
  FileUploaderModule,
  "updateDuplicateMessage"
);
mockUpdateDuplicateMessage.mockImplementation(jest.fn);
const mockUpdateCtrlErrorText = jest.spyOn(
  FileUploaderModule,
  "updateCtrlErrorText"
);
mockUpdateCtrlErrorText.mockImplementation(jest.fn);

const props: InlineFileUploaderProps = {
  elem: document.createElement("div"),
  ctrlId: "P0C0",
  entries: [],
  maxAttachments: 2,
  canUpload: true,
  dialog: jest.fn(),
  editable: true,
  commandUrl: "https://localhost:8080/test/upload",
  reloadState: jest.fn(),
  strings: {
    edit: "edit",
    replace: "replace",
    delete: "delete",
    deleteConfirm: "Are you sure to delete?",
    cancel: "cancel",
    add: "add",
    drop: "drop",
    none: "no attached attachments",
    preview: "preview",
    toomany: "Max number of attachments is {0} please remove {1} attachment(s)",
    toomany_1: "Max number of attachments is 1 please remove {0} attachment(s)",
  },
};

describe("<InlineFileUploader />", () => {
  let component: RenderResult;

  const dropFiles = async (container: HTMLElement, files: File[]) => {
    const dropZone = container.querySelector<HTMLDivElement>(".filedrop");
    if (dropZone) {
      await act(async () => {
        await fireEvent.drop(dropZone, {
          dataTransfer: {
            files,
            types: ["Files"],
          },
        });
      });
    } else {
      throw new Error("Fail to find the Drop zone.");
    }
  };

  beforeEach(async () => {
    component = await render(<InlineFileUploader {...props} />);
  });

  it("supports drag and drop", async () => {
    const { container, queryByText } = component;
    // Make the first upload fail.
    mockNewUpload.mockResolvedValueOnce(failedUploadResponse);
    await dropFiles(container, files);
    expect(mockNewUpload).toHaveBeenCalledTimes(2);
    // The first upload fails so show the file name in a 'div' and failure reason in a 'p'.
    expect(queryByText("test1.png", { selector: "div" })).toBeInTheDocument();
    expect(
      queryByText("File is too large", { selector: "div" })
    ).toBeInTheDocument();
    // The second upload succeeds so show the file name as a link.
    expect(queryByText("test2.png", { selector: "a" })).toBeInTheDocument();
  });

  it("displays max attachment warning when the number of selected files is more than the limit", async () => {
    const moreFiles = [...files, new File(["test3"], "test3.png")];
    const { container } = component;
    await dropFiles(container, moreFiles);
    // Because the Wizard Control error message is NOT part of the File Uploader, we can only check if
    // the function that updates the warning has been called or not.
    expect(mockUpdateCtrlErrorText).toHaveBeenLastCalledWith(
      "P0C0",
      "Max number of attachments is 2 please remove 1 attachment(s)"
    );
  });

  it("displays duplicate attachment warning when a file existing in another Item is uploaded", async () => {
    const { container } = component;
    await dropFiles(container, files);
    // Because the Duplicate Warning message is NOT part of the File Uploader, we can only check if
    // the function that updates the warning has been called or not.
    expect(mockUpdateDuplicateMessage).toHaveBeenLastCalledWith("P0C0", true);
  });
});
