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
import { Dialog, DialogTitle, DialogActions, Button } from "@mui/material";
import { commonString } from "../util/commonstrings";

const { cancel, ok: okLabel } = commonString.action;

export interface SimpleConfirmDialogProps {
  /**
   * Open the dialog when true.
   */
  open: boolean;
  /**
   * The title of the dialog.
   */
  title: string;
  /**
   * Fired when click the Confirm button.
   */
  onConfirm: () => void;
  /**
   * Fired when click the Cancel button.
   */
  onCancel: () => void;
}

/**
 * A simple confirm dialog which only contains a title, confirm and cancel button.
 */
const SimpleConfirmDialog = ({
  open,
  title,
  onCancel,
  onConfirm,
}: SimpleConfirmDialogProps) => (
  <Dialog
    open={open}
    onClose={onCancel}
    aria-labelledby="simple-dialog-dialog-title"
    aria-describedby="simple-dialog-description"
  >
    <DialogTitle id="simple-dialog-title">{title}</DialogTitle>
    <DialogActions>
      <Button onClick={onCancel} color="primary">
        {cancel}
      </Button>
      <Button onClick={onConfirm} color="primary" autoFocus>
        {okLabel}
      </Button>
    </DialogActions>
  </Dialog>
);

export default SimpleConfirmDialog;
