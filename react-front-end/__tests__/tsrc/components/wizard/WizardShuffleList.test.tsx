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
import "@testing-library/jest-dom/extend-expect";
import { render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { pipe } from "fp-ts/function";
import * as RSET from "fp-ts/ReadonlySet";
import * as S from "fp-ts/string";
import * as React from "react";
import { WizardShuffleList } from "../../../../tsrc/components/wizard/WizardShuffleList";
import { languageStrings } from "../../../../tsrc/util/langstrings";

const {
  common: {
    action: { add: addString, delete: deleteString },
  },
  shuffleList: shuffleListStrings,
} = languageStrings;

describe("<WizardShuffleList/>", () => {
  it("displays the values provided", () => {
    const values: readonly string[] = ["mouse", "cat", "dog"];
    const { queryByText } = render(
      <WizardShuffleList
        values={new Set(values)}
        onChange={jest.fn()}
        mandatory={false}
      />
    );

    expect(values.every((v) => queryByText(v))).toBeTruthy();
  });

  it.each([
    ["pressing enter", true],
    ["using add button", false],
  ])(
    "supports the adding of a value by %s",
    async (_: string, pressEnterKey: boolean) => {
      const onChange = jest.fn();
      const { getByLabelText } = render(
        <WizardShuffleList
          values={new Set()}
          onChange={onChange}
          mandatory={false}
        />
      );

      const doAdd = async (value: string) => {
        await userEvent.type(
          getByLabelText(shuffleListStrings.newEntry),
          `${value}${pressEnterKey ? "{enter}" : ""}`
        );
        if (!pressEnterKey) {
          await userEvent.click(getByLabelText(addString));
        }
      };

      const me = "Add me";
      await doAdd(me);
      expect(onChange).toHaveBeenLastCalledWith(new Set([me]));
    }
  );

  it("supports removing a value", async () => {
    const onChange = jest.fn();
    const deleteMe = "Please delete me";
    const testValues: ReadonlySet<string> = new Set<string>([
      "Keep me",
      deleteMe,
      "Save me for later",
    ]);
    const { getByLabelText } = render(
      <WizardShuffleList
        values={testValues}
        onChange={onChange}
        mandatory={false}
      />
    );

    await userEvent.click(getByLabelText(`${deleteString} ${deleteMe}`));

    expect(onChange).toHaveBeenLastCalledWith(
      pipe(testValues, RSET.remove(S.Eq)(deleteMe))
    );
  });
});
