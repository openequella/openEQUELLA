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
import { CreateNewFolder } from "@mui/icons-material";
import DeleteIcon from "@mui/icons-material/Delete";
import type { TreeItemProps } from "@mui/x-tree-view/TreeItem";
import { InputLabel, MenuItem, Select } from "@mui/material";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as React from "react";
import type { ACLOperatorType } from "../../modules/ACLExpressionModule";
import { getOperatorLabel } from "../../modules/ACLExpressionModule";
import { languageStrings } from "../../util/langstrings";
import { TooltipIconButton } from "../TooltipIconButton";
import { ACLTreeItem, classes } from "./ACLTreeItem";

const {
  aclExpressionBuilder: { match: matchLabel, addGroup: addGroupLabel },
} = languageStrings;

export interface ACLTreeOperatorProps extends TreeItemProps {
  /**
   * Operator name.
   */
  operator: ACLOperatorType;
  /**
   * Available operators for the common level expressions. Default to AND and OR and NOT.
   */
  groupOperators?: ACLOperatorType[];
  /**
   * `true` if an operator is a root operator which appears on the top of the tree without a delete button.
   */
  isRoot?: boolean;
  /**
   * Handler to select the operator that is being clicked.
   */
  onClick: () => void;
  /**
   * Fired when the node is deleted.
   */
  onDelete: () => void;
  /**
   * Fired when the operator is changed.
   */
  onOperatorChange: (operator: ACLOperatorType) => void;
  /**
   * Fired when the add group button is clicked.
   */
  onAddGroup: () => void;
}

/**
 * Tree item (node) which represents an ACL operator.
 */
export const ACLTreeOperator = ({
  itemId,
  groupOperators = ["OR", "AND", "NOT"],
  isRoot,
  operator,
  onClick,
  onDelete,
  onOperatorChange,
  onAddGroup,
  ...other
}: ACLTreeOperatorProps): React.JSX.Element => {
  // default selected value is `OR`
  const buildMenuItemForOperator = (operator: ACLOperatorType) => (
    <MenuItem key={operator} selected={operator === "OR"} value={operator}>
      {getOperatorLabel(operator)}
    </MenuItem>
  );

  const treeOperatorLabel = () => (
    <div className={classes.labelRoot}>
      <InputLabel data-testid="ACLTreeOperator-label" sx={{ paddingRight: 2 }}>
        {matchLabel}
      </InputLabel>
      <Select
        value={operator}
        onChange={(event) =>
          onOperatorChange(event.target.value as ACLOperatorType)
        }
        // prevent toggling tree item
        onClick={(event) => event.stopPropagation()}
        className={classes.labelSelect}
      >
        {pipe(groupOperators, A.map(buildMenuItemForOperator))}
      </Select>
      <TooltipIconButton
        onClick={(event) => {
          onAddGroup();
          // prevent toggling tree item
          event.stopPropagation();
        }}
        title={addGroupLabel}
      >
        <CreateNewFolder />
      </TooltipIconButton>
      {!isRoot && (
        <TooltipIconButton
          title={languageStrings.common.action.delete}
          onClick={(event) => {
            onDelete();
            // prevent toggling tree item
            event.stopPropagation();
          }}
        >
          <DeleteIcon data-testid="ACLTreeOperator-delete" />
        </TooltipIconButton>
      )}
    </div>
  );

  // the className `ACLTreeOperator` is used for testing purpose (find the element)
  return (
    <ACLTreeItem
      id={itemId}
      itemId={itemId}
      label={treeOperatorLabel()}
      onClick={(e) => {
        // Since MUI-X TreeItem V7, this event will be propagated all the way up to the root so we must
        // stop the propagation to make sure only this operator is selected.
        e.stopPropagation();
        onClick();
      }}
      classes={{
        root: classes.root,
      }}
      {...other}
    />
  );
};
