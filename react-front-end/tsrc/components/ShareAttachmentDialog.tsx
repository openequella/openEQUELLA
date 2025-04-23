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
import Copy from "@mui/icons-material/ContentCopy";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  Typography,
} from "@mui/material";
import { styled } from "@mui/material/styles";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as React from "react";
import { languageStrings } from "../util/langstrings";
import { TooltipIconButton } from "./TooltipIconButton";

const PREFIX = "ShareAttachmentDialog";

const classes = {
  dialogContent: `${PREFIX}-dialog-content`,
  copyContent: `${PREFIX}-copy-content`,
};

const StyledDialog = styled(Dialog)(({ theme }) => ({
  [`& .${classes.dialogContent}`]: {
    // Extra indentation for the dialog content.
    paddingLeft: theme.spacing(4),
    paddingRight: theme.spacing(4),
  },
  [`& .${classes.copyContent}`]: {
    backgroundColor: theme.palette.background.default,
    padding: theme.spacing(2),
    width: "100%",
    wordBreak: "break-word",
  },
}));

export interface ShareAttachmentDialogProps {
  /**
   * `true` to open the dialog.
   */
  open: boolean;
  /**
   * Fired when the dialog is closed.
   */
  onCloseDialog: () => void;
  /**
   * URL of the Attachment to be shared.
   */
  src: string;
  /**
   * Embed code of the Attachment to be shared.
   */
  embedCode: O.Option<string>;
}

const { embedCode: embedCodeLabel, link: linkLabel } =
  languageStrings.shareAttachment;
const {
  copy: actionCopy,
  close: actionClose,
  share: actionShare,
} = languageStrings.common.action;

const ShareDetails = ({
  title,
  details,
}: {
  title: string;
  details: string;
}) => (
  <Grid item container>
    <Grid item container justifyContent="space-between" spacing={2}>
      <Grid item>
        <Typography variant="h6">{title}</Typography>
      </Grid>
      <Grid item>
        <TooltipIconButton
          title={actionCopy}
          onClick={(event) => {
            event.stopPropagation();
            navigator.clipboard.writeText(details);
          }}
        >
          <Copy />
        </TooltipIconButton>
      </Grid>
    </Grid>
    <Grid item className={classes.copyContent}>
      <code>{details}</code>
    </Grid>
  </Grid>
);

/**
 * Provide a Dialog which allows users to copy the information of an Attachment, including:
 *
 * - The URL of the Attachment;
 * - Optional embed code if browser supports embedding the Attachment type.
 */
export const ShareAttachmentDialog = ({
  open,
  onCloseDialog,
  src,
  embedCode,
}: ShareAttachmentDialogProps) => {
  const shareDetails = (title: string, details: O.Option<string>) =>
    pipe(
      details,
      O.map((d) => <ShareDetails title={title} details={d} />),
      O.toUndefined,
    );

  return (
    <StyledDialog open={open} fullWidth>
      <DialogTitle variant="h4">{actionShare}</DialogTitle>
      <DialogContent className={classes.dialogContent}>
        <Grid container direction="row" spacing={2}>
          {shareDetails(embedCodeLabel, embedCode)}
          {shareDetails(linkLabel, O.of(src))}
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button
          onClick={(event) => {
            event.stopPropagation();
            onCloseDialog();
          }}
          color="secondary"
          aria-label={actionClose}
        >
          {actionClose}
        </Button>
      </DialogActions>
    </StyledDialog>
  );
};
