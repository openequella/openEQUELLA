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
// Helper to render ACLExpressionBuilder and wait for component under test
import { render, RenderResult } from "@testing-library/react";
import { createMemoryHistory } from "history";
import * as React from "react";
import { Router } from "react-router-dom";
import {
  hierarchies,
  topicWithHideNoResultChild,
  topicWithNoResultChild,
  topicWithShortAndLongDesc,
  virtualTopics,
} from "../../../__mocks__/Hierarchy.mock";
import BrowseHierarchyPage from "../../../tsrc/hierarchy/BrowseHierarchyPage";
import "@testing-library/jest-dom";
import { selectHierarchy } from "./HierarchyTestHelper";

jest.mock("../../../tsrc/modules/HierarchyModule", () => ({
  getHierarchies: jest.fn(() => Promise.resolve(hierarchies)),
}));

const renderBrowseHierarchyPage = async (): Promise<RenderResult> => {
  const history = createMemoryHistory();
  const result = render(
    <Router history={history}>
      <BrowseHierarchyPage updateTemplate={jest.fn()} />
    </Router>,
  );
  await result.findByText(hierarchies[0].name!);
  return result;
};

describe("<BrowseHierarchyPage/>", () => {
  it("displays root hierarchy topic", async () => {
    const { getByText } = await renderBrowseHierarchyPage();

    expect(getByText(topicWithShortAndLongDesc.name!)).toBeInTheDocument();
    expect(
      getByText(topicWithShortAndLongDesc.shortDescription!),
    ).toBeInTheDocument();
  });

  it("displays sub hierarchy topic", async () => {
    const { container, getByText } = await renderBrowseHierarchyPage();

    await selectHierarchy(container, virtualTopics.name!);

    virtualTopics.subHierarchyTopics.forEach(({ name }) =>
      expect(getByText(name!)).toBeInTheDocument(),
    );
    expect.assertions(virtualTopics.subHierarchyTopics.length);
  });

  it("hide sub hierarchy topic if it has no result when `hideSubtopicsWithNoResults` is set to `true`", async () => {
    const { container, queryByText } = await renderBrowseHierarchyPage();

    await selectHierarchy(container, topicWithHideNoResultChild.name!);

    expect(
      queryByText(topicWithHideNoResultChild.subHierarchyTopics[0].name!),
    ).not.toBeInTheDocument();
  });

  it("show sub hierarchy topic even if it has no result when `hideSubtopicsWithNoResults` is set to `false`", async () => {
    const { container, queryByText } = await renderBrowseHierarchyPage();

    await selectHierarchy(container, topicWithNoResultChild.name!);

    expect(
      queryByText(topicWithNoResultChild.subHierarchyTopics[0].name!),
    ).toBeInTheDocument();
  });

  it.each([
    { text: "show", topic: topicWithNoResultChild, show: true },
    { text: "hide", topic: topicWithHideNoResultChild, show: false },
  ])(
    "should $text sub hierarchy topic if `hideSubtopicsWithNoResults` is set to $show",
    async ({ topic, show }) => {
      const { container, queryByText } = await renderBrowseHierarchyPage();

      await selectHierarchy(container, topic.name!);

      const isPresent = !!queryByText(topic.subHierarchyTopics[0].name!);
      expect(isPresent).toBe(show);
    },
  );
});
