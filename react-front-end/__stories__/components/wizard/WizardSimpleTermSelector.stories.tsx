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
import * as React from "react";
import { Meta, StoryFn } from "@storybook/react";
import { mockedSearchTaxonomyTerms } from "../../../__mocks__/TaxonomyTerms.mock";
import {
  WizardSimpleTermSelector,
  WizardSimpleTermSelectorProps,
} from "../../../tsrc/components/wizard/WizardSimpleTermSelector";

export default {
  title: "Component/Wizard/WizardSimpleTermSelector",
  component: WizardSimpleTermSelector,
  argTypes: {
    onSelect: {
      action: "onSelect called",
    },
  },
} as Meta<WizardSimpleTermSelectorProps>;

export const NoValues: StoryFn<WizardSimpleTermSelectorProps> = (args) => (
  <WizardSimpleTermSelector {...args} />
);
NoValues.args = {
  id: "wizard-simple-term-selector-story",
  label: "Example",
  description: "This an example of Simple Term Selector",
  mandatory: true,
  values: new Set(),
  termProvider: mockedSearchTaxonomyTerms,
};

export const WithValues: StoryFn<WizardSimpleTermSelectorProps> = (args) => (
  <WizardSimpleTermSelector {...args} />
);
WithValues.args = {
  ...NoValues.args,
  values: new Set(["term1", "term2", "term3"]),
};
