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
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import ArrowRightIcon from "@mui/icons-material/ArrowRight";
import { TreeView } from "@mui/x-tree-view/TreeView";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as S from "fp-ts/string";
import * as React from "react";
import { ChangeEvent, useState } from "react";
import type { ACLEntityResolvers } from "../../modules/ACLEntityModule";
import type {
  ACLExpression,
  ACLOperatorType,
} from "../../modules/ACLExpressionModule";
import {
  createACLExpression,
  flattenIds,
} from "../../modules/ACLExpressionModule";
import type { ACLRecipient } from "../../modules/ACLRecipientModule";
import { recipientEq, showRecipient } from "../../modules/ACLRecipientModule";
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
  onDelete: (expression: ACLExpression) => void;
  /**
   * Fired when the ACLExpression tree view is changed. (Such as delete recipient or update the operator)
   */
  onChange: (expression: ACLExpression) => void;
  /***
   * Functions to lookup user, group and role entities.
   */
  aclEntityResolvers?: ACLEntityResolvers;
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
  aclEntityResolvers,
}: ACLExpressionTreeProps): JSX.Element => {
  // expand all nodes by default
  const [expanded, setExpanded] = useState<string[]>(flattenIds(aclExpression));
  const [selected, setSelected] = useState<string[]>([]);

  const handleTreeToggle = (_: ChangeEvent<object>, nodeIds: string[]) =>
    setExpanded(nodeIds);

  const handleTreeSelect = (_: React.SyntheticEvent, nodeIds: string[]) =>
    setSelected(nodeIds);

  const parseACLExpression = (aclExpression: ACLExpression, isRoot = false) => {
    const { id, operator, children, recipients } = aclExpression;

    const handleRecipientDelete = (recipient: ACLRecipient) => {
      const newRecipients = pipe(
        recipients,
        A.filter((r) => !recipientEq.equals(r, recipient)),
      );

      onChange({
        ...aclExpression,
        recipients: newRecipients,
      });
    };

    const handleAddGroup = () => {
      const newACLExpression = createACLExpression("OR");

      onChange({
        ...aclExpression,
        children: [...children, newACLExpression],
      });
      // expand current and new group node for user
      setExpanded(
        pipe(
          [...expanded, aclExpression.id, newACLExpression.id],
          A.uniq(S.Eq),
        ),
      );
      // select new group node for user
      // `setSelected` won't trigger the `onSelect` event for operator node, but it can update the UI.
      setSelected([newACLExpression.id]);
      onSelect(newACLExpression);
    };

    return (
      <ACLTreeOperator
        key={id}
        nodeId={id}
        operator={operator}
        isRoot={isRoot}
        onSelect={() => onSelect(aclExpression)}
        onDelete={() => onDelete(aclExpression)}
        onOperatorChange={(newOperator: ACLOperatorType) => {
          onChange({
            ...aclExpression,
            operator: newOperator,
          });
        }}
        onAddGroup={handleAddGroup}
      >
        {recipients.map((recipient: ACLRecipient) => {
          const nodeId = showRecipient(recipient);

          return (
            <ACLTreeRecipient
              key={nodeId}
              nodeId={nodeId}
              recipient={recipient}
              onDelete={() => handleRecipientDelete(recipient)}
              aclEntityResolvers={aclEntityResolvers}
            ></ACLTreeRecipient>
          );
        })}
        {children.map((aclEx) => parseACLExpression(aclEx))}
      </ACLTreeOperator>
    );
  };

  return (
    <TreeView
      multiSelect
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
