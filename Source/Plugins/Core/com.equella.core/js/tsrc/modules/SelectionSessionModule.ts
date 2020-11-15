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
import { AppConfig, SelectionSessionInfo } from "../AppConfig";
/**
 * Build a Selection Session specific ItemSummary Link.
 * @param selectionSessionInfo An object containing information of a Selection Session such as the layout and ID
 * @param uuid The UUID of an Item
 * @param version The version of an Item
 */
export const buildSelectionSessionItemSummaryLink = (
  selectionSessionInfo: SelectionSessionInfo,
  uuid: string,
  version: number
): string => {
  const { stateId, integId, layout } = selectionSessionInfo;
  const itemSummaryPageLink = AppConfig.baseUrl.concat(
    `items/${uuid}/${version}/?_sl.stateId=${stateId}&a=${layout}`
  );

  // integId can be null in 'Resource Selector'.
  if (integId) {
    return itemSummaryPageLink.concat(`&_int.id=${integId}`);
  }
  return itemSummaryPageLink;
};
