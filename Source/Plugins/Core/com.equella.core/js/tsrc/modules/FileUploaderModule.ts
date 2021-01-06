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
import Axios from "axios";
import { FileElement } from "../components/FileUploader";

interface AttachmentDuplicateInfo {
  displayWarningMessage: boolean;
  warningMessageWebId: string;
}

type UploadResponseType =
  | "updateentry"
  | "removeentries"
  | "uploadfailed"
  | "newuploadresponse";

interface BasicUploadResponse {
  response: UploadResponseType;
}

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

interface UpdateEntry extends BasicUploadResponse {
  entry: FileElement;
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

export const newUpload = (
  path: string,
  file: File
): Promise<CompleteUploadResponse> => {
  const uploadData: NewUpload = {
    command: "newupload",
    filename: file.name,
    size: file.size,
  };
  return Axios.post<NewUploadResponse>(
    path,
    uploadData
  ).then(({ data: { uploadUrl } }) => completeUpload(uploadUrl, file));
};

const completeUpload = (
  path: string,
  file: File
): Promise<CompleteUploadResponse> => {
  const formData = new FormData();
  formData.append("file", file);
  return Axios.post<CompleteUploadResponse>(path, formData).then(
    ({ data }) => data
  );
};

export const deleteUpload = (
  path: string,
  id: string
): Promise<RemoveEntries> => {
  const deleteData: DeleteUpload = { command: "delete", id: id };
  return Axios.post<RemoveEntries>(path, deleteData).then(({ data }) => data);
};
