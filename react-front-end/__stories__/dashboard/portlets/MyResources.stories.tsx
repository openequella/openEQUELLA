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
import { privateMyResourcesPortlet } from "../../../__mocks__/Dashboard.mock";
import { getMyResourceCategoriesTransformedResp } from "../../../__mocks__/myResourcesCategories_mock_data";
import { getCurrentUserMock } from "../../../__mocks__/UserModule.mock";
import {
  PortletMyResources,
  PortletMyResourcesProps,
} from "../../../tsrc/dashboard/portlet/PortletMyResources";
import { AppContext } from "../../../tsrc/mainui/App";
import { MyResourcesCategory } from "../../../tsrc/modules/MyResourceModule";

interface StoryProps extends PortletMyResourcesProps {
  currentUser?: typeof getCurrentUserMock;
}

export default {
  title: "Dashboard/portlets/PortletMyResources",
  component: PortletMyResources,
} as Meta<PortletMyResourcesProps>;

const Template: StoryFn<StoryProps> = ({
  // eslint-disable-next-line react/prop-types
  currentUser = getCurrentUserMock,
  ...args
}) => (
  <AppContext.Provider
    value={{
      appErrorHandler: () => {},
      currentUser,
      refreshUser: () => Promise.resolve(undefined),
    }}
  >
    <PortletMyResources {...args} />
  </AppContext.Provider>
);

const mockMyResourcesTypeProvider = async (
  isScrapbookEnabled: boolean,
): Promise<MyResourcesCategory[]> =>
  isScrapbookEnabled
    ? getMyResourceCategoriesTransformedResp
    : getMyResourceCategoriesTransformedResp.filter(
        (r) => r.id !== "scrapbook",
      );

const failingMyResourcesTypeProvider = async (): Promise<
  MyResourcesCategory[]
> => {
  throw new Error("Failed to fetch my resources types");
};

const slowMyResourcesTypeProvider = async (): Promise<
  MyResourcesCategory[]
> => {
  await new Promise((resolve) => setTimeout(resolve, 3000));
  return getMyResourceCategoriesTransformedResp;
};

export const Simple = Template.bind({});
Simple.args = {
  cfg: privateMyResourcesPortlet,
  position: { order: 0, column: 0 },
  myResourcesTypeProvider: mockMyResourcesTypeProvider,
};

export const ScrapbookDisabled = Template.bind({});
ScrapbookDisabled.args = {
  ...Simple.args,
  currentUser: { ...getCurrentUserMock, scrapbookEnabled: false },
};

export const ErrorOnLoad = Template.bind({});
ErrorOnLoad.args = {
  ...Simple.args,
  myResourcesTypeProvider: failingMyResourcesTypeProvider,
};

export const SlowLoading = Template.bind({});
SlowLoading.args = {
  ...Simple.args,
  myResourcesTypeProvider: slowMyResourcesTypeProvider,
};
