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
import * as React from "react";
import SelectGroupDialog, {
  SelectGroupDialogProps,
} from "../../../../tsrc/components/securityentitydialog/SelectGroupDialog";
import {
  searchGroups,
  findGroupsByIds,
} from "../../../../__mocks__/GroupModule.mock";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import {
  doSearch,
  searchAndSelect,
  waitForEntityDialogToRender,
} from "./SelectEntityDialogTestHelper";
import * as OEQ from "@openequella/rest-api-client";

const { queryFieldLabel } = languageStrings.groupSearchComponent;

export const commonSelectGroupDialogProps: SelectGroupDialogProps = {
  open: true,
  value: new Set<OEQ.Common.UuidString>(),
  onClose: jest.fn(),
  searchGroupsProvider: searchGroups,
  findGroupsByIdsProvider: findGroupsByIds,
};

/**
 * Helper to render SelectGroupDialog.
 */
export const renderSelectGroupDialog = async (
  props: SelectGroupDialogProps = commonSelectGroupDialogProps,
): Promise<RenderResult> => {
  const result = render(<SelectGroupDialog {...props} />);
  await waitForEntityDialogToRender(result);
  return result;
};

const searchGroup = (dialog: HTMLElement, queryName: string) =>
  doSearch(dialog, queryFieldLabel, queryName);

/**
 * Do search and select a group in SelectGroupDialog.
 */
export const searchAndSelectGroup = (
  dialog: HTMLElement,
  searchFor: string,
  selectEntityName: string,
) => searchAndSelect(dialog, searchFor, selectEntityName, searchGroup);
