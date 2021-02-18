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
import { sprintf } from "sprintf-js";
import { v4 } from "uuid";
import { languageStrings } from "../util/langstrings";

const { CancelToken } = Axios;

/**
 * Data structure matching server side type 'AjaxFileEntry'
 */
export interface AjaxFileEntry {
  /**
   * A file's UUID generated on server
   */
  id: string;
  /**
   * A file's name
   */
  name: string;
  /**
   * A link used to access the file
   */
  link: string;
  /**
   * True if previewing the file is allowed
   */
  preview: boolean;
  /**
   * True if editing the file is allowed
   */
  editable: boolean;
  /**
   * Files that are children of this file.
   */
  children: AjaxFileEntry[];
}

/**
 * Data structure for files that have been uploaded
 */
export interface UploadedFile {
  /**
   * An object of AjaxFileEntry returned from server
   */
  fileEntry: AjaxFileEntry;
  /**
   * Status indicating the upload is successful
   */
  status: "uploaded";
  /**
   * Text describing why fail to delete this file
   */
  errorMessage?: string;
  /**
   * Whether to show this file indented. Typically, true for child AjaxFileEntry.
   */
  indented: boolean;
}

/**
 * Data structure for files that are being uploaded
 */
export interface UploadingFile {
  /**
   * A temporary unique ID generated on client
   */
  localId: string;
  /**
   * A local file added through the File Uploader
   */
  fileEntry: File;
  /**
   * Status indicating the upload is in progress
   */
  status: "uploading" | "failed";
  /**
   * A number representing the amount of upload already performed
   */
  uploadPercentage: number;
  /**
   * Text describing errors that happen when the upload is in progress
   */
  errorMessage?: string;
}

/**
 * A type guard used to check if an object is UploadedFile.
 * @param file An object that is either a UploadedFile or a UploadingFile
 */
export const isUploadedFile = (
  file: UploadedFile | UploadingFile
): file is UploadedFile => file.status === "uploaded";

/**
 * Data structure matching server side type 'AttachmentDuplicateInfo'
 */
interface AttachmentDuplicateInfo {
  /**
   * Whether to display the duplicate attachment warning
   */
  displayWarningMessage: boolean;
  /**
   * The Attachment Control's root ID
   */
  warningMessageWebId: string;
}

interface BasicUploadCommand {
  command: "newupload" | "delete";
}

/**
 * The structure for POST data used to initialise an upload
 */
interface NewUpload extends BasicUploadCommand {
  /**
   * The file name
   */
  filename: string;
  /**
   * The file size
   */
  size: number;
}

/**
 * The structure for POST data used to delete an upload
 */
interface DeleteUpload extends BasicUploadCommand {
  /**
   * The file ID generated on either server or client
   */
  id: string;
}

/**
 * String literal type for the response text
 */
type UploadResponseType =
  | "updateentry"
  | "removeentries"
  | "uploadfailed"
  | "newuploadresponse";

interface BasicUploadResponse {
  response: UploadResponseType;
}

/**
 * Data structure matching server side type UpdateEntry which indicates an upload is successful
 */
export interface UpdateEntry extends BasicUploadResponse {
  /**
   * Information of an uploaded file
   */
  entry: AjaxFileEntry;
  /**
   * Information of attachment duplicates
   */
  attachmentDuplicateInfo: AttachmentDuplicateInfo | null;
}

/**
 * Data structure matching server side type UploadFailed which indicates an upload is failed
 */
export interface UploadFailed extends BasicUploadResponse {
  /**
   * Text describing why the uploading is failed
   */
  reason: string;
}

/**
 * Data structure matching server side type RemoveEntries which indicates deletion of an upload is successful
 */
export interface RemoveEntries extends BasicUploadResponse {
  /**
   * An array of IDs of removed files
   * It's unsure why server uses a collection type for an individual entry.
   */
  ids: string[];
  /**
   * Information of attachment duplicates
   */
  attachmentDuplicateInfo: AttachmentDuplicateInfo | null;
}

/**
 * Data structure matching server side type NewUploadResponse which provides information of an initialised upload
 */
export interface NewUploadResponse extends BasicUploadResponse {
  /**
   * A POST URL used to perform an upload
   */
  uploadUrl: string;
  /**
   * A server generated ID for an upload
   */
  id: string;
  /**
   * A server generated unique file name
   */
  name: string;
}

/**
 * A type guard used to check if an object is UpdateEntry.
 * @param uploadResponse An object that is either a UpdateEntry or a UploadFailed
 */
export const isUpdateEntry = (
  uploadResponse: BasicUploadResponse
): uploadResponse is UpdateEntry => uploadResponse.response === "updateentry";

const isUploadFailed = (
  uploadResponse: BasicUploadResponse
): uploadResponse is UploadFailed => uploadResponse.response === "uploadfailed";

/**
 * A map where the key is each UploadingFile's localId and the value is a CancelTokenSource
 */
const axiosSourceMap: Map<string, CancelTokenSource> = new Map();

/**
 * Due to the support of uploading multiple files at once, multiple Axios CancelToken
 * sources are generated for each request. In order to cancel any request independently,
 * call this function to access a CancelToken source.
 *
 * @param id The localId of each UploadingFile
 */
export const getAxiosSource = (id: string) => axiosSourceMap.get(id);

/**
 * Send a POST request to initialise an upload.
 * @param path The request URL
 * @param uploadingFile The file to be uploaded
 * @param updateUploadProgress
 */
export const newUpload = (
  path: string,
  uploadingFile: UploadingFile,
  updateUploadProgress: (file: UploadingFile) => void
): Promise<UpdateEntry | UploadFailed> => {
  const {
    fileEntry: { name, size },
  } = uploadingFile;
  const uploadData: NewUpload = {
    command: "newupload",
    filename: name,
    size: size,
  };
  return Axios.post<NewUploadResponse | UploadFailed>(
    path,
    uploadData
  ).then(({ data }) =>
    isUploadFailed(data)
      ? data
      : doUpload(data.uploadUrl, uploadingFile, updateUploadProgress)
  );
};

/**
 * Send a POST request to complete an upload. This function must be called after
 * initialising an upload by 'newUpload'.
 *
 * @param path The request URL
 * @param uploadingFile A file to be uploaded
 * @param updateUploadProgress A Function fired during upload to update the ProgressBar
 */
const doUpload = (
  path: string,
  uploadingFile: UploadingFile,
  updateUploadProgress: (file: UploadingFile) => void
): Promise<UpdateEntry | UploadFailed> => {
  const { fileEntry } = uploadingFile;

  // Create a new CancelToken source and add the source to the map.
  const source = CancelToken.source();
  axiosSourceMap.set(uploadingFile.localId, source);
  const token = source.token;
  return Axios.post<UpdateEntry | UploadFailed>(path, fileEntry, {
    // IMPORTANT! Must specify the file type. There is legacy server side code that consumes
    // the InputStream of request body if Content-Type is `application/x-www-form-urlencoded`,
    // which is the default value. That will result in uploaded files being empty.
    headers: {
      "Content-Type": fileEntry.type || "application/octet-stream",
    },
    cancelToken: token,
    onUploadProgress: (progressEvent: ProgressEvent) => {
      const uploadPercentage = Math.floor(
        (progressEvent.loaded / fileEntry.size) * 100
      );

      updateUploadProgress({
        ...uploadingFile,
        uploadPercentage: uploadPercentage > 100 ? 100 : uploadPercentage,
      });
    },
  }).then(({ data }) => data);
};

/**
 * Upload a file to oEQ server.
 * @param uploadURL The upload URL provided by server
 * @param file The file to be uploaded
 * @param beforeUpload The function fired before doing an upload
 * @param onUpload The function fired when an upload is in progress
 * @param onSuccessful The function fired when an upload is successfully completed
 * @param onError The function fired when an upload fails
 */
export const upload = (
  uploadURL: string,
  file: UploadingFile,
  beforeUpload: () => void,
  onUpload: (file: UploadingFile) => void,
  onSuccessful: (
    uploadedFile: UploadedFile,
    displayWarningMessage?: boolean
  ) => void,
  onError: (file: UploadingFile) => void
) => {
  beforeUpload();
  newUpload(uploadURL, file, onUpload)
    .then((uploadResponse: UpdateEntry | UploadFailed) => {
      if (isUpdateEntry(uploadResponse)) {
        const { entry, attachmentDuplicateInfo } = uploadResponse;
        const uploadedFile: UploadedFile = {
          fileEntry: entry,
          status: "uploaded",
          indented: false,
        };
        onSuccessful(
          uploadedFile,
          attachmentDuplicateInfo?.displayWarningMessage ?? false
        );
      } else {
        throw new Error(uploadResponse.reason);
      }
    })
    .catch((error: Error) => {
      // There is no need to handle Axios cancel error.
      if (!Axios.isCancel(error)) {
        onError({
          ...file,
          status: "failed",
          errorMessage: error.message,
        });
      }
    });
};

/**
 * Send a POST request to remove an uploaded file.
 * @param path The request URL
 * @param file The file to be deleted
 * @param onSuccessful The function fired when deleting a file is completed
 * @param onError The function fired when deleting a file is failed
 * @param deleteConfirmText Text to show in the confirm dialog
 */
export const deleteUpload = (
  path: string,
  file: UploadedFile,
  onSuccessful: (displayWarningMessage?: boolean) => void,
  onError: (file: UploadedFile) => void,
  deleteConfirmText?: string
) => {
  const { id, name } = file.fileEntry;
  const deleteConfirmed =
    (deleteConfirmText && window.confirm(deleteConfirmText)) ||
    !deleteConfirmText;
  if (deleteConfirmed) {
    const deleteData: DeleteUpload = { command: "delete", id: id };
    Axios.post<RemoveEntries>(path, deleteData)
      .then(({ data: { attachmentDuplicateInfo } }) => {
        onSuccessful(attachmentDuplicateInfo?.displayWarningMessage);
      })
      .catch((error: Error) => {
        onError({
          ...file,
          errorMessage: sprintf(
            languageStrings.fileUploader.failedToDelete,
            name,
            error.message
          ),
        });
      });
  }
};

/**
 * Cancel an upload request.
 * Returns a resolved promise for successful cancel or a rejected promise otherwise.
 * @param id The file ID which must be the client generated ID
 */
export const cancelUpload = (id: string) => {
  const axiosSource = getAxiosSource(id);
  if (axiosSource) {
    axiosSource.cancel();
    axiosSourceMap.delete(id);
  }
};

/**
 * Update whether to show the attachment duplicate warning. Because the warning message UI
 * is created on server side through 'ftl' template, we have to use jQuery to update the DOM.
 *
 * @param ctrlId The root ID of an Attachment Wizard Control
 * @param display True to show the warning
 */
export const updateDuplicateMessage = (ctrlId: string, display: boolean) => {
  const duplicateMessageDiv = document.querySelector(
    `#${ctrlId}_attachment_duplicate_warning`
  );
  if (duplicateMessageDiv) {
    if (display) {
      duplicateMessageDiv.setAttribute("style", "display: inline");
    } else {
      duplicateMessageDiv.setAttribute("style", "display: none");
    }
  }
};

/**
 * Update the error text of an Attachment Wizard Control. An example of calling this
 * function is when the number of uploaded/uploading files is more than the maximum
 * number of attachments. Similar to 'updateDuplicateMessage', jQuery is used to help
 * update DOM.
 *
 * @param ctrlId The root ID of an Attachment Wizard Control
 * @param text The text describing the error
 */
export const updateCtrlErrorText = (ctrlId: string, text: string) => {
  const contElem = document.querySelector(`DIV#${ctrlId} > DIV.control`);
  if (contElem) {
    if (text === "") {
      contElem.classList.remove("ctrlinvalid");
    } else {
      contElem.classList.add("ctrlinvalid");
    }

    const invalidMessage = contElem.querySelector("P.ctrlinvalidmessage");
    if (invalidMessage) {
      invalidMessage.textContent = text;
    }
  }
};

export const generateLocalFile = (file: File): UploadingFile => ({
  localId: v4(),
  fileEntry: file,
  status: "uploading",
  uploadPercentage: 0,
});

/**
 * Provide a function used to compare an UploadingFile's ID and a client-generated ID.
 * @param comparedFileId A file ID which must be generated on client side.
 */
export const generateUploadingFileComparator = (comparedFileId: string) => (
  file: UploadingFile
) => file.localId === comparedFileId;

/**
 * Provide a function used to compare an UploadedFile's ID and a server-generated ID.
 * @param comparedFileId A file ID which must be generated on server side.
 */
export const generateUploadedFileComparator = (comparedFileId: string) => (
  file: UploadedFile
) => file.fileEntry.id === comparedFileId;
