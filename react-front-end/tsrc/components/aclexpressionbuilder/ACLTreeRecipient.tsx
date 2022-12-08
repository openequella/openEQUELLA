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
import { Typography } from "@material-ui/core";
import DeleteIcon from "@material-ui/icons/Delete";
import TreeItem, { TreeItemProps } from "@material-ui/lab/TreeItem";
import * as React from "react";
import { languageStrings } from "../../util/langstrings";
import { TooltipIconButton } from "../TooltipIconButton";
import { useACLTreeItemStyles } from "./ACLExpressionHelper";

export interface ACLTreeRecipientProps extends TreeItemProps {
  /**
   * Name of the expression.
   * Example: `Referred by http://edalex.com`
   */
  expressionName: string;
  /**
   * Fired when the item is deleted.
   */
  onDelete: (nodeID: string) => void;
}

/**
 * Tree item (node) which represents an ACL Recipient.
 */
export const ACLTreeRecipient = ({
  nodeId,
  expressionName,
  onDelete,
  ...other
}: ACLTreeRecipientProps): JSX.Element => {
  const classes = useACLTreeItemStyles();

  const treeRecipientLabel = () => (
    <div className={classes.labelRoot}>
      <Typography className={classes.labelText} variant="body2">
        {expressionName}
      </Typography>
      <TooltipIconButton
        title={languageStrings.common.action.delete}
        onClick={(_) => {
          onDelete(nodeId);
        }}
      >
        <DeleteIcon />
      </TooltipIconButton>
    </div>
  );

  return (
    <TreeItem
      nodeId={nodeId}
      label={treeRecipientLabel()}
      classes={{
        root: classes.root,
      }}
      {...other}
    />
  );
};
