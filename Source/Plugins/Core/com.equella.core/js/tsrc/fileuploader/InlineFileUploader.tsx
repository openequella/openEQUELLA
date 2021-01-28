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
import { Grid } from "@material-ui/core";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import CancelIcon from "@material-ui/icons/Cancel";
import * as React from "react";
import { useEffect, useState } from "react";
import { useDropzone } from "react-dropzone";
import { LabelledIconButton } from "../components/LabelledIconButton";
import {
  AjaxFileEntry,
  cancelUpload,
  deleteUpload,
  generateLocalFile,
  generateUploadedFileComparator,
  generateUploadingFileComparator,
  isUploadedFile,
  updateCtrlErrorText,
  updateDuplicateMessage,
  upload,
  UploadedFile,
  UploadingFile,
} from "../modules/FileUploaderModule";
import {
  addElement,
  deleteElement,
  replaceElement,
} from "../util/ImmutableArrayUtil";
import { buildOEQServerString } from "../util/TextUtils";
import { UploadAction } from "./UploadActions";
import { UploadList } from "./UploadList";

/**
 * Data structure for all strings from the server.
 */
interface ControlStrings {
  edit: string;
  replace: string;
  delete: string;
  deleteConfirm: string;
  cancel: string;
  add: string;
  drop: string;
  none: string;
  preview: string;
  toomany: string;
  toomany_1: string;
}

/**
 * Data structure matching the props server passes in
 */
export interface InlineFileUploaderProps {
  /**
   * The root HTML Element under which this component gets rendered
   */
  elem: Element;
  /**
   * The root ID of an Attachment Wizard Control
   */
  ctrlId: string;
  /**
   * A list of files that have been uploaded to server
   */
  entries: AjaxFileEntry[];
  /**
   * The number of maximum attachments
   */
  maxAttachments: number | null;
  /**
   * Whether uploading files is allowed
   */
  canUpload: boolean;
  /**
   * The function used to open the Universal Resource Dialog to update a uploaded file
   * @param replaceUuid The ID of a file that is the replacement of a selected file
   * @param editUuid The ID of a selected file
   */
  dialog: (replaceUuid: string, editUuid: string) => void;
  /**
   * Whether editing files is allowed
   */
  editable: boolean;
  /**
   * The URL used to initialise or delete an upload
   */
  commandUrl: string;
  /**
   * A number of language strings defined on server
   */
  strings: ControlStrings;
  /**
   * The function used to reload the Wizard state
   */
  reloadState: () => void;
}

/**
 * This component is used for selecting and uploading local files to server.
 * It supports 'Drag and Drop', multiple selections, and common actions such as
 * editing, replacing and deleting uploaded files.
 *
 * Also, upload restrictions configured on server are applied.
 *
 * However, this component should *only* be used in the context of Legacy Wizard pages
 * at the moment because required props are only available via server side rendering (Sections).
 */
export const InlineFileUploader = ({
  ctrlId,
  entries,
  maxAttachments,
  canUpload: canUploadFile,
  dialog: openDialog,
  editable,
  commandUrl,
  strings,
  reloadState,
}: InlineFileUploaderProps) => {
  const [uploadedFiles, setUploadedFiles] = useState<UploadedFile[]>(
    entries.map((entry) => ({
      fileEntry: entry,
      status: "uploaded",
    }))
  );
  const [uploadingFiles, setUploadingFiles] = useState<UploadingFile[]>([]);
  const [attachmentCount, setAttachmentCount] = useState<number>(
    entries.length
  );
  const [showDuplicateWarning, setShowDuplicateWarning] = useState<
    boolean | undefined
  >(undefined);

  const { getRootProps, getInputProps } = useDropzone({
    onDrop: (droppedFiles) => {
      setAttachmentCount(attachmentCount + droppedFiles.length);

      droppedFiles.forEach((droppedFile) => {
        const localFile = generateLocalFile(droppedFile);
        const beforeUpload = () =>
          setUploadingFiles((prev) => addElement(prev, localFile));
        const onUpload = (updatedFile: UploadingFile) =>
          setUploadingFiles((prev) =>
            replaceElement(
              prev,
              generateUploadingFileComparator(localFile.localId),
              updatedFile
            )
          );
        // What onError essentially does is the same as what onUpload does - update the Uploading list.
        const onError = onUpload;
        const onSuccessful = (
          uploadedFile: UploadedFile,
          displayWarningMessage = false
        ) => {
          setUploadingFiles((prev) =>
            deleteElement(
              prev,
              generateUploadingFileComparator(localFile.localId),
              1
            )
          );
          setUploadedFiles((prev) => addElement(prev, uploadedFile));
          setShowDuplicateWarning(displayWarningMessage);
        };

        upload(
          commandUrl,
          localFile,
          beforeUpload,
          onUpload,
          onSuccessful,
          onError,
          reloadState
        );
      });
    },
  });

  /**
   * Update whether to show the attachment duplicate warning.
   * In the first render where 'showDuplicateWarning' is undefined, do nothing
   * because the warning message is controlled by server.
   */
  useEffect(() => {
    if (showDuplicateWarning !== undefined) {
      updateDuplicateMessage(ctrlId, showDuplicateWarning);
    }
  }, [showDuplicateWarning, ctrlId]);

  /**
   * Update the error text for exceeding maximum number of attachments.
   * When there is a limit, check if 'fileCount' is greater than 'maxAttachments'.
   * If yes, depending on whether the limit is 1 or more than 1, use a proper format
   * provided by server to update the error text.
   * If no, set the text to an empty string which will remove the error.
   * Lastly, do nothing when there is no limit.
   */
  useEffect(() => {
    if (maxAttachments) {
      const remainingQuota = attachmentCount - maxAttachments;
      const getMaxAttachmentWarningText = () =>
        maxAttachments > 1
          ? buildOEQServerString(
              strings.toomany,
              maxAttachments,
              remainingQuota
            )
          : buildOEQServerString(strings.toomany_1, remainingQuota);
      const warningText =
        attachmentCount > maxAttachments ? getMaxAttachmentWarningText() : "";

      updateCtrlErrorText(ctrlId, warningText);
    }
  }, [
    attachmentCount,
    ctrlId,
    maxAttachments,
    strings.toomany,
    strings.toomany_1,
  ]);

  const onEdit = (fileId: string) => openDialog("", fileId);

  const onReplace = (fileId: string) => openDialog(fileId, "");

  const onDelete = (fileId: string) => {
    const confirmDelete = window.confirm(strings.deleteConfirm);
    if (confirmDelete) {
      deleteUpload(commandUrl, fileId)
        .then(({ attachmentDuplicateInfo }) => {
          setUploadedFiles(
            deleteElement(
              uploadedFiles,
              generateUploadedFileComparator(fileId),
              1
            )
          );
          setShowDuplicateWarning(
            attachmentDuplicateInfo?.displayWarningMessage ?? false
          );
          setAttachmentCount(attachmentCount - 1);
        })
        .finally(reloadState);
    }
  };

  const onCancel = (fileId: string) => {
    cancelUpload(fileId);
    setUploadingFiles(
      deleteElement(uploadingFiles, generateUploadingFileComparator(fileId), 1)
    );
    setAttachmentCount(attachmentCount - 1);
  };

  /**
   * Build three text buttons for UploadedFile or one icon button for UploadingFile.
   */
  const buildActions = (file: UploadedFile | UploadingFile): UploadAction[] =>
    isUploadedFile(file)
      ? [
          {
            onClick: () => onEdit(file.fileEntry.id),
            text: strings.edit,
          },
          {
            onClick: () => onReplace(file.fileEntry.id),
            text: strings.replace,
          },
          {
            onClick: () => onDelete(file.fileEntry.id),
            text: strings.delete,
          },
        ]
      : [
          {
            onClick: () => onCancel(file.localId),
            text: strings.cancel,
            icon: <CancelIcon />,
          },
        ];

  return (
    <Grid
      container
      id={`${ctrlId}universalresources`}
      className="universalresources"
      direction="column"
    >
      <Grid item>
        <UploadList
          files={[...uploadedFiles, ...uploadingFiles]}
          buildActions={buildActions}
          noFileSelectedText={strings.none}
        />
      </Grid>

      {editable &&
        (maxAttachments === null || attachmentCount < maxAttachments) && (
          <Grid item>
            <LabelledIconButton
              buttonText={strings.add}
              icon={<AddCircleIcon />}
              id={`${ctrlId}_addLink`}
              onClick={() => openDialog("", "")}
              color="primary"
            />
            {canUploadFile && (
              <div {...getRootProps({ className: "dropzone" })}>
                <input id={`${ctrlId}_fileUpload_file`} {...getInputProps()} />
                <div className="filedrop">{strings.drop}</div>
              </div>
            )}
          </Grid>
        )}
    </Grid>
  );
};
