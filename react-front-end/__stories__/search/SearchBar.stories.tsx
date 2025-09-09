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
import { action } from "storybook/actions";
import { Meta, StoryFn } from "@storybook/react";
import * as React from "react";
import SearchBar, {
  SearchBarProps,
} from "../../tsrc/search/components/SearchBar";

export default {
  title: "Search/SearchBar",
  component: SearchBar,
  argTypes: {
    onQueryChange: {
      action: "onQueryChange called",
    },
    doSearch: { action: "doSearch called" },
  },
} as Meta<SearchBarProps>;

export const NoWildcardToggle: StoryFn<SearchBarProps> = (args) => (
  <SearchBar {...args} />
);

export const NonWildcardMode: StoryFn<SearchBarProps> = (args) => (
  <SearchBar {...args} />
);
const wildcardSearch = {
  wildcardMode: false,
  onWildcardModeChange: action("onWildcardModeChange called"),
};
NonWildcardMode.args = {
  query: "",
  wildcardSearch: { ...wildcardSearch },
};

export const WildcardMode: StoryFn<SearchBarProps> = (args) => (
  <SearchBar {...args} />
);
WildcardMode.args = {
  ...NonWildcardMode.args,
  wildcardSearch: {
    ...wildcardSearch,
    wildcardMode: true,
  },
};

export const AdvancedSearchMode: StoryFn<SearchBarProps> = (args) => (
  <SearchBar {...args} />
);
const advancedSearchFilter = {
  onClick: action("advancedSearchFilter onClick called"),
  accent: false,
};
AdvancedSearchMode.args = {
  ...NonWildcardMode.args,
  advancedSearchFilter: { ...advancedSearchFilter },
};

export const AdvancedSearchModeAccented: StoryFn<SearchBarProps> = (args) => (
  <SearchBar {...args} />
);
AdvancedSearchModeAccented.args = {
  ...AdvancedSearchMode.args,
  advancedSearchFilter: {
    ...advancedSearchFilter,
    accent: true,
  },
};

export const AdvancedSearchModeAccentedAndWildcardMode: StoryFn<
  SearchBarProps
> = (args) => <SearchBar {...args} />;
AdvancedSearchModeAccentedAndWildcardMode.args = {
  ...AdvancedSearchModeAccented.args,
  wildcardSearch: {
    ...wildcardSearch,
    wildcardMode: true,
  },
};
