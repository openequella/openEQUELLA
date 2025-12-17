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
import DeleteIcon from "@mui/icons-material/Delete";
import { Typography } from "@mui/material";
import type { TreeItemProps } from "@mui/x-tree-view/TreeItem";
import * as E from "fp-ts/Either";
import { pipe } from "fp-ts/function";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { useEffect, useState } from "react";
import type { ACLEntityResolvers } from "../../modules/ACLEntityModule";
import { defaultACLEntityResolvers } from "../../modules/ACLExpressionModule";
import {
  ACLRecipient,
  showRecipient,
  showRecipientHumanReadable,
} from "../../modules/ACLRecipientModule";
import { languageStrings } from "../../util/langstrings";
import { TooltipIconButton } from "../TooltipIconButton";
import { ACLTreeItem, classes } from "./ACLTreeItem";

export interface ACLTreeRecipientProps extends TreeItemProps {
  /**
   * Recipient object which represents this node.
   */
  recipient: ACLRecipient;
  /**
   * Fired when the recipient is deleted.
   */
  onDelete: () => void;
  /***
   * Functions to lookup user, group and role entities.
   */
  aclEntityResolvers?: ACLEntityResolvers;
}

/**
 * Tree item (node) which represents an ACL Recipient.
 */
export const ACLTreeRecipient = ({
  itemId,
  recipient,
  onDelete,
  aclEntityResolvers = defaultACLEntityResolvers,
  ...other
}: ACLTreeRecipientProps): React.JSX.Element => {
  const [expressionName, setExpressionName] = useState(
    recipient.name ?? showRecipient(recipient),
  );

  useEffect(() => {
    (async () => {
      const getName: TE.TaskEither<string, string> = pipe(
        recipient,
        showRecipientHumanReadable(aclEntityResolvers),
      );
      pipe(await getName(), E.fold(console.warn, setExpressionName));
    })();
  }, [aclEntityResolvers, recipient]);

  const treeRecipientLabel = () => (
    <div className={classes.labelRoot}>
      <Typography className={classes.labelText} variant="body2">
        {expressionName}
      </Typography>
      <TooltipIconButton
        title={languageStrings.common.action.delete}
        onClick={(event) => {
          // Prevent the TreeItem from also being selected when clicking the delete button.
          event.stopPropagation();
          onDelete();
        }}
      >
        <DeleteIcon />
      </TooltipIconButton>
    </div>
  );

  return (
    <ACLTreeItem
      itemId={itemId}
      label={treeRecipientLabel()}
      classes={{
        root: classes.root,
      }}
      {...other}
    />
  );
};
