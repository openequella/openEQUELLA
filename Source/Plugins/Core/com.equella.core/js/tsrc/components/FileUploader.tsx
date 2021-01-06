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
import { Divider, Grid, Link } from "@material-ui/core";
import * as React from "react";
import { useState } from "react";
import * as ReactDOM from "react-dom";
import { useDropzone } from "react-dropzone";
import {
  CompleteUploadResponse,
  deleteUpload,
  isUpdateEntry,
  newUpload,
} from "../modules/FileUploaderModule";
import { deleteElement } from "../util/ImmutableArrayUtil";

export interface FileElement {
  id: string;
  name: string;
  link: string;
  preview: boolean;
  editable: boolean;
  children: FileElement[];
}

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

interface UploadListProps {
  elem: Element;
  ctrlId: string;
  entries: FileElement[];
  maxAttachments?: number;
  canUpload: boolean;
  dialog: (replaceUuid: string, editUuid: string) => void;
  onAdd: () => void;
  editable: boolean;
  commandUrl: string;
  strings: ControlStrings;
  reloadState: () => void;
}

const isUploadPromiseFulFilled = (
  promise: PromiseSettledResult<CompleteUploadResponse>
): promise is PromiseFulfilledResult<CompleteUploadResponse> =>
  promise.status === "fulfilled";

/**
 * A component used to upload files by 'drag and drop' or 'file selector'.
 */
const FileUploader = ({
  ctrlId,
  entries,
  maxAttachments,
  canUpload,
  dialog: openDialog,
  onAdd,
  editable,
  commandUrl,
  strings,
  reloadState,
}: UploadListProps) => {
  const [files, setFiles] = useState<FileElement[]>(entries);

  const handleUploadResponse = (
    res: PromiseSettledResult<CompleteUploadResponse>[]
  ) => {
    const fulFilled: CompleteUploadResponse[] = res
      .filter(isUploadPromiseFulFilled)
      .map((res) => res.value);
    const uploaded = fulFilled.filter(isUpdateEntry).map((f) => f.entry);
    setFiles([...files, ...uploaded]);

    //todo: handle UploadFailed
  };
  const { getRootProps, getInputProps } = useDropzone({
    onDrop: (droppedFiles) => {
      console.log(droppedFiles.map((f) => f.size));
      Promise.allSettled([
        ...droppedFiles.map((file) => newUpload(commandUrl, file)),
      ])
        .then(handleUploadResponse)
        .finally(reloadState);
    },
  });

  const onEdit = (id: string) => openDialog("", id);

  const onReplace = (id: string) => openDialog(id, "");

  const onDelete = (id: string) =>
    deleteUpload(commandUrl, id)
      .then(({ ids }) =>
        ids.forEach((id) =>
          setFiles(
            deleteElement(files, (file: FileElement) => file.id === id, 1)
          )
        )
      )
      .finally(reloadState);

  const fileListBody = () => {
    const fileList = files.map((file, index) => {
      return (
        <tr className={index % 2 === 0 ? "even" : "odd rowShown"}>
          <td className="name">
            <Link href={file.link} target="_blank">
              {file.name}
            </Link>
          </td>
          <td className="actions">
            <Grid container wrap="nowrap" spacing={2}>
              <Grid item>
                <Link
                  href="javascript:void(0);"
                  onClick={() => onEdit(file.id)}
                >
                  {strings.edit}
                </Link>
              </Grid>

              <Divider orientation="vertical" flexItem />
              <Grid item>
                <Link
                  href="javascript:void(0);"
                  onClick={() => onReplace(file.id)}
                >
                  {strings.replace}
                </Link>
              </Grid>

              <Divider orientation="vertical" flexItem />
              <Grid item>
                <Link
                  href="javascript:void(0);"
                  onClick={() => onDelete(file.id)}
                >
                  {strings.delete}
                </Link>
              </Grid>
            </Grid>
          </td>
        </tr>
      );
    });
    const noFiles = (
      <tr className="even">
        <td>{strings.none}</td>
      </tr>
    );

    return <tbody>{files.length > 0 ? fileList : noFiles}</tbody>;
  };

  return (
    <div id={`${ctrlId}universalresources`} className="universalresources">
      <table className="zebra selections">{fileListBody()}</table>

      <Link
        id={`${ctrlId}_addLink`}
        className="add"
        href="javascript:void(0);"
        title={strings.add}
        onClick={() => openDialog("", "")}
      >
        {strings.add}
      </Link>

      <div {...getRootProps({ className: "dropzone" })}>
        <input id={`${ctrlId}_fileUpload_file`} {...getInputProps()} />
        <div className="filedrop">{strings.drop}</div>
      </div>
    </div>
  );
};

export const inlineUpload = (props: UploadListProps) => {
  console.log(props);
  ReactDOM.render(<FileUploader {...props} />, props.elem);
};
