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
import * as OEQ from "@openequella/rest-api-client";
import { privateFavouritePortlet } from "../../../__mocks__/Dashboard.mock";
import {
  getFavouriteResourcesResp,
  getFavouriteSearchesResp,
} from "../../../__mocks__/Favourites.mock";
import { getCurrentUserMock } from "../../../__mocks__/UserModule.mock";
import {
  PortletFavourites,
  PortletFavouritesProps,
} from "../../../tsrc/dashboard/portlet/PortletFavourites";
import { AppContext } from "../../../tsrc/mainui/App";

export default {
  title: "Dashboard/portlets/PortletFavourites",
  component: PortletFavourites,
} as Meta<PortletFavouritesProps>;

const Template: StoryFn<PortletFavouritesProps> = (args) => (
  <AppContext.Provider
    value={{
      appErrorHandler: () => {},
      currentUser: getCurrentUserMock,
      refreshUser: () => Promise.resolve(undefined),
    }}
  >
    <PortletFavourites {...args} />
  </AppContext.Provider>
);

const emptyResolver: <T>() => Promise<OEQ.Search.SearchResult<T>> = () =>
  Promise.resolve({
    results: [],
    start: 0,
    available: 0,
    length: 0,
    highlight: [],
  });

export const Simple = Template.bind({});
Simple.args = {
  cfg: privateFavouritePortlet,
  favouriteResourcesProvider: (_) => Promise.resolve(getFavouriteResourcesResp),
  favouriteSearchesProvider: () => Promise.resolve(getFavouriteSearchesResp),
};

export const NoResources = Template.bind({});
NoResources.args = {
  ...Simple.args,
  favouriteResourcesProvider: emptyResolver,
};

export const NoSearches = Template.bind({});
NoSearches.args = {
  ...Simple.args,
  favouriteSearchesProvider: emptyResolver,
};

export const NoResourcesAndNoSearches = Template.bind({});
NoResourcesAndNoSearches.args = {
  ...Simple.args,
  favouriteResourcesProvider: emptyResolver,
  favouriteSearchesProvider: emptyResolver,
};

export const Loading = Template.bind({});
Loading.args = {
  ...Simple.args,
  favouriteResourcesProvider: (_) =>
    new Promise((_) => {
      // Never resolves.
    }),
  favouriteSearchesProvider: () =>
    new Promise((_) => {
      // Never resolves.
    }),
};

export const SlowLoading = Template.bind({});
SlowLoading.args = {
  ...Simple.args,
  favouriteResourcesProvider: (_) =>
    new Promise((resolve) => {
      setTimeout(() => resolve(getFavouriteResourcesResp), 3000);
    }),
  favouriteSearchesProvider: () =>
    new Promise((resolve) => {
      setTimeout(() => resolve(getFavouriteSearchesResp), 3000);
    }),
};
