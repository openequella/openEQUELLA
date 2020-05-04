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
  Typography,
} from "@material-ui/core";
import { commonString } from "../util/commonstrings";

interface MessageDialogProps {
  open: boolean;
  title: string;
  subtitle: string;
  messages: string[];
  close: () => void;
}
const MessageDialog = ({
  open,
  title,
  subtitle,
  messages,
  close,
}: MessageDialogProps) => {
  return (
    <Dialog open={open} onClose={close} fullWidth>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <DialogContentText>{subtitle}</DialogContentText>
        {messages.map((message) => (
          <Typography>{message}</Typography>
        ))}
      </DialogContent>
      <DialogActions>
        <Button onClick={close} color="primary">
          {commonString.action.ok}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default MessageDialog;
