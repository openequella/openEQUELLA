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
  Typography,
} from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import Axios from "axios";
import * as React from "react";
import { ChangeEvent, useState } from "react";
import * as ReactDOM from "react-dom";
import { useDropzone } from "react-dropzone";
import { getRenderData } from "../AppConfig";
import {
  cancelUpload,
  deleteUpload,
  generateLocalFile,
  isUpdateEntry,
  isUploadedFile,
  newUpload,
  UpdateEntry,
  UploadedFile,
  UploadFailed,
  UploadingFile,
} from "../modules/FileUploaderModule";
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
  scrapBookOnClick = () => {},
  commandUrl,
  strings,
}: UniversalFileUploaderProps) => {
  const classes = useStyles();
  const { browse, noFileSelected } = languageStrings.fileUploader;
  const [uploadedFiles, setUploadedFiles] = useState<UploadedFile[]>([]);
  const [uploadingFiles, setUploadingFiles] = useState<UploadingFile[]>([]);

  const updateUploadingFile = (updatedFile: UploadingFile) => {
    setUploadingFiles((prev) =>
      replaceElement(
        prev,
        (file) => file.localId === updatedFile.localId,
        updatedFile
      )
    );
  };

  const { getRootProps, getInputProps } = useDropzone({
    onDrop: (droppedFiles) => {
      droppedFiles.forEach((droppedFile) => {
        const localFile: UploadingFile = generateLocalFile(droppedFile);
        setUploadingFiles((prev) => addElement(prev, localFile));

        newUpload(commandUrl, localFile, updateUploadingFile)
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
            } else {
              throw new Error(uploadResponse.reason);
            }
          })
          .catch((error: Error) => {
            if (!Axios.isCancel(error)) {
              updateUploadingFile({
                ...localFile,
                status: "failed",
                failedReason: error.message,
              });
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

  const onCancel = (file: UploadingFile) => {
    cancelUpload(file);
    setUploadingFiles(
      deleteElement(
        uploadingFiles,
        ({ localId }) => localId === file.localId,
        1
      )
    );
  };

  const buildFileList = () => {
    const fileName = (file: UploadedFile | UploadingFile) => (
      <Typography
        variant="subtitle1"
        style={file.status !== "uploading" ? { fontWeight: 500 } : undefined}
      >
        {file.fileEntry.name}
      </Typography>
    );

    const progressBar = (file: UploadedFile | UploadingFile) => {
      const progressBarProps = isUploadedFile(file)
        ? {
            className: "progress-bar-inner complete",
            value: 100,
            classes: {
              barColorPrimary: classes.barColorPrimary,
            },
          }
        : {
            className: "progress-bar-inner",
            value: file.uploadPercentage,
          };
      return (
        <Grid item xs={9}>
          <LinearProgress variant="determinate" {...progressBarProps} />
        </Grid>
      );
    };

    const uploadPercentage = (file: UploadedFile | UploadingFile) =>
      isUploadedFile(file) ? "" : `${file.uploadPercentage}%`;

    const binIcon = (file: UploadedFile | UploadingFile) => {
      const binIconProps = isUploadedFile(file)
        ? {
            onClick: () => onDelete(file.fileEntry.id),
            text: strings.delete,
          }
        : {
            onClick: () => onCancel(file),
            text: strings.cancel,
          };
      return (
        <FileActionLink
          {...binIconProps}
          showText={false}
          customClass="unselect"
        />
      );
    };
    return [...uploadedFiles, ...uploadingFiles].map((file) => (
      <Grid container className="file-upload" spacing={2} alignItems="center">
        <Grid item className="file-name" xs={7}>
          {fileName(file)}
        </Grid>
        {file.status !== "failed" ? (
          <>
            <Grid item className="file-upload-progress" xs={3}>
              {progressBar(file)}
            </Grid>
            <Grid item xs={1}>
              {uploadPercentage(file)}
            </Grid>
          </>
        ) : (
          <Grid item xs={4} className="ctrlinvalidmessage">
            <Typography variant="subtitle1" color="error">
              {file.failedReason}
            </Typography>
          </Grid>
        )}
        <Grid item xs={1}>
          {binIcon(file)}
        </Grid>
      </Grid>
    ));
  };
  return (
    <div id="uploads">
      <div className="uploadsprogress">{buildFileList()}</div>
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
        </div>
        <div className="filedrop">{strings.drop}</div>
      </div>
      <FileActionLink
        id={`${ctrlId}_filesFromScrapbookLink`}
        onClick={scrapBookOnClick}
        text={strings.scrapbook}
        customClass="add"
      />
    </div>
  );
};

export const render = (props: UniversalFileUploaderProps) => {
  if (getRenderData()?.newUI) {
    const generateClassName = createGenerateClassName({
      productionPrefix: "oeq-ufu",
      seed: "oeq-ufu",
    });

    import("../theme/index").then(({ oeqTheme }) => {
      ReactDOM.render(
        <StylesProvider generateClassName={generateClassName}>
          <ThemeProvider theme={oeqTheme}>
            <UniversalFileUploader {...props} />
          </ThemeProvider>
        </StylesProvider>,
        props.elem
      );
    });
  } else {
    ReactDOM.render(<UniversalFileUploader {...props} />, props.elem);
  }
};
