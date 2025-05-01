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
import { Grid, List, ListItem } from "@mui/material";
import * as React from "react";
import {
  isUploadedFile,
  UploadedFile,
  UploadingFile,
} from "../modules/FileUploaderModule";
import { UploadAction, UploadActions } from "./UploadActions";
import { UploadFileName } from "./UploadFileName";
import { UploadInfo } from "./UploadInfo";

interface UploadListProps {
  /**
   * A list of UploadingFile or UploadedFile
   */
  files: (UploadingFile | UploadedFile)[];
  /**
   * Function fired to build actions for each selected file.
   */
  buildActions: (file: UploadingFile | UploadedFile) => UploadAction[];
  /**
   * The text displayed when there are no files selected
   */
  noFileSelectedText?: string;
}

/**
 * Show selected files in a MUI List, or a text indicating no files selected.
 * In each ListItem, the primary text is used to show file name. The secondary
 * text is used to show upload information such as a progress bar. The Secondary
 * Action is used to show actions applicable to each file.
 */
export const UploadList = ({
  files,
  buildActions,
  noFileSelectedText,
}: UploadListProps) => (
  <List>
    {files.length > 0 ? (
      files.map((file) => {
        const fileId = isUploadedFile(file) ? file.fileEntry.id : file.localId;

        /**
         * Build the content of each ListItem. Directly show the content for any UploadedFile that
         * does not have any error message. For other cases, use ListItemText to wrap the content,
         * that's, file name as primary text and other information as secondary text.
         */
        const ListItemContent = () => {
          const primaryText = (
            <UploadFileName
              fileName={file.fileEntry.name}
              indented={isUploadedFile(file) ? file.indented : false}
              link={isUploadedFile(file) ? file.fileEntry.link : undefined}
            />
          );
          const secondaryText = isUploadedFile(file) ? (
            <div role="alert" style={{ color: "red" }}>
              {file.errorMessage}
            </div>
          ) : (
            <UploadInfo file={file} />
          );

          return isUploadedFile(file) && !file.errorMessage ? (
            primaryText
          ) : (
            <Grid container direction="column" spacing={1} wrap="nowrap">
              <Grid>{primaryText}</Grid>
              <Grid>{secondaryText}</Grid>
            </Grid>
          );
        };

        // Use "div" as the ListItem component to avoid EBP styles which apply to "li".
        return (
          <ListItem key={fileId} divider component="div">
            <Grid container spacing={2} alignItems="center" sx={{ flex: 1 }}>
              <Grid
                size={{
                  xs: 6,
                  sm: 8,
                  lg: 9,
                }}
              >
                <ListItemContent />
              </Grid>
              <Grid
                size={{
                  xs: 6,
                  sm: 4,
                  lg: 3,
                }}
              >
                <UploadActions actions={buildActions(file)} />
              </Grid>
            </Grid>
          </ListItem>
        );
      })
    ) : (
      <ListItem divider>{noFileSelectedText}</ListItem>
    )}
  </List>
);
