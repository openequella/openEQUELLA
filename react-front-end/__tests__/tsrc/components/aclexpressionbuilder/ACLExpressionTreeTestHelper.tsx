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
  findByText,
  getAllByTestId,
  render,
  RenderResult,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { pipe } from "fp-ts/function";
import * as React from "react";
import ACLExpressionTree from "../../../../tsrc/components/aclexpressionbuilder/ACLExpressionTree";
import { classes } from "../../../../tsrc/components/aclexpressionbuilder/ACLTreeItem";
import {
  ACLExpression,
  ACLOperatorType,
  getOperatorLabel,
} from "../../../../tsrc/modules/ACLExpressionModule";
import { getSelectOption } from "../../MuiTestHelpers";

// Helper to render ACLExpressionTree
export const renderACLExpressionTree = (
  aclExpression: ACLExpression
): RenderResult =>
  render(
    <ACLExpressionTree
      aclExpression={aclExpression}
      onSelect={jest.fn()}
      onDelete={jest.fn()}
      onChange={jest.fn()}
    />
  );

/**
 * Helper function to mock select the arrow expend icon for each group node (`operator` tree item) in the tree view.
 *
 * @param container The element which contains the element.
 * @param nodeIndex The index represents the position of the node in the current `displayed` tree
 * (not the real tree structure, some nodes may not be in the dom tree until their parent node is expended).
 */
export const selectOperatorNode = async (
  container: HTMLElement,
  nodeIndex: number
) => {
  const nodes = getAllByTestId(container, "ACLTreeOperator-label");
  const operator = nodes[nodeIndex];

  if (!operator) {
    throw Error(`Can't find operator node: ${nodeIndex}`);
  }

  await userEvent.click(operator);
};

/**
 * Helper function to mock select an `operator` for group node (`operator` tree item) in the tree view.
 * @param container The element which contains the element.
 * @param nodeIndex The index represents the node position in the current `displayed` tree
 *                  (not the real tree structure,
 *                  some nodes may not be in the dom tree until their parent node is expended ).
 * @param operator The expected option to select.
 */
export const selectOperatorForNode = async (
  container: HTMLElement,
  nodeIndex: number,
  operator: ACLOperatorType
) => {
  const selects = container.querySelectorAll(
    `.${classes.labelSelect} div[role="button"]`
  );
  const select = selects[nodeIndex];

  if (!select) {
    throw Error(`Can't find operator select: ${nodeIndex}`);
  }

  await userEvent.click(select);
  // Click the option in the list
  await userEvent.click(pipe(operator, getOperatorLabel, getSelectOption));
};

// Helper function to mock click the delete button for a gaven recipient (located by name) in the tree view.
export const clickDeleteButtonForRecipient = async (
  container: HTMLElement,
  name: string
) => {
  const deleteButton = (await findByText(container, name)).nextElementSibling;
  if (!deleteButton) {
    throw Error(`Can't find delete button for recipient with name: ${name}`);
  }

  await userEvent.click(deleteButton);
};

// Helper function to mock click the delete button for the operator node in the tree view.
export const clickDeleteButtonForOperatorNode = async (
  container: HTMLElement,
  nodeIndex: number
) => {
  const nodes = getAllByTestId(container, "ACLTreeOperator-delete");
  // the root node doesn't have `delete` button
  const node = nodes[nodeIndex - 1];

  if (!node) {
    throw Error(`Can't find delete button for node: ${nodeIndex}`);
  }
  await userEvent.click(node);
};
