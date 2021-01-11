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
import { v4 } from "uuid";
import {
  cancelUpload,
  CompleteUploadResponse,
  deleteUpload,
  isUpdateEntry,
  isUploadedFile,
  newUpload,
  UploadedFile,
  UploadingFile,
} from "../modules/FileUploaderModule";
import { oeqTheme } from "../theme";
import {
  addElement,
  deleteElement,
  replaceElement,
} from "../util/ImmutableArrayUtil";
import { FileActionLink } from "./FileUploaderActionLink";

interface DialogStrings {
  scrapbook: string;
  delete: string;
  cancel: string;
  drop: string;
}

interface UniversalFileUploaderProps {
  elem: Element;
  ctrlId: string;
  updateFooter: () => void;
  scrapBookOnClick?: () => void;
  commandUrl: string;
  strings: DialogStrings;
}

const useStyles = makeStyles({
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
  const [uploadedFiles, setUploadedFiles] = useState<UploadedFile[]>([]);
  const [uploadingFiles, setUploadingFiles] = useState<UploadingFile[]>([]);

  const updateUploadProgress = (updatedFile: UploadingFile) => {
    setUploadingFiles((prev) =>
      replaceElement(
        prev,
        (file: UploadingFile) => file.localId === updatedFile.localId,
        updatedFile
      )
    );
  };

  const { getRootProps, getInputProps } = useDropzone({
    onDrop: (droppedFiles) => {
      droppedFiles.forEach((droppedFile) => {
        const localId = v4();
        const localFile: UploadingFile = {
          localId: localId,
          uploadingFile: droppedFile,
          status: "uploading",
          uploadPercentage: 0,
        };
        setUploadingFiles((prev) => addElement<UploadingFile>(prev, localFile));
        newUpload(commandUrl, localFile, updateUploadProgress)
          .then((res: CompleteUploadResponse) => {
            if (isUpdateEntry(res)) {
              const uploadedFile: UploadedFile = {
                uploadedFile: res.entry,
                status: "uploaded",
              };
              setUploadingFiles(
                deleteElement(
                  uploadingFiles,
                  (file: UploadingFile) => file.localId === localId,
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
  const onCancel = (fileId: string) => {
    const cancelCallback = () => {
      setUploadingFiles((prev) =>
        deleteElement(prev, ({ localId }) => localId === fileId, 1)
      );
    };
    cancelUpload(fileId, cancelCallback);
  };

  const onDelete = (fileId: string) => {
    deleteUpload(commandUrl, fileId)
      .then(({ ids, attachmentDuplicateInfo }) => {
        setUploadedFiles(
          deleteElement(
            uploadedFiles,
            ({ uploadedFile: { id } }: UploadedFile) => id === fileId,
            1
          )
        );
      })
      .finally(updateFooter);
  };

  const fileList = [...uploadingFiles, ...uploadedFiles].map((file) => {
    const fileName = () =>
      isUploadedFile(file) ? file.uploadedFile.name : file.uploadingFile.name;

    const progressBarProps = () =>
      isUploadedFile(file)
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
    const progressPercentage = () =>
      isUploadedFile(file) ? "" : `${file.uploadPercentage}%`;

    const binIconProps = () =>
      isUploadedFile(file)
        ? {
            onClick: () => onDelete(file.uploadedFile.id),
            text: strings.delete,
          }
        : {
            onClick: () => onCancel(file.localId),
            text: strings.cancel,
          };

    return (
      <Grid container className="file-upload" spacing={2} alignItems="center">
        <Grid item className="file-name" xs={8}>
          {fileName()}
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
            <LinearProgress variant="determinate" {...progressBarProps()} />
          </Grid>
          <Grid item xs={3}>
            {progressPercentage()}
          </Grid>
        </Grid>
        <Grid item xs={1}>
          <FileActionLink
            {...binIconProps()}
            showText={false}
            linkClassName="unselect"
          />
        </Grid>
      </Grid>
    );
  });
  return (
    <div id="uploads">
      <div className="uploadsprogress">{fileList}</div>
      <div {...getRootProps()}>
        <div className="customfile focus">
          <Button
            className="customfile-button focus btn btn-equella btn-mini"
            onClick={(e) => e.preventDefault()}
          >
            Browse
          </Button>
          <span className="customfile-feedback">No file selected...</span>
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
    productionPrefix: "oeq-universal-file-uploader",
    seed: "oeq-universal-file-uploader",
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
