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
import { languageStrings } from "../util/langstrings";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  Button,
  DialogActions,
} from "@material-ui/core";
import { commonString } from "../util/commonstrings";

export const strings = languageStrings.template;

export function defaultNavMessage() {
  return strings.navaway.content;
}

interface NavAwayDialogProps {
  message: string;
  navigateConfirm: (confirmed: boolean) => void;
  open: boolean;
}

export const NavAwayDialog = React.memo(function NavAwayDialog({
  message,
  navigateConfirm,
  open,
}: NavAwayDialogProps) {
  return (
    <Dialog open={open}>
      <DialogTitle>{strings.navaway.title}</DialogTitle>
      <DialogContent>
        <DialogContentText>{message}</DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button color="secondary" onClick={(_) => navigateConfirm(false)}>
          {commonString.action.cancel}
        </Button>
        <Button color="primary" onClick={(_) => navigateConfirm(true)}>
          {commonString.action.discard}
        </Button>
      </DialogActions>
    </Dialog>
  );
});
