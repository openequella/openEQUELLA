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
import mediaQuery from "css-mediaquery";

/**
 * To emulate a screen size where Jest tests are executed, call this function with the intended
 * screen size to generate another function assignable to 'window.matchMedia'.
 * Only `width` and `pointer` are currently supported.
 *
 * @param width The screen width to be mocked.
 * @param pointerFine If `true` will respond to any `@media(pointer: fine)` queries with `true`.
 *                    This is especially useful for MUI-X pickers which use `@media (pointer: fine)`
 *                    to determine whether to use Mobile Pickers (`false`) or
 *                    Desktop Pickers (`true`).
 */
export const createMatchMedia = (
  width: number,
  pointerFine = false
): ((query: string) => MediaQueryList) => {
  const nop = () => {};
  return (query) => ({
    matches:
      query === "(pointer: fine)" && pointerFine
        ? pointerFine
        : mediaQuery.match(query, {
            width,
          }),
    media: query,
    addListener: nop,
    addEventListener: nop,
    removeListener: nop,
    removeEventListener: nop,
    onchange: nop,
    dispatchEvent: () => true,
  });
};
