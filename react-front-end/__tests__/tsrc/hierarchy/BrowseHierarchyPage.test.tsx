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
  getRootHierarchies,
  getSubHierarchies,
  rootHierarchies,
  topicWithHideNoResultChild,
  topicWithNoResultChild,
  topicWithShortAndLongDesc,
  virtualTopics,
} from "../../../__mocks__/Hierarchy.mock";
import BrowseHierarchyPage from "../../../tsrc/hierarchy/BrowseHierarchyPage";
import "@testing-library/jest-dom";
import { selectHierarchy } from "./HierarchyTestHelper";

jest.mock("../../../tsrc/modules/HierarchyModule", () => ({
  getRootHierarchies: jest.fn(getRootHierarchies),
  getSubHierarchies: jest.fn(getSubHierarchies),
}));

const renderBrowseHierarchyPage = async (): Promise<RenderResult> => {
  const history = createMemoryHistory();
  const result = render(
    <Router history={history}>
      <BrowseHierarchyPage updateTemplate={jest.fn()} />
    </Router>,
  );
  await result.findByText(rootHierarchies[0].name!);
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
    const subHierarchyTopics = await getSubHierarchies(
      virtualTopics.compoundUuid,
    );
    const { container, getByText } = await renderBrowseHierarchyPage();

    await selectHierarchy(container, virtualTopics.name!);

    subHierarchyTopics.forEach(({ name }) =>
      expect(getByText(name!)).toBeInTheDocument(),
    );
    expect.assertions(subHierarchyTopics.length);
  });

  it("hide sub hierarchy topic if it has no result when `hideSubtopicsWithNoResults` is set to `true`", async () => {
    const subHierarchyTopics = await getSubHierarchies(
      topicWithHideNoResultChild.compoundUuid,
    );

    const { container, queryByText } = await renderBrowseHierarchyPage();

    await selectHierarchy(container, topicWithHideNoResultChild.name!);

    expect(queryByText(subHierarchyTopics[0].name!)).not.toBeInTheDocument();
  });

  it("show sub hierarchy topic even if it has no result when `hideSubtopicsWithNoResults` is set to `false`", async () => {
    const subHierarchyTopics = await getSubHierarchies(
      topicWithNoResultChild.compoundUuid,
    );

    const { container, queryByText } = await renderBrowseHierarchyPage();

    await selectHierarchy(container, topicWithNoResultChild.name!);

    expect(queryByText(subHierarchyTopics[0].name!)).toBeInTheDocument();
  });

  it.each([
    { text: "show", topic: topicWithNoResultChild, show: true },
    { text: "hide", topic: topicWithHideNoResultChild, show: false },
  ])(
    "should $text sub hierarchy topic if `hideSubtopicsWithNoResults` is set to $show",
    async ({ topic, show }) => {
      const subHierarchyTopics = await getSubHierarchies(topic.compoundUuid);

      const { container, queryByText } = await renderBrowseHierarchyPage();

      await selectHierarchy(container, topic.name!);

      const isPresent = !!queryByText(subHierarchyTopics[0].name!);
      expect(isPresent).toBe(show);
    },
  );
});
