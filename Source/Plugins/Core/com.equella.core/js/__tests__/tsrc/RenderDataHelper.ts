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
import * as AppConfig from "../../tsrc/AppConfig";
import { RenderData, SelectionSessionInfo } from "../../tsrc/AppConfig";

/**
 * The intention of creating this helper is to help Jest tests mock the global
 * object 'renderData' more easily.
 *
 * This helper provides objects of 'RenderData' and 'SelectionSessionInfo',
 * and a function which can change what the mocked function 'getRenderData' returns.
 */

export const basicSelectionSessionInfo: SelectionSessionInfo = {
  stateId: "1",
  layout: "coursesearch",
  isSelectSummaryButtonDisabled: false,
};

export const withIntegId: SelectionSessionInfo = {
  ...basicSelectionSessionInfo,
  integId: "2",
};

export const selectSummaryButtonDisabled: SelectionSessionInfo = {
  ...basicSelectionSessionInfo,
  isSelectSummaryButtonDisabled: true,
};

export const basicRenderData: RenderData = {
  baseResources: "p/r/2020.2.0/com.equella.core/",
  newUI: true,
  autotestMode: false,
  newSearch: true,
  selectionSessionInfo: basicSelectionSessionInfo,
  viewedFromIntegration: false,
};

export const renderDataForSelectOrAdd: RenderData = {
  ...basicRenderData,
  selectionSessionInfo: {
    ...basicSelectionSessionInfo,
    layout: "search",
  },
};

export const renderDataForSkinny: RenderData = {
  ...basicRenderData,
  selectionSessionInfo: {
    ...basicSelectionSessionInfo,
    layout: "skinnysearch",
  },
};

const mockGetRenderData = jest.spyOn(AppConfig, "getRenderData");
export const updateMockGetRenderData = (renderData?: RenderData) => {
  mockGetRenderData.mockReturnValue(renderData);
};
