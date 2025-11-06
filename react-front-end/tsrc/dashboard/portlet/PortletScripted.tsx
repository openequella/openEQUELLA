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
import { LegacyPortlet } from "./LegacyPortlet";
import type { PortletBasicProps } from "./PortletHelper";

/**
 * Portlet component that delegates its customised UI rendering to the  `LegacyPortlet` component.
 *
 * It handles:
 * - Displaying portlet content generated from HTML and legacy Freemarker markup;
 * - Executing Client-side scripts;
 * - Supporting server-side script execution that occurs during portlet content retrieval.
 */
export const PortletScripted = (props: PortletBasicProps) => (
  <LegacyPortlet {...props} />
);
