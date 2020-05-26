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
import { mount } from "enzyme";
import * as NavigationGuardModule from "../../../tsrc/components/NavigationGuard";
import { Router } from "react-router";
import { createMemoryHistory } from "history";

/**
 * Need to mock the react-router context and function 'push'.
 */
jest.mock("react-router-dom", () => ({
  useHistory: () => ({
    push: jest.fn(),
  }),
}));

const mockBlockNavigationWithBrowser = jest.spyOn(
  NavigationGuardModule,
  "blockNavigationWithBrowser"
);

describe("<NavigationGuard />", () => {
  const renderComponent = (when: boolean) => {
    const history = createMemoryHistory();
    return mount(
      <Router history={history}>
        <NavigationGuardModule.NavigationGuard when={when} />
      </Router>
    );
  };

  it("should call blockNavigationWithBrowser once when the component first mounts", () => {
    renderComponent(true);
    expect(mockBlockNavigationWithBrowser).toHaveBeenCalledTimes(1);
  });

  it("should set 'window.onbeforeunload' to null when the component unmounts", () => {
    const component = renderComponent(true);
    component.unmount();
    expect(window.onbeforeunload).toBeNull();
  });
});
