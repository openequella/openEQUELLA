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
import { IconButton, Snackbar, SnackbarContent } from "@mui/material";
import { styled } from "@mui/material/styles";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import CloseIcon from "@mui/icons-material/Close";
import ErrorIcon from "@mui/icons-material/Error";
import InfoIcon from "@mui/icons-material/Info";
import WarningIcon from "@mui/icons-material/Warning";
import * as React from "react";
import { commonString } from "../util/commonstrings";

import { amber, green } from "@mui/material/colors";

const PREFIX = "MessageInfo";

const classes = {
  success: `${PREFIX}-success`,
  error: `${PREFIX}-error`,
  info: `${PREFIX}-info`,
  warning: `${PREFIX}-warning`,
  icon: `${PREFIX}-icon`,
  iconVariant: `${PREFIX}-iconVariant`,
  message: `${PREFIX}-message`,
};

const StyledSnackbar = styled(Snackbar)(({ theme }) => ({
  [`& .${classes.success}`]: {
    backgroundColor: green[600],
  },

  [`& .${classes.error}`]: {
    backgroundColor: theme.palette.error.dark,
  },

  [`& .${classes.info}`]: {
    backgroundColor: theme.palette.primary.dark,
  },

  [`& .${classes.warning}`]: {
    backgroundColor: amber[700],
  },

  [`& .${classes.icon}`]: {
    fontSize: 20,
  },

  [`& .${classes.iconVariant}`]: {
    opacity: 0.9,
    marginRight: theme.spacing(1),
  },

  [`& .${classes.message}`]: {
    display: "flex",
    alignItems: "center",
  },
}));

const variantIcon = {
  success: CheckCircleIcon,
  warning: WarningIcon,
  error: ErrorIcon,
  info: InfoIcon,
};

export type MessageInfoVariant = "success" | "warning" | "error" | "info";

export interface MessageInfoProps {
  open: boolean;
  onClose: () => void;
  title: string;
  variant: MessageInfoVariant;
}

const MessageInfo = ({ open, title, variant, onClose }: MessageInfoProps) => {
  const Icon = variantIcon[variant];
  return (
    <StyledSnackbar open={open} onClose={onClose} autoHideDuration={5000}>
      <SnackbarContent
        className={classes[variant]}
        aria-describedby="client-snackbar"
        message={
          <span id="client-snackbar" className={classes.message}>
            <Icon className={`${classes.icon} ${classes.iconVariant}`} />
            {title}
          </span>
        }
        action={
          <IconButton
            key="close"
            aria-label={commonString.action.close}
            color="inherit"
            onClick={onClose}
            size="large"
          >
            <CloseIcon className={classes.icon} />
          </IconButton>
        }
      />
    </StyledSnackbar>
  );
};

export default MessageInfo;
