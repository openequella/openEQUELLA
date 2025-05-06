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
import { Grid } from "@mui/material";
import AddCircleIcon from "@mui/icons-material/AddCircle";
import CancelIcon from "@mui/icons-material/Cancel";
import DeleteIcon from "@mui/icons-material/Delete";
import * as React from "react";
import { ChangeEvent, useState } from "react";
import { useDropzone } from "react-dropzone";
import { getRenderData } from "../AppConfig";
import { LabelledIconButton } from "../components/LabelledIconButton";
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
              updatedFile,
            ),
          );
        const onError = onUpload;
        const onSuccessful = (uploadedFile: UploadedFile) => {
          setUploadingFiles((prev) =>
            deleteElement(
              prev,
              generateUploadingFileComparator(localFile.localId),
              1,
            ),
          );
          setUploadedFiles((prev) => addElement(prev, uploadedFile));
          updateFooter();
        };

        upload(
          commandUrl,
          localFile,
          beforeUpload,
          onUpload,
          onSuccessful,
          onError,
        );
      });
    },
  });

  const onDelete = (file: UploadedFile) => {
    const { id } = file.fileEntry;
    const onSuccessful = () => {
      setUploadedFiles(
        deleteElement(uploadedFiles, generateUploadedFileComparator(id), 1),
      );
      updateFooter();
    };
    const onError = (file: UploadedFile) => {
      setUploadedFiles(
        replaceElement(uploadedFiles, generateUploadedFileComparator(id), file),
      );
    };
    deleteUpload(commandUrl, file, onSuccessful, onError);
  };

  const onCancel = (fileId: string) => {
    cancelUpload(fileId);
    setUploadingFiles(
      deleteElement(uploadingFiles, generateUploadingFileComparator(fileId), 1),
    );
  };

  const buildActions = (file: UploadedFile | UploadingFile): UploadAction[] => [
    isUploadedFile(file)
      ? {
          onClick: () => onDelete(file),
          text: strings.delete,
          icon: <DeleteIcon />,
        }
      : {
          onClick: () => onCancel(file.localId),
          text: strings.cancel,
          icon: <CancelIcon />,
        },
  ];

  // Build an Icon button for adding Scrapbooks. In Old UI, this is achieved by legacy CSS styles.
  // In New UI, use component LabelledIconButton. The reason for using '<a>' for Old UI is because
  // the legacy style 'add' only applies to '<a>'.
  const AddScrapBookButton = () => {
    const commonProps = {
      id: `${ctrlId}_filesFromScrapbookLink`,
      onClick: scrapBookOnClick,
    };
    return getRenderData()?.newUI ? (
      <LabelledIconButton
        icon={<AddCircleIcon />}
        buttonText={strings.scrapbook}
        color="primary"
        {...commonProps}
      />
    ) : (
      <a className="add" {...commonProps}>
        {strings.scrapbook}
      </a>
    );
  };

  return (
    <Grid container id="uploads" direction="column" spacing={1}>
      <Grid className="uploadsprogress">
        <UploadList
          files={[...uploadedFiles, ...uploadingFiles]}
          buildActions={buildActions}
          noFileSelectedText={noFileSelected}
        />
      </Grid>
      <Grid {...getRootProps()}>
        <input
          id={`${ctrlId}_fileUpload`}
          {...getInputProps({
            onChange: (event: ChangeEvent<HTMLInputElement>) => {
              const fileInput: HTMLInputElement = event.target;
              const changeEventHandler = getInputProps().onChange;
              if (changeEventHandler) {
                changeEventHandler(event);
              }
              // Need to empty the input value to make 'Selenium - sendKeys' happy.
              fileInput.value = "";
            },
          })}
        />
        <div className="filedrop">{strings.drop}</div>
      </Grid>
      <Grid>
        <AddScrapBookButton />
      </Grid>
    </Grid>
  );
};
