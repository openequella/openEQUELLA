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
  createGenerateClassName,
  Divider,
  Grid,
  LinearProgress,
  Link,
  StylesProvider,
  ThemeProvider,
} from "@material-ui/core";
import { v4 } from "uuid";
import * as React from "react";
import { useEffect, useState } from "react";
import * as ReactDOM from "react-dom";
import { useDropzone } from "react-dropzone";
import {
  buildMaxAttachmentWarning,
  cancelUpload,
  CompleteUploadResponse,
  deleteUpload,
  isUpdateEntry,
  isUploadedFile,
  newUpload,
  OeqFileInfo,
  updateCtrlErrorText,
  updateDuplicateMessage,
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

interface InlineFileUploaderProps {
  elem: Element;
  ctrlId: string;
  entries: OeqFileInfo[];
  maxAttachments: number | null;
  canUpload: boolean;
  dialog: (replaceUuid: string, editUuid: string) => void;
  editable: boolean;
  commandUrl: string;
  strings: ControlStrings;
  reloadState: () => void;
}

/**
 * A component used to upload files by 'drag and drop' or 'file selector'.
 */
const InlineFileUploader = ({
  ctrlId,
  entries,
  maxAttachments,
  canUpload,
  dialog: openDialog,
  editable,
  commandUrl,
  strings,
  reloadState,
}: InlineFileUploaderProps) => {
  const [uploadedFiles, setUploadedFiles] = useState<UploadedFile[]>(
    entries.map((entry) => ({
      uploadedFile: entry,
      status: "uploaded",
    }))
  );
  const [uploadingFiles, setUploadingFiles] = useState<UploadingFile[]>([]);
  const [fileCount, setFileCount] = useState<number>(entries.length);
  const [showDuplicateWarning, setShowDuplicateWarning] = useState<
    boolean | undefined
  >(undefined);

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
      setFileCount(fileCount + droppedFiles.length);
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
              setShowDuplicateWarning(
                res.attachmentDuplicateInfo?.displayWarningMessage ?? false
              );
            }
          })
          .finally(reloadState);
      });
    },
  });

  useEffect(() => {
    if (showDuplicateWarning !== undefined) {
      updateDuplicateMessage(ctrlId, showDuplicateWarning);
    }
  }, [showDuplicateWarning]);

  useEffect(() => {
    if (maxAttachments) {
      if (fileCount > maxAttachments) {
        const removeNumber = `${fileCount - maxAttachments}`;
        if (maxAttachments > 1) {
          updateCtrlErrorText(
            ctrlId,
            `${buildMaxAttachmentWarning(
              strings.toomany,
              `${maxAttachments}`,
              removeNumber
            )}`
          );
        } else {
          updateCtrlErrorText(
            ctrlId,
            `${buildMaxAttachmentWarning(strings.toomany_1, removeNumber)}`
          );
        }
      } else {
        updateCtrlErrorText(ctrlId);
      }
    }
  }, [fileCount]);

  const onEdit = (fileId: string) => openDialog("", fileId);

  const onReplace = (fileId: string) => openDialog(fileId, "");

  const onDelete = (fileId: string) => {
    const confirmDelete = window.confirm(strings.deleteConfirm);
    if (confirmDelete) {
      deleteUpload(commandUrl, fileId)
        .then(({ ids, attachmentDuplicateInfo }) => {
          setUploadedFiles(
            deleteElement(
              uploadedFiles,
              ({ uploadedFile: { id } }: UploadedFile) => fileId === id,
              1
            )
          );
          setShowDuplicateWarning(
            attachmentDuplicateInfo?.displayWarningMessage ?? false
          );
          setFileCount(fileCount - 1);
        })
        .finally(reloadState);
    }
  };

  const onCancel = (fileId: string) => {
    const cancelCallback = () => {
      setUploadingFiles((prev) =>
        deleteElement(prev, ({ localId }) => localId === fileId, 1)
      );
      setFileCount(fileCount - 1);
    };
    cancelUpload(fileId, cancelCallback);
  };

  const fileList = () => {
    const fileName = (file: UploadedFile | UploadingFile) => {
      if (isUploadedFile(file)) {
        const { link, name } = file.uploadedFile;
        return (
          <Link href={link} target="_blank">
            {name}
          </Link>
        );
      }

      const {
        uploadingFile: { name },
        uploadPercentage,
      } = file;
      return (
        <Grid container className="progress-bar">
          <Grid xs={10} item>
            {name}
          </Grid>
          <Grid xs={2} item container alignItems="center" spacing={2}>
            <Grid xs={9} item>
              <LinearProgress variant="determinate" value={uploadPercentage} />
            </Grid>
            <Grid xs={3} item>
              {`${uploadPercentage}%`}
            </Grid>
          </Grid>
        </Grid>
      );
    };

    const fileActions = (file: UploadedFile | UploadingFile) => {
      if (isUploadedFile(file)) {
        const { id } = file.uploadedFile;
        return (
          <Grid container spacing={2} wrap="nowrap">
            {editable && (
              <>
                <Grid item>
                  <FileActionLink
                    onClick={() => onEdit(id)}
                    text={strings.edit}
                  />
                </Grid>

                <Divider orientation="vertical" flexItem />
                <Grid item>
                  <FileActionLink
                    onClick={() => onReplace(id)}
                    text={strings.replace}
                  />
                </Grid>

                <Divider orientation="vertical" flexItem />
              </>
            )}

            <Grid item>
              <FileActionLink
                onClick={() => onDelete(id)}
                text={strings.delete}
              />
            </Grid>
          </Grid>
        );
      }
      return (
        <FileActionLink
          onClick={() => onCancel(file.localId)}
          text={strings.cancel}
          showText={false}
          linkClassName="unselect"
        />
      );
    };

    const files = [...uploadedFiles, ...uploadingFiles].map((file, index) => (
      <tr
        key={isUploadedFile(file) ? file.uploadedFile.id : file.localId}
        className={index % 2 === 0 ? "even" : "odd rowShown"}
      >
        <td className="name">{fileName(file)}</td>
        <td className="actions">{fileActions(file)}</td>
      </tr>
    ));

    const noFiles = (
      <tr className="even">
        <td>{strings.none}</td>
      </tr>
    );

    return <tbody>{fileCount > 0 ? files : noFiles}</tbody>;
  };

  return (
    <div id={`${ctrlId}universalresources`} className="universalresources">
      <table className="zebra selections">{fileList()}</table>

      {editable && (maxAttachments === null || fileCount < maxAttachments) && (
        <>
          <FileActionLink
            id={`${ctrlId}_addLink`}
            onClick={() => openDialog("", "")}
            text={strings.add}
            linkClassName="add"
          />
          {canUpload && (
            <div {...getRootProps({ className: "dropzone" })}>
              <input id={`${ctrlId}_fileUpload_file`} {...getInputProps()} />
              <div className="filedrop">{strings.drop}</div>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export const render = (props: InlineFileUploaderProps) => {
  const generateClassName = createGenerateClassName({
    productionPrefix: "oeq-inline-file-uploader",
    seed: "oeq-inline-file-uploader",
  });
  ReactDOM.render(
    <StylesProvider generateClassName={generateClassName}>
      <ThemeProvider theme={oeqTheme}>
        <InlineFileUploader {...props} />
      </ThemeProvider>
    </StylesProvider>,
    props.elem
  );
};
