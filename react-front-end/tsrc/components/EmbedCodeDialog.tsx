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
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from "@mui/material";
import { styled } from "@mui/material/styles";
import * as React from "react";
import { languageStrings } from "../util/langstrings";

const PREFIX = "EmbedCodeDialog";

const classes = {
  content: `${PREFIX}-content`,
};

const StyledDialog = styled(Dialog)(({ theme }) => ({
  [`& .${classes.content}`]: {
    backgroundColor: theme.palette.background.default,
    padding: theme.spacing(2),
  },
}));

export interface EmbedCodeDialogProps {
  /**
   * `true` to open the dialog.
   */
  open: boolean;
  /**
   * Fired when the dialog is closed.
   */
  onCloseDialog: () => void;
  /**
   * Embed code of a resource to be displayed in the dialog.
   */
  embedCode: string;
}

const {
  label: embedCodeLabel,
  copy: copyEmbedCode,
  closeEmbedCodeDialog,
} = languageStrings.embedCode;

const { copy: actionCopy, close: actionClose } = languageStrings.common.action;

/**
 * Provide a Dialog which allows users to copy embed code of a resource(e.g. image && video).
 */
export const EmbedCodeDialog = ({
  open,
  onCloseDialog,
  embedCode,
}: EmbedCodeDialogProps) => {
  return (
    <StyledDialog open={open} fullWidth>
      <DialogTitle>{embedCodeLabel}</DialogTitle>
      <DialogContent>
        <DialogContentText className={classes.content}>
          <code>{embedCode}</code>
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        {[
          {
            buttonText: actionClose,
            buttonLabel: closeEmbedCodeDialog,
            onClose: onCloseDialog,
          },
          {
            buttonText: actionCopy,
            buttonLabel: copyEmbedCode,
            onClose: () =>
              navigator.clipboard.writeText(embedCode).then(onCloseDialog),
          },
        ].map(({ buttonText, buttonLabel, onClose }) => (
          <Button
            key={buttonText}
            onClick={(event) => {
              event.stopPropagation();
              onClose();
            }}
            color="secondary"
            aria-label={buttonLabel}
          >
            {buttonText}
          </Button>
        ))}
      </DialogActions>
    </StyledDialog>
  );
};
