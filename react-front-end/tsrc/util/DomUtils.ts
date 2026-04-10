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
import { HEADER_OFFSET } from "../mainui/Template";

/**
 * Check if a DOM element is outside the visible viewport.
 * This includes checking if the element is hidden behind the main application header.
 *
 * @param targetElement The DOM element to check.
 * @returns `true` if the element is outside the viewport, otherwise `false`.
 */
export const isOutsideViewport = (targetElement: Element): boolean => {
  const rect = targetElement.getBoundingClientRect();
  // If rect.top is less than HEADER_OFFSET, it's behind the app header.
  const isHiddenAbove = rect.top < HEADER_OFFSET;
  // If rect.bottom is greater than the window height, it's off-screen below.
  const isHiddenBelow = rect.bottom > window.innerHeight;

  return isHiddenAbove || isHiddenBelow;
};
