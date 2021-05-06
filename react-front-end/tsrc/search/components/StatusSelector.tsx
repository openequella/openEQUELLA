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
import { Button, ButtonGroup } from "@material-ui/core";
import * as OEQ from "@openequella/rest-api-client";
import { isEqual } from "lodash";
import * as React from "react";
import { liveStatuses, nonLiveStatuses } from "../../modules/SearchModule";
import { languageStrings } from "../../util/langstrings";

export interface StatusSelectorProps {
  /**
   * A list of the currently selected statuses. This list is then used to determine one of two
   * possible sets: live OR all. The main reason to not simply abstract this out to a boolean, is
   * to support the easy passing and storing of a value used in calls to the `SearchModule`.
   */
  value?: OEQ.Common.ItemStatus[];
  /**
   * Handler to call when the selection is modified. The resulting value being what would be passed
   * back as `value` for future renderings.
   *
   * @param value a list representing the new selection.
   */
  onChange: (value: OEQ.Common.ItemStatus[]) => void;
}

/**
 * A button toggle to provide a simple means of either seeing items which are 'live' or otherwise
 * all. To do this, it basically considers that if the provided `value` contains only those known
 * as live then then status is 'live', otherwise it's all. Very simplistic.
 */
const StatusSelector = ({
  value = liveStatuses,
  onChange,
}: StatusSelectorProps) => {
  // iff it contains only those specified in the live status list then we consider
  // the selection to be the 'live' option, otherwise we will go with 'all'
  const isLive = (statusList: OEQ.Common.ItemStatus[]): boolean =>
    isEqual(statusList, liveStatuses);
  const variant = (determiner: () => boolean) =>
    determiner() ? "contained" : "outlined";

  return (
    <ButtonGroup color="secondary">
      <Button
        variant={variant(() => isLive(value))}
        onClick={() => onChange(liveStatuses)}
      >
        {languageStrings.searchpage.statusSelector.live}
      </Button>
      <Button
        variant={variant(() => !isLive(value))}
        onClick={() => onChange(liveStatuses.concat(nonLiveStatuses))}
      >
        {languageStrings.searchpage.statusSelector.all}
      </Button>
    </ButtonGroup>
  );
};

export default StatusSelector;
