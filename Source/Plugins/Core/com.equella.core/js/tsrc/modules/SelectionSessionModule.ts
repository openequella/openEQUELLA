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
import { AppConfig, getRenderData, SelectionSessionInfo } from "../AppConfig";

const selectionSessionInfo:
  | SelectionSessionInfo
  | undefined
  | null = getRenderData()?.selectionSessionInfo;

/**
 * Type guard to check whether an object is of type SelectionSessionInfo.
 * @param data The data to be checked
 */
const isSelectionSession = (
  data: unknown
): data is { [K in keyof SelectionSessionInfo]: unknown } =>
  typeof data === "object" && data !== null && "stateId" in data;

/**
 * True if the Selection Session info provided by renderData is neither null nor undefined.
 */
export const inSelectionSession = isSelectionSession(selectionSessionInfo);

/**
 * Build a Selection Session specific ItemSummary Link.
 * @param uuid The UUID of an Item
 * @param version The version of an Item
 */
export const buildSelectionSessionItemSummaryLink = (
  uuid: string,
  version: number
): string => {
  if (isSelectionSession(selectionSessionInfo)) {
    const { stateId, integId, layout } = selectionSessionInfo;
    const itemSummaryPageLink = AppConfig.baseUrl.concat(
      `items/${uuid}/${version}/?_sl.stateId=${stateId}&a=${layout}`
    );

    // integId can be null in 'Resource Selector'.
    if (integId) {
      return itemSummaryPageLink.concat(`&_int.id=${integId}`);
    }
    return itemSummaryPageLink;
  } else {
    throw new TypeError("The type of Selection Session Info is incorrect.");
  }
};
