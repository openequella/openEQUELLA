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
import {
  Button,
  createGenerateClassName,
  Grid,
  LinearProgress,
  StylesProvider,
  ThemeProvider,
} from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import * as React from "react";
import { useState } from "react";
import * as ReactDOM from "react-dom";
import { useDropzone } from "react-dropzone";
import {
  cancelUpload,
  completeUpload,
  deleteUpload,
  generateLocalFile,
  isUpdateEntry,
  isUploadedFile,
  newUpload,
  NewUploadResponse,
  UpdateEntry,
  UploadedFile,
  UploadFailed,
  UploadingFile,
} from "../modules/FileUploaderModule";
import { oeqTheme } from "../theme";
import {
  addElement,
  deleteElement,
  replaceElement,
} from "../util/ImmutableArrayUtil";
import { languageStrings } from "../util/langstrings";
import { FileActionLink } from "./FileUploaderActionLink";

/**
 * Data structure for all texts server passes in
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
interface UniversalFileUploaderProps {
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

const useStyles = makeStyles({
  // In order to show the custom progress bar background color, override the MUI LinearProgress styles
  // to make its background color transparent.
  barColorPrimary: {
    backgroundColor: "transparent",
  },
});

const UniversalFileUploader = ({
  ctrlId,
  updateFooter,
  scrapBookOnClick,
  commandUrl,
  strings,
}: UniversalFileUploaderProps) => {
  const classes = useStyles();
  const { browse, noFileSelected } = languageStrings.fileUploader;
  const [uploadedFiles, setUploadedFiles] = useState<UploadedFile[]>([]);
  const [uploadingFiles, setUploadingFiles] = useState<UploadingFile[]>([]);

  const { getRootProps, getInputProps } = useDropzone({
    onDrop: (droppedFiles) => {
      const updateUploadProgress = (updatedFile: UploadingFile) => {
        setUploadingFiles((prev) =>
          replaceElement(
            prev,
            (file) => file.localId === updatedFile.localId,
            updatedFile
          )
        );
      };
      droppedFiles.forEach((droppedFile) => {
        const localFile: UploadingFile = generateLocalFile(droppedFile);
        setUploadingFiles((prev) => addElement(prev, localFile));
        newUpload(commandUrl, localFile)
          .then(({ uploadUrl }: NewUploadResponse) =>
            completeUpload(uploadUrl, localFile, updateUploadProgress)
          )
          .then((uploadResponse: UpdateEntry | UploadFailed) => {
            if (isUpdateEntry(uploadResponse)) {
              const uploadedFile: UploadedFile = {
                fileEntry: uploadResponse.entry,
                status: "uploaded",
              };
              setUploadingFiles((prev) =>
                deleteElement(
                  prev,
                  (file: UploadingFile) => file.localId === localFile.localId,
                  1
                )
              );
              setUploadedFiles((prev) => addElement(prev, uploadedFile));
            }
          })
          .finally(updateFooter);
      });
    },
  });

  const onDelete = (fileId: string) => {
    deleteUpload(commandUrl, fileId)
      .then((_) => {
        setUploadedFiles(
          deleteElement(
            uploadedFiles,
            ({ fileEntry: { id } }: UploadedFile) => id === fileId,
            1
          )
        );
      })
      .finally(updateFooter);
  };

  const onCancel = (fileId: string) => {
    cancelUpload(fileId).then(() => {
      setUploadingFiles(
        deleteElement(uploadingFiles, ({ localId }) => localId === fileId, 1)
      );
    });
  };

  const buildFileList = [...uploadingFiles, ...uploadedFiles].map((file) => {
    const fileListProps = isUploadedFile(file)
      ? {
          progressBarProps: {
            className: "progress-bar-inner complete",
            value: 100,
            classes: {
              barColorPrimary: classes.barColorPrimary,
            },
          },
          progressPercentage: "",
          binIconProps: {
            onClick: () => onDelete(file.fileEntry.id),
            text: strings.delete,
          },
        }
      : {
          progressBarProps: {
            className: "progress-bar-inner",
            value: file.uploadPercentage,
          },
          progressPercentage: `${file.uploadPercentage}%`,
          binIconProps: {
            onClick: () => onCancel(file.localId),
            text: strings.cancel,
          },
        };
    return (
      <Grid container className="file-upload" spacing={2} alignItems="center">
        <Grid item className="file-name" xs={8}>
          {file.fileEntry.name}
        </Grid>
        <Grid
          item
          container
          className="file-upload-progress"
          xs={3}
          spacing={2}
          alignItems="center"
        >
          <Grid item xs={9}>
            <LinearProgress
              variant="determinate"
              {...fileListProps.progressBarProps}
            />
          </Grid>
          <Grid item xs={3}>
            {fileListProps.progressPercentage}
          </Grid>
        </Grid>
        <Grid item xs={1}>
          <FileActionLink
            {...fileListProps.binIconProps}
            showText={false}
            customClass="unselect"
          />
        </Grid>
      </Grid>
    );
  });
  return (
    <div id="uploads">
      <div className="uploadsprogress">{buildFileList}</div>
      <div {...getRootProps()}>
        <div className="customfile focus">
          <Button
            className="customfile-button focus btn btn-equella btn-mini"
            onClick={(e) => e.preventDefault()}
          >
            {browse}
          </Button>
          <span className="customfile-feedback">{noFileSelected}</span>
          <input
            id={`${ctrlId}_fileUpload`}
            className="customfile-input"
            {...getInputProps()}
          />
        </div>
        <div className="filedrop">{strings.drop}</div>
      </div>
    </div>
  );
};

export const render = (props: UniversalFileUploaderProps) => {
  const generateClassName = createGenerateClassName({
    productionPrefix: "oeq-ufu",
    seed: "oeq-ufu",
  });
  ReactDOM.render(
    <StylesProvider generateClassName={generateClassName} injectFirst>
      <ThemeProvider theme={oeqTheme}>
        <UniversalFileUploader {...props} />
      </ThemeProvider>
    </StylesProvider>,
    props.elem
  );
};
