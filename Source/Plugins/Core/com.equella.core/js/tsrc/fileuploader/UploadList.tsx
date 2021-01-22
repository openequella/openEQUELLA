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
  Grid,
  List,
  ListItem,
  ListItemSecondaryAction,
  ListItemText,
} from "@material-ui/core";
import * as React from "react";
import { ReactElement } from "react";
import {
  isUploadedFile,
  UploadedFile,
  UploadingFile,
} from "../modules/FileUploaderModule";

interface UploadListProps {
  files: (UploadingFile | UploadedFile)[];
  buildFileName: (file: UploadingFile | UploadedFile) => ReactElement | string;
  buildActions: (file: UploadingFile | UploadedFile) => ReactElement[];
  buildUploadInfo: (
    file: UploadingFile | UploadedFile
  ) => ReactElement | undefined;
  noFiles?: string;
}
export const UploadList = ({
  files,
  buildFileName,
  buildActions,
  buildUploadInfo,
  noFiles,
}: UploadListProps) => (
  <List>
    {files.length > 0 ? (
      files.map((file) => (
        <ListItem
          key={isUploadedFile(file) ? file.fileEntry.id : file.localId}
          divider
        >
          <ListItemText
            primary={buildFileName(file)}
            secondary={buildUploadInfo(file)}
          />
          <ListItemSecondaryAction>
            <Grid container spacing={1}>
              {buildActions(file).map((action, index) => (
                <Grid item key={index}>
                  {action}
                </Grid>
              ))}
            </Grid>
          </ListItemSecondaryAction>
        </ListItem>
      ))
    ) : (
      <ListItem divider>{noFiles}</ListItem>
    )}
  </List>
);
