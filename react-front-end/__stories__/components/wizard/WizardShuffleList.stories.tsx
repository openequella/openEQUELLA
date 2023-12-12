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
import { Meta, StoryFn } from "@storybook/react";
import * as React from "react";
import {
  WizardShuffleList,
  WizardShuffleListProps,
} from "../../../tsrc/components/wizard/WizardShuffleList";

export default {
  title: "Component/Wizard/WizardShuffleList",
  component: WizardShuffleList,
  argTypes: {
    onChange: {
      action: "onChange called",
    },
  },
} as Meta<WizardShuffleListProps>;

export const NoValues: StoryFn<WizardShuffleListProps> = (args) => (
  <WizardShuffleList {...args} />
);
NoValues.args = {
  id: "wizard-shufflelist-story",
  label: "WizardShuffleList",
  description: "A Shuffle List with no values set",
  values: new Set<string>([]),
};

export const WithValues: StoryFn<WizardShuffleListProps> = (args) => (
  <WizardShuffleList {...args} />
);
WithValues.args = {
  ...NoValues.args,
  description:
    "A Shuffle List with values provided - they should be nicely sorted",
  values: new Set<string>(["first one", "second one", "and another"]),
};
