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
import * as React from "react";
import { ReactNode } from "react";
import { commonString } from "../util/commonstrings";
import MessageInfo from "./MessageInfo";
import { NavigationGuard } from "./NavigationGuard";
import { Button, Grid } from "@material-ui/core";
import { Save } from "@material-ui/icons";

export interface SettingPageTemplateProps {
  /**
   * Fired when the Save button is clicked.
   */
  onSave: () => void;
  /**
   * Disable the Save button if true.
   */
  saveButtonDisabled: boolean;
  /**
   * Open the snack bar if true.
   */
  snackbarOpen: boolean;
  /**
   * Fired when the snack bar is closed.
   */
  snackBarOnClose: () => void;
  /**
   * Prevent navigate to different pages if true.
   */
  preventNavigation: boolean;
  /**
   * Child components wrapped in this template.
   */
  children: ReactNode;
}

/**
 * This component is a top level template for Setting pages.
 * It renders child components as well as a Save button, a snack bar
 * and a dialog preventing navigation.
 */
const SettingPageTemplate = ({
  onSave,
  snackbarOpen,
  snackBarOnClose,
  saveButtonDisabled,
  preventNavigation,
  children,
}: SettingPageTemplateProps) => {
  return (
    <>
      <Grid container spacing={2}>
        <Grid item container xs={9} spacing={2}>
          {
            // Put each child in this nested Grid
            React.Children.map(children, (child) => (
              <Grid item xs={12}>
                {child}
              </Grid>
            ))
          }
        </Grid>

        <Grid item xs={3}>
          <Button
            fullWidth
            id="_saveButton"
            color="primary"
            variant="contained"
            size="large"
            onClick={onSave}
            aria-label={commonString.action.save}
            disabled={saveButtonDisabled}
          >
            <Save />
            {commonString.action.save}
          </Button>
        </Grid>
      </Grid>

      <MessageInfo
        title={commonString.result.success}
        open={snackbarOpen}
        onClose={snackBarOnClose}
        variant={"success"}
      />

      <NavigationGuard when={preventNavigation} />
    </>
  );
};

export default SettingPageTemplate;
