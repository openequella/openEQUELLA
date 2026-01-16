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
import "@testing-library/jest-dom";
import { composeStories } from "@storybook/react";
import { render, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as NEA from "fp-ts/NonEmptyArray";
import * as O from "fp-ts/Option";
import { not } from "fp-ts/Predicate";
import * as React from "react";
import { getTaskAndNotificationCountsResp } from "../../../../__mocks__/TaskModule.mock";
import * as stories from "../../../../__stories__/dashboard/portlets/Tasks.stories";
import { languageStrings } from "../../../../tsrc/util/langstrings";

const strings = {
  ...languageStrings.dashboard.portlets.tasks,
};

const { Default, NoResults, MissingParentError, ErrorOnLoad } =
  composeStories(stories);

type RenderContext = ReturnType<typeof render> & {
  user: ReturnType<typeof userEvent.setup>;
};

/** Setup function that waits for portlet to be ready. Focused on supporting Storybook stories. */
const setup = async (
  element: React.ReactElement,
  readyStateCheck?: (context: RenderContext) => Promise<void>,
) => {
  const user = userEvent.setup();
  const renderResult = render(element);
  const context = { user, ...renderResult };

  // Default ready state check - wait for a known task name
  const defaultReadyStateCheck = async (ctx: typeof context) => {
    await ctx.findByText(getTaskAndNotificationCountsResp[0].name!);
  };

  // Use provided ready state check or default
  await (readyStateCheck || defaultReadyStateCheck)(context);

  return context;
};

/** Ready state check function that waits for Alert component to be present */
const waitForAlert = async (context: RenderContext): Promise<void> => {
  await context.findByRole("alert");
};

const getCountForItem = (
  name: string,
  getByText: (text: string) => HTMLElement,
): number | undefined => {
  const nameEl = getByText(name);
  const digitRegex = /^[0-9]+$/;

  const getBadgeValue = (container: HTMLElement) =>
    pipe(
      within(container).queryAllByText((c) => digitRegex.test(c)),
      O.fromPredicate(A.isNonEmpty),
      O.map(NEA.head),
    );

  return pipe(
    O.fromNullable(nameEl.parentElement),
    O.chain(getBadgeValue),
    O.map((badgeMaybe) => parseInt(badgeMaybe.textContent ?? "", 10)),
    O.chain(O.fromPredicate(not(Number.isNaN))),
    O.toUndefined,
  );
};

describe("<PortletTasks />", () => {
  it("renders without crashing and shows tasks and notifications", async () => {
    const { getByText } = await setup(<Default />);

    // Check that all names from mock data are displayed
    const allNamesArePresent = pipe(
      getTaskAndNotificationCountsResp,
      A.every((taskFilter) => !!getByText(taskFilter.name!)),
    );

    expect(allNamesArePresent).toBe(true);
  });

  it("displays task counts correctly", async () => {
    const { getByText } = await setup(<Default />);

    const countsMatch = pipe(
      getTaskAndNotificationCountsResp,
      A.filter((item) => (item.count ?? 0) > 0),
      A.every((item) => getCountForItem(item.name!, getByText) === item.count),
    );

    expect(countsMatch).toBe(true);
  });

  it("does not show badges for items with zero count", async () => {
    const { getByText } = await setup(<Default />);

    const countsMatch = pipe(
      getTaskAndNotificationCountsResp,
      A.filter((item) => item.count === 0),
      // They should be undefined, as they should not exist
      A.every((item) => getCountForItem(item.name!, getByText) === undefined),
    );

    expect(countsMatch).toBe(true);
  });

  it("shows error message when no results are returned", async () => {
    const { getByText } = await setup(<NoResults />, waitForAlert);

    expect(
      getByText(`${strings.failedToInitialise} [${strings.nothingReturned}]`),
    ).toBeInTheDocument();
  });

  it("shows error message when parent item is missing", async () => {
    const { getByText } = await setup(<MissingParentError />, waitForAlert);

    expect(
      getByText(strings.unableToFindItemsOfType, { exact: false }),
    ).toBeInTheDocument();
  });

  it("shows error message when provider fails", async () => {
    const { getByText } = await setup(<ErrorOnLoad />, waitForAlert);

    expect(
      getByText(`${strings.failedToInitialise} [Error:`, { exact: false }),
    ).toBeInTheDocument();
  });
});
