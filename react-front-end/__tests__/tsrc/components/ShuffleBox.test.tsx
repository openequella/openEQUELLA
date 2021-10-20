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
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as React from "react";
import { useState } from "react";
import { ShuffleBox } from "../../../tsrc/components/ShuffleBox";
import { languageStrings } from "../../../tsrc/util/langstrings";

const shuffleBoxStrings = languageStrings.shuffleBox;

const testContainerShuffleBoxId = "shufflebox-test-container";
const TestContainer = ({
  options,
  initValues = new Set<string>([]),
}: {
  options: Map<string, string>;
  initValues?: Set<string>;
}): JSX.Element => {
  const [values, setValues] = useState<Set<string>>(initValues);

  return (
    <ShuffleBox
      id={testContainerShuffleBoxId}
      options={options}
      values={values}
      onSelect={setValues}
    />
  );
};

const options = {
  one: "first",
  two: "second",
  three: "third",
};
const optionsMap = new Map(A.zip(Object.keys(options), Object.values(options)));
type optionKeyType = keyof typeof options;
const optionKey = (o: optionKeyType) => o;
const optionValue = (o: optionKeyType): string => options[o];

const selectionsListId = (idPrefix: string) => `${idPrefix}-selections`;
const optionsListId = (idPrefix: string) => `${idPrefix}-options`;

const getParentElementId =
  (getByText: (text: string) => Element) =>
  (option: string): string =>
    getByText(option).parentElement!.id;

const shouldBeIn =
  (getId: (byText: string) => string) =>
  (idPrefix: string) =>
  (option: string): boolean =>
    getId(option).startsWith(`${idPrefix}`);

describe("<ShuffleBox/>", () => {
  it("places the options in their respective boxes", () => {
    const id = "correctbox";
    const selectedOption = optionKey("two");
    const { getByText } = render(
      <ShuffleBox
        id={id}
        options={optionsMap}
        values={new Set<string>([selectedOption])}
        onSelect={jest.fn()}
      />
    );
    const isIn = shouldBeIn(getParentElementId(getByText));

    expect(
      pipe(optionValue(selectedOption), isIn(selectionsListId(id)))
    ).toBeTruthy();
    expect(pipe(options.one, isIn(optionsListId(id)))).toBeTruthy();
  });

  it("moves items to the selections list when Add Selected button clicked", () => {
    const { getByText, getByLabelText } = render(
      <TestContainer options={optionsMap} />
    );
    const isIn = shouldBeIn(getParentElementId(getByText));

    userEvent.click(getByText(options.one));
    userEvent.click(getByLabelText(shuffleBoxStrings.addSelected));

    expect(
      pipe(options.one, isIn(selectionsListId(testContainerShuffleBoxId)))
    ).toBeTruthy();
  });

  it("removes items to the selections list when Remove Selected button clicked", () => {
    const selectedOption = optionKey("one");
    const { getByText, getByLabelText } = render(
      <TestContainer
        options={optionsMap}
        initValues={new Set<string>([selectedOption])}
      />
    );
    const isIn = shouldBeIn(getParentElementId(getByText));

    userEvent.click(getByText(optionValue(selectedOption)));
    userEvent.click(getByLabelText(shuffleBoxStrings.removeSelected));

    expect(
      pipe(
        optionValue(selectedOption),
        isIn(optionsListId(testContainerShuffleBoxId))
      )
    ).toBeTruthy();
  });

  it("moves ALL items to the expected list when one of the All buttons are clicked", () => {
    const { getByText, getByLabelText } = render(
      <TestContainer options={optionsMap} />
    );
    const isIn = shouldBeIn(getParentElementId(getByText));
    const isInSelectionsList = isIn(
      selectionsListId(testContainerShuffleBoxId)
    );
    const isInOptionsList = isIn(optionsListId(testContainerShuffleBoxId));
    const optionList: string[] = Object.values(options);
    const howMany = (isInList: (option: string) => boolean): number =>
      pipe(
        optionList,
        A.map(isInList),
        A.filter((inList) => inList),
        A.size
      );

    // -- Begin Test
    // First, confirm starting state
    expect(howMany(isInSelectionsList)).toEqual(0);
    expect(howMany(isInOptionsList)).toEqual(optionList.length);

    // Add/select all
    userEvent.click(getByLabelText(shuffleBoxStrings.addAll));

    expect(howMany(isInSelectionsList)).toEqual(optionList.length);
    expect(howMany(isInOptionsList)).toEqual(0);

    // Remove all
    userEvent.click(getByLabelText(shuffleBoxStrings.removeAll));

    expect(howMany(isInSelectionsList)).toEqual(0);
    expect(howMany(isInOptionsList)).toEqual(optionList.length);
  });
});
