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
import { getByLabelText, render } from "@testing-library/react";
import "@testing-library/jest-dom";
import * as React from "react";
import {
  getHierarchies,
  getHierarchyIdsWithKeyResource,
  topicWithShortAndLongDesc,
} from "../../../../__mocks__/Hierarchy.mock";
import * as mockData from "../../../../__mocks__/searchresult_mock_data";
import ModifyKeyResourceDialog, {
  ModifyKeyResourceDialogProps,
} from "../../../../tsrc/search/components/ModifyKeyResourceDialog";
import { languageStrings } from "../../../../tsrc/util/langstrings";

const { viewHierarchy } = languageStrings.hierarchy;
const { remove, add } = languageStrings.searchpage.addToHierarchy;

describe("<ModifyKeyResourceDialog />", () => {
  const defaultProps: ModifyKeyResourceDialogProps = {
    item: mockData.basicSearchObj,
    open: true,
    onClose: () => {},
    getHierarchiesProvider: getHierarchies,
    getHierarchyIdsWithKeyResourceProvider: getHierarchyIdsWithKeyResource,
  };

  const renderModifyKeyResourceDialog = async (props = defaultProps) => {
    const result = render(<ModifyKeyResourceDialog {...props} />);
    const allHierarchies = await props.getHierarchiesProvider!();
    await result.findByText(allHierarchies[0].name!);
    return result;
  };

  it("shows hierarchy tree", async () => {
    const { getByLabelText } = await renderModifyKeyResourceDialog();

    expect(getByLabelText(viewHierarchy)).toBeInTheDocument();
  });

  it("shows remove button for hierarchy with the key resource", async () => {
    const hierarchyUuid = topicWithShortAndLongDesc.compoundUuid;
    const getHierarchiesProvider = () =>
      Promise.resolve([topicWithShortAndLongDesc]);
    const getHierarchyIdsWithKeyResourceProvider = () =>
      Promise.resolve([hierarchyUuid]);

    const { getByTestId } = await renderModifyKeyResourceDialog({
      ...defaultProps,
      getHierarchiesProvider,
      getHierarchyIdsWithKeyResourceProvider,
    });

    expect(
      getByLabelText(getByTestId(hierarchyUuid), remove),
    ).toBeInTheDocument();
  });

  it("shows add button for hierarchy without the key resource", async () => {
    const hierarchyUuid = topicWithShortAndLongDesc.compoundUuid;
    const getHierarchiesProvider = () =>
      Promise.resolve([topicWithShortAndLongDesc]);
    const getHierarchyIdsWithKeyResourceProvider = () => Promise.resolve([]);

    const { getByTestId } = await renderModifyKeyResourceDialog({
      ...defaultProps,
      getHierarchiesProvider,
      getHierarchyIdsWithKeyResourceProvider,
    });

    expect(getByLabelText(getByTestId(hierarchyUuid), add)).toBeInTheDocument();
  });
});
