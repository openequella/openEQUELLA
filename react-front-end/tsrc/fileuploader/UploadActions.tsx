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
import { Divider, Grid, IconButton } from "@mui/material";
import * as React from "react";
import { Fragment } from "react";
import { ReactElement } from "react";
import { FileUploaderActionLink } from "./FileUploaderActionLink";

export interface UploadAction {
  /**
   * Fired when clicking an action button
   */
  onClick: () => void;
  /**
   * Text of an action
   */
  text: string;
  /**
   * The MUI Icon representing the action
   */
  icon?: ReactElement;
}

interface UploadActionsProps {
  /**
   * A list of UploadActions to be rendered
   */
  actions: UploadAction[];
}

/**
 * Display one or more actions that are available for different upload status.
 * For each action, display an IconButton or a FileUploaderActionLink, depending on whether a MUI Icon is provided or not.
 * A vertical MUI Divider is added after each action except the last action.
 */
export const UploadActions = ({ actions }: UploadActionsProps) => (
  <Grid container spacing={1} className="actions" justifyContent="flex-end">
    {actions.map(({ onClick, text, icon }, index) => (
      <Fragment key={index}>
        <Grid>
          {icon ? (
            <IconButton
              onClick={onClick}
              title={text}
              color="primary"
              size="small"
            >
              {icon}
            </IconButton>
          ) : (
            <FileUploaderActionLink onClick={onClick} text={text} />
          )}
        </Grid>
        {index < actions.length - 1 && (
          <Grid>
            <Divider orientation="vertical" />
          </Grid>
        )}
      </Fragment>
    ))}
  </Grid>
);
