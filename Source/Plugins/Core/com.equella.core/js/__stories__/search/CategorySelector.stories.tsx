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
import type { SelectedCategories } from "../../tsrc/modules/SearchFacetsModule";
import {
  CategorySelector,
  CategorySelectorProps,
} from "../../tsrc/search/components/CategorySelector";
import * as CategorySelectorMock from "../../__mocks__/CategorySelector.mock";
import type { Meta, Story } from "@storybook/react";

export default {
  title: "Search/CategorySelector",
  component: CategorySelector,
  argTypes: {
    onSelectedCategoriesChange: { action: "on select categories" },
  },
} as Meta<CategorySelectorProps>;

const CategorySelectorTemplate = (args: CategorySelectorProps) => (
  <CategorySelector {...args} />
);
const selectedCategories: SelectedCategories[] = [
  { id: 766942, categories: ["scala", "java"] },
  { id: 766943, categories: ["Hobart"] },
];

export const noCategoriesSelected: Story<CategorySelectorProps> = CategorySelectorTemplate.bind(
  {}
);
noCategoriesSelected.args = {
  classifications: CategorySelectorMock.classifications,
};

export const categoriesSelected: Story<CategorySelectorProps> = CategorySelectorTemplate.bind(
  {}
);
categoriesSelected.args = {
  ...noCategoriesSelected.args,
  selectedCategories: selectedCategories,
};
