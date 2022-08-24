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
import * as OEQ from "@openequella/rest-api-client";
import { action } from "@storybook/addon-actions";
import { Meta, Story } from "@storybook/react";
import * as React from "react";
import { liveStatuses, nonLiveStatuses } from "../../tsrc/modules/SearchModule";
import StatusSelector, {
  StatusSelectorProps,
} from "../../tsrc/search/components/StatusSelector";

export default {
  title: "Search/StatusSelector",
  component: StatusSelector,
} as Meta<StatusSelectorProps>;

const onChange = action("onChange");

const basicSelectorParams = (
  value: OEQ.Common.ItemStatus[] = liveStatuses,
  advancedUse = false
): StatusSelectorProps => ({
  value,
  advancedUse,
  onChange,
});

export const BasicSelectorLiveOnly: Story<StatusSelectorProps> = (args) => (
  <StatusSelector {...args} />
);
BasicSelectorLiveOnly.args = basicSelectorParams();

export const BasicSelectorAllStatuses: Story<StatusSelectorProps> = (args) => (
  <StatusSelector {...args} />
);
BasicSelectorAllStatuses.args = basicSelectorParams(
  liveStatuses.concat(nonLiveStatuses)
);

export const DefaultAdvancedSelector: Story<StatusSelectorProps> = (args) => (
  <StatusSelector {...args} />
);
DefaultAdvancedSelector.args = basicSelectorParams(
  ["LIVE", "PERSONAL", "DRAFT"],
  true
);

export const AdvancedSelectorCustomOptions: Story<StatusSelectorProps> = (
  args
) => <StatusSelector {...args} />;
AdvancedSelectorCustomOptions.args = {
  ...DefaultAdvancedSelector.args,
  value: ["REVIEW"],
  options: ["MODERATING", "REVIEW", "REJECTED"],
};
