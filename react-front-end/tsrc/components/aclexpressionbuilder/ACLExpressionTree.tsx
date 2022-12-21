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
import { TreeView } from "@material-ui/lab";
import * as React from "react";
import { useState, ChangeEvent } from "react";
import ArrowDropDownIcon from "@material-ui/icons/ArrowDropDown";
import ArrowRightIcon from "@material-ui/icons/ArrowRight";
import type {
  ACLExpression,
  ACLOperatorType,
} from "../../modules/ACLExpressionModule";
import type { ACLRecipient } from "../../modules/ACLRecipientModule";
import { ACLTreeOperator } from "./ACLTreeOperator";
import { ACLTreeRecipient } from "./ACLTreeRecipient";

export interface ACLExpressionTreeProps {
  /**
   * ACLExpression object
   */
  aclExpression: ACLExpression;
  /**
   * Fired when the tree item is selected. Only `ACLTreeOperator` can be selected.
   */
  onSelect: (expression: ACLExpression) => void;
  /**
   * Fired when the tree item is deleted. Both `ACLTreeOperator` and `ACLTreeRecipient` can be deleted.
   */
  onDelete: (nodeID: string) => void;
  /**
   * Fired when the ACLExpression tree view is changed. (Such as delete recipient or update the operator)
   */
  onChange: (expression: ACLExpression) => void;
}

/**
 * A tree view shows the whole structure of an ACL Expression.
 * It will render `Operator` as parent node and `Recipient` as child node.
 * */
const ACLExpressionTree = ({
  aclExpression,
  onSelect,
  onDelete,
  onChange,
}: ACLExpressionTreeProps): JSX.Element => {
  const [expanded, setExpanded] = useState<string[]>([]);
  const [selected, setSelected] = useState<string[]>([]);

  const handleTreeToggle = (_: ChangeEvent<{}>, nodeIds: string[]) =>
    setExpanded(nodeIds);

  const handleTreeSelect = (_: ChangeEvent<{}>, nodeIds: string[]) =>
    setSelected(nodeIds);

  const handleTreeRecipientDelete = (nodeId: string) => {
    // TODO: handle delete action
  };

  const parseACLExpression = (aclExpression: ACLExpression, isRoot = false) => {
    const { id, operator, children, recipients } = aclExpression;

    return (
      <ACLTreeOperator
        key={id}
        nodeId={id}
        operator={operator}
        isRoot={isRoot}
        onSelect={() => onSelect(aclExpression)}
        onDelete={onDelete}
        onOperatorChange={(newOperator: ACLOperatorType) => {
          onChange({
            ...aclExpression,
            operator: newOperator,
          });
        }}
      >
        {recipients.map(({ type, expression, name }: ACLRecipient) => {
          const unnamedValue = `${type} : ${expression}`;

          return (
            <ACLTreeRecipient
              key={unnamedValue}
              nodeId={unnamedValue}
              expressionName={name ?? unnamedValue}
              onDelete={handleTreeRecipientDelete}
            ></ACLTreeRecipient>
          );
        })}
        {children.map((aclEx) => parseACLExpression(aclEx))}
      </ACLTreeOperator>
    );
  };

  return (
    <TreeView
      defaultCollapseIcon={<ArrowDropDownIcon />}
      defaultExpandIcon={<ArrowRightIcon />}
      expanded={expanded}
      selected={selected}
      onNodeToggle={handleTreeToggle}
      onNodeSelect={handleTreeSelect}
    >
      {parseACLExpression(aclExpression, true)}
    </TreeView>
  );
};

export default ACLExpressionTree;
