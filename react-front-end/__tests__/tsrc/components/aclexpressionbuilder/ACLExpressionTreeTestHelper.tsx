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
import { render, RenderResult } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import ACLExpressionTree from "../../../../tsrc/components/aclexpressionbuilder/ACLExpressionTree";
import {
  ACLExpression,
  ACLOperatorType,
  getOperatorLabel,
} from "../../../../tsrc/modules/ACLExpressionModule";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import { selectOption } from "../../MuiTestHelpers";

const { delete: deleteLabel } = languageStrings.common.action;

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

// Helper function to mock select the arrow expend icon for each group node (`operator` tree item) in the tree view.
export const selectOperatorNode = (container: HTMLElement, nodeId: string) => {
  const node = container.querySelector(`#${nodeId} svg`);
  if (!node) {
    throw Error(`Can't find node with id: ${nodeId}`);
  }
  userEvent.click(node);
};

// Helper function to mock select an `operator` for group node (`operator` tree item) in the tree view.
export const selectOperatorForNode = async (
  container: HTMLElement,
  nodeId: string,
  operator: ACLOperatorType
) => selectOption(container, `#${nodeId}-select`, getOperatorLabel(operator));

// Helper function to mock click the delete button for a gaven recipient (located by name) in the tree view.
export const clickDeleteButtonForRecipient = async (
  { findByText }: RenderResult,
  name: string
) => {
  const deleteButton = (await findByText(name)).nextElementSibling;
  if (!deleteButton) {
    throw Error(`Can't find delete button for recipient with name: ${name}`);
  }

  userEvent.click(deleteButton);
};

// Helper function to mock click the delete button for the operator node in the tree view.
export const clickDeleteButtonForOperatorNode = (
  container: HTMLElement,
  nodeId: string
) => {
  const node = container.querySelector(
    `#${nodeId} button[aria-label=${deleteLabel}]`
  );
  if (!node) {
    throw Error(`Can't find delete button for node with id: ${nodeId}`);
  }
  userEvent.click(node);
};
