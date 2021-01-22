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
import { Divider, Grid, IconButton } from "@material-ui/core";
import * as React from "react";
import { ReactElement } from "react";
import { FileUploaderActionLink } from "./FileUploaderActionLink";

export interface UploadAction {
  onClick: () => void;
  text: string;
  icon?: ReactElement;
}
interface UploadActionsProps {
  actions: UploadAction[];
}
export const UploadActions = ({ actions }: UploadActionsProps) => (
  <Grid container spacing={1} className="actions">
    {actions.map(({ onClick, text, icon }, index) => (
      <>
        <Grid item key={index}>
          {icon ? (
            <IconButton onClick={onClick} title={text} color="primary">
              {icon}
            </IconButton>
          ) : (
            <FileUploaderActionLink onClick={onClick} text={text} />
          )}
        </Grid>
        {index < actions.length - 1 && (
          <Grid item>
            <Divider orientation="vertical" />
          </Grid>
        )}
      </>
    ))}
  </Grid>
);
