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
import "@testing-library/jest-dom/extend-expect";
import type { ACLExpression } from "../../../../tsrc/modules/ACLExpressionModule";
import { ACLRecipientTypes } from "../../../../tsrc/modules/ACLRecipientModule";
import {
  renderACLExpressionTree,
  selectOperatorNode,
} from "./ACLExpressionTreeTestHelper";

describe("<ACLExpressionTree/>", () => {
  const rootACLExpression: ACLExpression = {
    id: "root",
    operator: "OR",
    recipients: [
      {
        type: ACLRecipientTypes.User,
        expression: "user-id-1",
        name: "User 1 [user1]",
      },
    ],
    children: [],
  };

  const childACLExpression: ACLExpression = {
    id: "child",
    operator: "AND",
    recipients: [
      {
        type: ACLRecipientTypes.User,
        expression: "group-id-1",
        name: "Group 1",
      },
    ],
    children: [],
  };

  it("displays the operator and recipient tree item", async () => {
    const { container, queryByText } =
      renderACLExpressionTree(rootACLExpression);

    // expend root node
    await selectOperatorNode(container, "root");

    expect(queryByText("Or")).toBeInTheDocument();
    expect(queryByText("User 1 [user1]")).toBeInTheDocument();
  });

  it("displays the child in the tree view", async () => {
    const { container, queryByText } = renderACLExpressionTree({
      ...rootACLExpression,
      children: [childACLExpression],
    });

    // expend root node
    await selectOperatorNode(container, "root");
    // expend child node
    await selectOperatorNode(container, "child");

    expect(queryByText("And")).toBeInTheDocument();
    expect(queryByText("Group 1")).toBeInTheDocument();
  });
});
