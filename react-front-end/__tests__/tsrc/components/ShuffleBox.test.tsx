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
import * as React from "react";
import { ShuffleBox } from "../../../tsrc/components/ShuffleBox";
import { languageStrings } from "../../../tsrc/util/langstrings";

const shuffleBoxStrings = languageStrings.shuffleBox;

const options = {
  one: "first",
  two: "second",
  three: "third",
};
const optionsAllKeys = Object.keys(options);
const optionsMap = new Map(A.zip(optionsAllKeys, Object.values(options)));
type optionKeyType = keyof typeof options;
const optionKey = (o: optionKeyType) => o;
const optionValue = (o: optionKeyType): string => options[o];

const selectionsListId = (idPrefix: string) => `${idPrefix}-selections`;
const optionsListId = (idPrefix: string) => `${idPrefix}-options`;

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

    // expect helper
    const isInList = (listId: string, option: string): boolean =>
      getByText(option).parentElement!.id.startsWith(listId);

    expect(
      isInList(selectionsListId(id), optionValue(selectedOption))
    ).toBeTruthy();
    expect(isInList(optionsListId(id), options.one)).toBeTruthy();
  });

  it("calls onSelect with the existing selections as well as the new ones when the Add Selected button is clicked", async () => {
    const onSelect = jest.fn();
    const existingSelection = optionKey("one");
    const { getByLabelText, getByText } = render(
      <ShuffleBox
        options={optionsMap}
        values={new Set<string>([existingSelection])}
        onSelect={onSelect}
      />
    );

    await userEvent.click(getByText(options.two));
    await userEvent.click(getByLabelText(shuffleBoxStrings.addSelected));

    expect(onSelect).toHaveBeenCalledWith(
      new Set<string>([existingSelection, optionKey("two")])
    );
  });

  it("calls onSelect with the selected selections removed from existing selections when the Removed Selected button is clicked", async () => {
    const selectedOption = optionKey("one");
    const remainingOption = optionKey("three");
    const onSelect = jest.fn();
    const { getByLabelText, getByText } = render(
      <ShuffleBox
        options={optionsMap}
        values={new Set<string>([selectedOption, remainingOption])}
        onSelect={onSelect}
      />
    );

    await userEvent.click(getByText(optionValue(selectedOption)));
    await userEvent.click(getByLabelText(shuffleBoxStrings.removeSelected));

    expect(onSelect).toHaveBeenCalledWith(new Set<string>([remainingOption]));
  });

  it("calls onSelect with all possible options when the Select All button is clicked", async () => {
    const onSelect = jest.fn();
    const { getByLabelText } = render(
      <ShuffleBox
        options={optionsMap}
        values={new Set<string>()}
        onSelect={onSelect}
      />
    );

    await userEvent.click(getByLabelText(shuffleBoxStrings.addAll));
    expect(onSelect).toHaveBeenCalledWith(new Set<string>(optionsAllKeys));
  });

  it("calls onSelect with an empty collection when Remove All button is clicked", async () => {
    const onSelect = jest.fn();
    const { getByLabelText } = render(
      <ShuffleBox
        options={optionsMap}
        values={new Set<string>()}
        onSelect={onSelect}
      />
    );

    await userEvent.click(getByLabelText(shuffleBoxStrings.removeAll));
    expect(onSelect).toHaveBeenCalledWith(new Set<string>());
  });
});
