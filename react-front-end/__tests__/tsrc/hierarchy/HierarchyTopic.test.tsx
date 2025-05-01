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
import { SimpleTreeView } from "@mui/x-tree-view/SimpleTreeView";
import * as OEQ from "@openequella/rest-api-client";
import { render } from "@testing-library/react";
import { createMemoryHistory } from "history";
import * as React from "react";
import { Router } from "react-router-dom";
import {
  topicWithChildren,
  topicWithoutSearchResults,
} from "../../../__mocks__/Hierarchy.mock";
import "@testing-library/jest-dom";
import HierarchyTopic from "../../../tsrc/hierarchy/components/HierarchyTopic";

const renderHierarchyTopic = (
  topic: OEQ.BrowseHierarchy.HierarchyTopicSummary,
) => {
  const history = createMemoryHistory();
  return render(
    <Router history={history}>
      <SimpleTreeView>
        <HierarchyTopic topic={topic} expandedNodes={[]} />
      </SimpleTreeView>
    </Router>,
  );
};

describe("<HierarchyTopic />", () => {
  it.each([
    ["Display count number if `showResult` is true", true, 1],
    ["Do not display count number if `showResult` is false", false, 0],
  ])("%s", async (_, showResult, expectedLength) => {
    const topic = showResult ? topicWithChildren : topicWithoutSearchResults;
    const { container } = renderHierarchyTopic(topic);

    expect(
      container.getElementsByClassName("HierarchyTopic-count"),
    ).toHaveLength(expectedLength);
  });
});
