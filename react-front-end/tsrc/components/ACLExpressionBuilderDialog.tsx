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
import CloseIcon from "@mui/icons-material/Close";
import {
  Dialog,
  DialogContent,
  DialogContentText,
  DialogTitle,
  IconButton,
} from "@mui/material";
import * as React from "react";
import { languageStrings } from "../util/langstrings";
import ACLExpressionBuilder, {
  ACLExpressionBuilderProps,
} from "./aclexpressionbuilder/ACLExpressionBuilder";

const { title } = languageStrings.aclExpressionBuilderDialog;

export interface ACLExpressionBuilderDialogProps
  extends Pick<
    ACLExpressionBuilderProps,
    | "searchUserProvider"
    | "searchGroupProvider"
    | "searchRoleProvider"
    | "aclEntityResolversProvider"
  > {
  /** Open the dialog when true. */
  open: boolean;
  /** The currently selected ACLExpression. */
  value: string;
  /** Handler for when dialog is closed. */
  onClose: (result?: string) => void;
}

/**
 * Simple dialog to prompt user to search and select recipients by embedding the ACLExpressionBuilder component.
 */
const ACLExpressionBuilderDialog = ({
  open,
  value,
  onClose,
  ...restProps
}: ACLExpressionBuilderDialogProps) => {
  return (
    <Dialog
      open={open}
      onClose={() => onClose()}
      aria-labelledby="aclexpressionbuilder-dialog-title"
      fullWidth
      maxWidth="lg"
    >
      <DialogTitle id="aclexpressionbuilder-dialog-title">
        {title}
        <IconButton
          aria-label="close"
          onClick={() => onClose()}
          sx={{
            position: "absolute",
            right: 8,
            top: 8,
          }}
        >
          <CloseIcon />
        </IconButton>
      </DialogTitle>
      <DialogContent>
        <DialogContentText component="div">
          <ACLExpressionBuilder
            aclExpression={value}
            onFinish={onClose}
            {...restProps}
          />
        </DialogContentText>
      </DialogContent>
    </Dialog>
  );
};

export default ACLExpressionBuilderDialog;
