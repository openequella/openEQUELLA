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
import { Grid, IconButton } from "@material-ui/core";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import CancelIcon from "@material-ui/icons/Cancel";
import DeleteIcon from "@material-ui/icons/Delete";
import * as React from "react";
import { ChangeEvent, useState } from "react";
import { useDropzone } from "react-dropzone";
import {
  cancelUpload,
  deleteUpload,
  generateLocalFile,
  generateUploadedFileComparator,
  generateUploadingFileComparator,
  isUploadedFile,
  upload,
  UploadedFile,
  UploadingFile,
} from "../modules/FileUploaderModule";
import {
  addElement,
  deleteElement,
  replaceElement,
} from "../util/ImmutableArrayUtil";
import { languageStrings } from "../util/langstrings";
import { UploadAction } from "./UploadActions";
import { UploadList } from "./UploadList";

/**
 * Data structure for all strings from the server.
 */
interface DialogStrings {
  scrapbook: string;
  delete: string;
  cancel: string;
  drop: string;
}

/**
 * Data structure matching the props server passes in
 */
export interface UniversalFileUploaderProps {
  /**
   * The root HTML Element under which this component gets rendered
   */
  elem: Element;
  /**
   * The root ID of an Attachment Wizard Control
   */
  ctrlId: string;
  /**
   * The function used to update the footer of Universal Resource Dialog
   */
  updateFooter: () => void;
  /**
   * The function used to import files from scrapbook
   */
  scrapBookOnClick?: () => void;
  /**
   * The URL used to initialise or delete an upload
   */
  commandUrl: string;
  /**
   * A number of language strings defined on server
   */
  strings: DialogStrings;
}

/**
 * Similar to InlineFileUploader, this component is also used for selecting and uploading local
 * files to server, but it should be ONLY used in the Legacy Universal Resource dialog at the moment.
 *
 * Also, upload restrictions configured on server do not apply.
 */
export const UniversalFileUploader = ({
  ctrlId,
  updateFooter,
  scrapBookOnClick = () => {},
  commandUrl,
  strings,
}: UniversalFileUploaderProps) => {
  const { noFileSelected } = languageStrings.fileUploader;
  const [uploadedFiles, setUploadedFiles] = useState<UploadedFile[]>([]);
  const [uploadingFiles, setUploadingFiles] = useState<UploadingFile[]>([]);

  const { getRootProps, getInputProps } = useDropzone({
    onDrop: (droppedFiles) => {
      droppedFiles.forEach((droppedFile) => {
        const localFile: UploadingFile = generateLocalFile(droppedFile);
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
        const onError = onUpload;
        const onSuccessful = (uploadedFile: UploadedFile) => {
          setUploadingFiles((prev) =>
            deleteElement(
              prev,
              generateUploadingFileComparator(localFile.localId),
              1
            )
          );
          setUploadedFiles((prev) => addElement(prev, uploadedFile));
        };

        upload(
          commandUrl,
          localFile,
          beforeUpload,
          onUpload,
          onSuccessful,
          onError,
          updateFooter
        );
      });
    },
  });

  const onDelete = (fileId: string) => {
    deleteUpload(commandUrl, fileId)
      .then(() => {
        setUploadedFiles(
          deleteElement(
            uploadedFiles,
            generateUploadedFileComparator(fileId),
            1
          )
        );
      })
      .finally(updateFooter);
  };

  const onCancel = (fileId: string) => {
    cancelUpload(fileId);
    setUploadingFiles(
      deleteElement(uploadingFiles, generateUploadingFileComparator(fileId), 1)
    );
  };

  const buildActions = (file: UploadedFile | UploadingFile): UploadAction[] => [
    isUploadedFile(file)
      ? {
          onClick: () => onDelete(file.fileEntry.id),
          text: strings.delete,
          icon: <DeleteIcon />,
        }
      : {
          onClick: () => onCancel(file.localId),
          text: strings.cancel,
          icon: <CancelIcon />,
        },
  ];

  return (
    <Grid container id="uploads" direction="column" spacing={1}>
      <Grid item className="uploadsprogress">
        <UploadList
          files={[...uploadedFiles, ...uploadingFiles]}
          buildActions={buildActions}
          noFileSelectedText={noFileSelected}
        />
      </Grid>
      <Grid item {...getRootProps()}>
        <input
          id={`${ctrlId}_fileUpload`}
          {...getInputProps()}
          onChange={(event: ChangeEvent<HTMLInputElement>) => {
            const fileInput: HTMLInputElement = event.target;
            const changeEventHandler = getInputProps().onChange;
            if (changeEventHandler) {
              changeEventHandler(event);
            }
            // Need to empty the input value to make 'Selenium - sendKeys' happy.
            fileInput.value = "";
          }}
        />
        <div className="filedrop">{strings.drop}</div>
      </Grid>
      <Grid item>
        <IconButton
          id={`${ctrlId}_filesFromScrapbookLink`}
          onClick={scrapBookOnClick}
          title={strings.scrapbook}
          color="primary"
        >
          <AddCircleIcon />
        </IconButton>
      </Grid>
    </Grid>
  );
};
