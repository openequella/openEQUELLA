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
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from "@material-ui/core";
import { languageStrings } from "../util/langstrings";

interface AdminDownloadDialogProps {
  open: boolean;
  onClose: () => void;
}

export default function AdminDownloadDialog({
  open,
  onClose,
}: AdminDownloadDialogProps) {
  const { ok } = languageStrings.common.action;
  const { link, text, title } = languageStrings.adminconsoledownload;

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <DialogContentText>
          {text.introTextOne}
          <a href={link} target="_blank">
            {text.introTextTwo}
          </a>
          {text.introTextThree}
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} color="primary">
          {ok}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
