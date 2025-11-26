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
import { render, RenderResult } from "@testing-library/react";
import { createMemoryHistory } from "history";
import * as React from "react";
import { Router } from "react-router-dom";
import { basicPortlet } from "../../../__mocks__/Dashboard.mock";
import PortletItem, {
  PortletItemProps,
} from "../../../tsrc/dashboard/components/PortletItem";
import "@testing-library/jest-dom";

export const portletContent = "Portlet content";

export const defaultProps: PortletItemProps = {
  portlet: basicPortlet,
  children: <div>{portletContent}</div>,
  highlight: false,
};

const history = createMemoryHistory();

/**
 * Helper to render PortletItem.
 *
 * @param props Props to pass to the component.
 */
export const renderPortletItem = (props = defaultProps): RenderResult =>
  render(
    <Router history={history}>
      <PortletItem {...props} />
    </Router>,
  );
