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
import { InputLabel, MenuItem, Select } from "@material-ui/core";
import { CreateNewFolder } from "@material-ui/icons";
import DeleteIcon from "@material-ui/icons/Delete";
import TreeItem, { TreeItemProps } from "@material-ui/lab/TreeItem";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as React from "react";
import { getOperatorLabel } from "../../modules/ACLExpressionModule";
import type { ACLOperatorType } from "../../modules/ACLExpressionModule";
import { languageStrings } from "../../util/langstrings";
import { TooltipIconButton } from "../TooltipIconButton";
import { useACLTreeItemStyles } from "./ACLExpressionHelper";

const {
  aclExpressionBuilder: { match: matchLabel, addGroup: addGroupLabel },
} = languageStrings;

export interface ACLTreeOperatorProps extends TreeItemProps {
  /**
   * Operator name.
   */
  operator: ACLOperatorType;
  /**
   * Available operators for the top level expression. Default to `AND` and `OR`.
   */
  rootGroupOperators?: ACLOperatorType[];
  /**
   * Available operators for the common level expressions. Default to AND and OR and NOT.
   */
  groupOperators?: ACLOperatorType[];
  /**
   * `true` if an operator is a root operator which appears on the top of the tree without a delete button.
   */
  isRoot?: boolean;
  /**
   * Fired when the item is deleted.
   */
  onDelete: (nodeID: string) => void;
  /**
   * Fired when the operator is changed.
   */
  onOperatorChange: (operator: ACLOperatorType) => void;
}

/**
 * Tree item (node) which represents an ACL operator.
 */
export const ACLTreeOperator = ({
  nodeId,
  rootGroupOperators = ["OR", "AND"],
  groupOperators = ["OR", "AND", "NOT"],
  isRoot,
  operator,
  onSelect,
  onDelete,
  onOperatorChange,
  ...other
}: ACLTreeOperatorProps): JSX.Element => {
  const classes = useACLTreeItemStyles();

  // default selected value is `OR`
  const buildMenuItemForOperator = (operator: ACLOperatorType) => (
    <MenuItem
      key={operator}
      selected={operator === "OR" ? true : false}
      value={operator}
    >
      {getOperatorLabel(operator)}
    </MenuItem>
  );

  const treeOperatorLabel = () => (
    <div className={classes.labelRoot}>
      <InputLabel>{matchLabel}&nbsp;</InputLabel>
      <Select
        id={`${nodeId}-select`}
        value={operator}
        onChange={(event) =>
          onOperatorChange(event.target.value as ACLOperatorType)
        }
        // prevent toggling tree item
        onClick={(event) => event.stopPropagation()}
        className={classes.labelSelect}
      >
        {pipe(
          isRoot ? rootGroupOperators : groupOperators,
          A.map(buildMenuItemForOperator)
        )}
      </Select>
      <TooltipIconButton
        onClick={(event) => {
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
            onDelete(nodeId);
            // prevent toggling tree item
            event.stopPropagation();
          }}
        >
          <DeleteIcon />
        </TooltipIconButton>
      )}
    </div>
  );

  return (
    <TreeItem
      id={nodeId}
      nodeId={nodeId}
      label={treeOperatorLabel()}
      onClick={onSelect}
      classes={{
        root: classes.root,
      }}
      {...other}
    />
  );
};
