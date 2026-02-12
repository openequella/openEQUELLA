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

import { render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

/**
 * Type that combines the render result from React Testing Library with the userEvent setup.
 */
export type RenderContext = ReturnType<typeof render> & {
  user: ReturnType<typeof userEvent.setup>;
};

/**
 * Setup function that waits for provided component to be ready. Focused on supporting Storybook stories.
 *
 * @param element - The React element to render
 * @param readyStateCheck - A function that checks if the component is ready.
 *                          It receives the render context and should resolve when the component is ready for testing.
 */
export const setupStoryComponent = async (
  element: React.ReactElement,
  readyStateCheck: (context: RenderContext) => Promise<void>,
): Promise<RenderContext> => {
  const user = userEvent.setup();
  const renderResult = render(element);
  const context = { user, ...renderResult };

  await readyStateCheck(context);
  return context;
};
