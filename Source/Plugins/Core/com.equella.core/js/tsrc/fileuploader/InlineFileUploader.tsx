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
import * as React from "react";
import { useEffect, useState } from "react";
import * as ReactDOM from "react-dom";
import { useDropzone } from "react-dropzone";
import {
  buildMaxAttachmentWarning,
  cancelUpload,
  deleteUpload,
  isUpdateEntry,
  isUploadedFile,
  newUpload,
  AjaxFileEntry,
  updateCtrlErrorText,
  updateDuplicateMessage,
  UploadedFile,
  UploadingFile,
  completeUpload,
  NewUploadResponse,
  UpdateEntry,
  UploadFailed,
  generateLocalFile,
} from "../modules/FileUploaderModule";
import { oeqTheme } from "../theme";
import {
  addElement,
  deleteElement,
  replaceElement,
} from "../util/ImmutableArrayUtil";
import { FileActionLink } from "./FileUploaderActionLink";

/**
 * Data structure for all texts server passes in
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
interface InlineFileUploaderProps {
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
      fileEntry: entry,
      status: "uploaded",
    }))
  );
  const [uploadingFiles, setUploadingFiles] = useState<UploadingFile[]>([]);
  const [fileCount, setFileCount] = useState<number>(entries.length);
  const [showDuplicateWarning, setShowDuplicateWarning] = useState<
    boolean | undefined
  >(undefined);

  const { getRootProps, getInputProps } = useDropzone({
    onDrop: (droppedFiles) => {
      const updateUploadProgress = (updatedFile: UploadingFile) => {
        setUploadingFiles((prev) =>
          replaceElement(
            prev,
            (file: UploadingFile) => file.localId === updatedFile.localId,
            updatedFile
          )
        );
      };
      // Update the file count first.
      setFileCount(fileCount + droppedFiles.length);
      // Upload each dropped file.
      droppedFiles.forEach((droppedFile) => {
        const localFile = generateLocalFile(droppedFile);
        // Add each file to the uploading list.
        setUploadingFiles((prev) => addElement(prev, localFile));
        // A promise chain which includes initialising and completing an upload as well as updating state.
        newUpload(commandUrl, localFile)
          .then(({ uploadUrl }: NewUploadResponse) =>
            completeUpload(uploadUrl, localFile, updateUploadProgress)
          )
          .then((uploadResponse: UpdateEntry | UploadFailed) => {
            if (isUpdateEntry(uploadResponse)) {
              const { entry, attachmentDuplicateInfo } = uploadResponse;
              const uploadedFile: UploadedFile = {
                fileEntry: entry,
                status: "uploaded",
              };
              // Remove each file from the uploading list.
              setUploadingFiles((prev) =>
                deleteElement(
                  prev,
                  (file: UploadingFile) => file.localId === localFile.localId,
                  1
                )
              );
              // Add each file to the uploaded list.
              setUploadedFiles((prev) => addElement(prev, uploadedFile));
              // Update duplicate warning.
              setShowDuplicateWarning(
                attachmentDuplicateInfo?.displayWarningMessage ?? false
              );
            }
          })
          .catch
          //todo handle error
          ()
          .finally(reloadState);
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
  }, [showDuplicateWarning]);

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
      const remainingQuota = fileCount - maxAttachments;
      const getTextFromFormat = () =>
        maxAttachments > 1
          ? buildMaxAttachmentWarning(
              strings.toomany,
              maxAttachments,
              remainingQuota
            )
          : buildMaxAttachmentWarning(strings.toomany_1, remainingQuota);
      const warningText = fileCount > maxAttachments ? getTextFromFormat() : "";

      updateCtrlErrorText(ctrlId, warningText);
    }
  }, [fileCount]);

  const onEdit = (fileId: string) => openDialog("", fileId);

  const onReplace = (fileId: string) => openDialog(fileId, "");

  const onDelete = (fileId: string) => {
    const confirmDelete = window.confirm(strings.deleteConfirm);
    if (confirmDelete) {
      deleteUpload(commandUrl, fileId)
        .then(({ attachmentDuplicateInfo }) => {
          // Remove the file from the uploaded list.
          setUploadedFiles(
            deleteElement(
              uploadedFiles,
              ({ fileEntry }) => fileId === fileEntry.id,
              1
            )
          );
          // Update duplicate warning.
          setShowDuplicateWarning(
            attachmentDuplicateInfo?.displayWarningMessage ?? false
          );
          // Update the file count.
          setFileCount(fileCount - 1);
        })
        .finally(reloadState);
    }
  };

  const onCancel = (fileId: string) => {
    cancelUpload(fileId).then(() => {
      // Remove the file from the uploading list.
      setUploadingFiles(
        deleteElement(uploadingFiles, ({ localId }) => localId === fileId, 1)
      );
      setFileCount(fileCount - 1);
    });
  };

  /**
   * Build a file list. Each list item has two columns and what the two columns
   * display depends on file status.
   */
  const buildFileList = () => {
    const buildFirstColumn = (file: UploadedFile | UploadingFile) => {
      if (isUploadedFile(file)) {
        const { link, name } = file.fileEntry;
        return (
          <Link href={link} target="_blank">
            {name}
          </Link>
        );
      }

      const {
        fileEntry: { name },
        uploadPercentage,
      } = file;
      return (
        <Grid container className="progress-bar progressbarOuter">
          <Grid item xs={10}>
            {name}
          </Grid>
          <Grid item container xs={2} alignItems="center" spacing={2}>
            <Grid item xs={9}>
              <LinearProgress variant="determinate" value={uploadPercentage} />
            </Grid>
            <Grid item xs={3}>
              {`${uploadPercentage}%`}
            </Grid>
          </Grid>
        </Grid>
      );
    };

    const buildSecondColumn = (file: UploadedFile | UploadingFile) => {
      if (isUploadedFile(file)) {
        const { id } = file.fileEntry;
        return (
          <Grid container spacing={2} wrap="nowrap">
            {editable &&
              [
                {
                  text: strings.edit,
                  handler: onEdit,
                  isDividerNeeded: true,
                },
                {
                  text: strings.replace,
                  handler: onReplace,
                  isDividerNeeded: true,
                },
                {
                  text: strings.delete,
                  handler: onDelete,
                  isDividerNeeded: false,
                },
              ].map(({ text, handler, isDividerNeeded }) => (
                <>
                  <Grid item>
                    <FileActionLink onClick={() => handler(id)} text={text} />
                  </Grid>
                  {isDividerNeeded && (
                    <Divider orientation="vertical" flexItem />
                  )}
                </>
              ))}
          </Grid>
        );
      }
      return (
        <FileActionLink
          onClick={() => onCancel(file.localId)}
          text={strings.cancel}
          showText={false}
          customClass="unselect"
        />
      );
    };

    const fileList = [...uploadedFiles, ...uploadingFiles].map(
      (file, index) => (
        <tr
          key={isUploadedFile(file) ? file.fileEntry.id : file.localId}
          className={index % 2 === 0 ? "even" : "odd rowShown"}
        >
          <td className="name">{buildFirstColumn(file)}</td>
          <td className="actions">{buildSecondColumn(file)}</td>
        </tr>
      )
    );

    const noFiles = (
      <tr className="even">
        <td>{strings.none}</td>
      </tr>
    );

    return <tbody>{fileCount > 0 ? fileList : noFiles}</tbody>;
  };

  return (
    <div id={`${ctrlId}universalresources`} className="universalresources">
      <table className="zebra selections">{buildFileList()}</table>

      {editable && (maxAttachments === null || fileCount < maxAttachments) && (
        <>
          <FileActionLink
            id={`${ctrlId}_addLink`}
            onClick={() => openDialog("", "")}
            text={strings.add}
            customClass="add"
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

/**
 * This function is primarily created for rendering this component on server
 * @param props The props passed into this component (search 'uploadArgs' in 'UniversalWebControlNew.scala' for details)
 */
export const render = (props: InlineFileUploaderProps) => {
  const generateClassName = createGenerateClassName({
    productionPrefix: "oeq-ifu",
    seed: "oeq-ifu",
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
