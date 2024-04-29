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
import { getByText } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

/**
 * Helper function to mock select a Hierarchy summary node in the tree view.
 * @param container The element which contains the tree view.
 * @param name The name of the Hierarchy topic summary.
 */
export const selectHierarchy = (container: HTMLElement, name: string) =>
  userEvent.click(getByText(container, name));
