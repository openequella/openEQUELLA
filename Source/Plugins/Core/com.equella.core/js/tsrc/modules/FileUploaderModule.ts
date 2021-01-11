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
import Axios, { CancelTokenSource } from "axios";

export interface OeqFileInfo {
  id: string;
  name: string;
  link: string;
  preview: boolean;
  editable: boolean;
  children: OeqFileInfo[];
}

export interface UploadedFile {
  uploadedFile: OeqFileInfo;
  status: "uploaded" | "failed";
}

export interface UploadingFile {
  localId: string;
  uploadingFile: File;
  status: "uploading";
  uploadPercentage: number;
}

export const isUploadedFile = (
  file: UploadedFile | UploadingFile
): file is UploadedFile =>
  file.status === "uploaded" || file.status === "failed";

interface AttachmentDuplicateInfo {
  displayWarningMessage: boolean;
  warningMessageWebId: string;
}

type UploadResponseType =
  | "updateentry"
  | "removeentries"
  | "uploadfailed"
  | "newuploadresponse";

interface BasicUploadCommand {
  command: string;
}

interface NewUpload extends BasicUploadCommand {
  filename: string;
  size: number;
}

interface DeleteUpload extends BasicUploadCommand {
  id: string;
}

interface BasicUploadResponse {
  response: UploadResponseType;
}

interface UpdateEntry extends BasicUploadResponse {
  entry: OeqFileInfo;
  attachmentDuplicateInfo?: AttachmentDuplicateInfo;
}

interface UploadFailed extends BasicUploadResponse {
  reason: string;
}

interface RemoveEntries extends BasicUploadResponse {
  ids: string[];
  attachmentDuplicateInfo?: AttachmentDuplicateInfo;
}

interface NewUploadResponse extends BasicUploadResponse {
  uploadUrl: string;
  id: string;
  name: string;
}

export type CompleteUploadResponse = UpdateEntry | UploadFailed;

export const isUpdateEntry = (
  uploadResponse: CompleteUploadResponse
): uploadResponse is UpdateEntry => uploadResponse.response === "updateentry";

const CancelToken = Axios.CancelToken;

const axiosSourceMap: Map<string, CancelTokenSource> = new Map();
export const getAxiosSource = (id: string) => axiosSourceMap.get(id);

export const newUpload = (
  path: string,
  uploadingFile: UploadingFile,
  updateUploadProgress: (newFile: UploadingFile) => void
): Promise<CompleteUploadResponse> => {
  const {
    uploadingFile: { name, size },
  } = uploadingFile;
  const uploadData: NewUpload = {
    command: "newupload",
    filename: name,
    size: size,
  };
  return Axios.post<NewUploadResponse>(
    path,
    uploadData
  ).then(({ data: { uploadUrl } }) =>
    completeUpload(uploadUrl, uploadingFile, updateUploadProgress)
  );
};

const completeUpload = (
  path: string,
  uploadingFile: UploadingFile,
  updateUploadProgress: (newFile: UploadingFile) => void
): Promise<CompleteUploadResponse> => {
  const { uploadingFile: file } = uploadingFile;

  const source = CancelToken.source();
  axiosSourceMap.set(uploadingFile.localId, source);
  const token = source.token;

  const formData = new FormData();
  formData.append("file", file);

  return Axios.post<CompleteUploadResponse>(path, formData, {
    cancelToken: token,
    onUploadProgress: (progressEvent: ProgressEvent) => {
      updateUploadProgress({
        ...uploadingFile,
        uploadPercentage: Math.floor((progressEvent.loaded / file.size) * 100),
      });
    },
  }).then(({ data }) => data);
};

export const deleteUpload = (
  path: string,
  id: string
): Promise<RemoveEntries> => {
  const deleteData: DeleteUpload = { command: "delete", id: id };
  return Axios.post<RemoveEntries>(path, deleteData).then(({ data }) => data);
};

export const cancelUpload = (fileId: string, cancelCallback: () => void) => {
  const axiosSource = getAxiosSource(fileId);
  if (axiosSource) {
    axiosSource.cancel();
  } else {
    console.error("Failed to cancel the upload request.");
  }
  cancelCallback();
};

export const updateDuplicateMessage = (id: string, display: boolean) => {
  // The div id of all duplicate warning messages automatically follows
  // this format: its parent div id concatenated with "_duplicateWarningMessage"
  const duplicateMessageDiv = $(`#${id}_attachment_duplicate_warning`);
  if (duplicateMessageDiv) {
    if (display) {
      duplicateMessageDiv.css("display", "inline");
    } else {
      duplicateMessageDiv.css("display", "none");
    }
  }
};

export const updateCtrlErrorText = (ctrlId: string, text = "") => {
  const contElem = $(`DIV#${ctrlId} > DIV.control`);
  if (contElem) {
    if (text === "") {
      contElem.removeClass("ctrlinvalid");
    } else {
      contElem.addClass("ctrlinvalid");
    }
    contElem.find("P.ctrlinvalidmessage").text(text);
  }
};

export const buildMaxAttachmentWarning = (
  format: string,
  ...args: string[]
): string =>
  format.replace(/{(\d+)}/g, (match, number) =>
    typeof args[number] !== "undefined" ? args[number] : match
  );
