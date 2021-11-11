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
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as M from "fp-ts/Map";
import * as NEA from "fp-ts/NonEmptyArray";
import * as R from "fp-ts/Record";
import {
  controls,
  mockedFieldValueMap,
} from "../../../../__mocks__/WizardHelper.mock";
import {
  ControlTarget,
  FieldValue,
  generateRawLuceneQuery,
  render,
} from "../../../../tsrc/components/wizard/WizardHelper";
import { simpleMatch } from "../../../../tsrc/util/match";
import * as TokenisationModule from "../../../../tsrc/modules/TokenisationModule";

/**
 * Produces a summary of the number of occurrences in an array identified by keys generated
 * with `by`.
 */
const countBy =
  <T>(by: (a: T) => string) =>
  (as: T[]): Record<string, number> =>
    pipe(as, NEA.groupBy(by), R.map(A.size));

describe("render()", () => {
  const logOnChange = (update: FieldValue): void =>
    console.debug("onChange called", update);

  const nameEditboxTarget: ControlTarget = {
    type: "editbox",
    schemaNode: ["/item/name"],
  };

  it("creates JSX.Elements matching definition", () => {
    const elements: JSX.Element[] = render(controls, new Map(), logOnChange);
    expect(elements).toHaveLength(controls.length);

    const expectedComponentCount = pipe(
      controls,
      countBy<OEQ.WizardControl.WizardControl>(({ controlType }) =>
        pipe(
          controlType,
          simpleMatch<string>({
            editbox: () => "WizardEditBox",
            radiogroup: () => "WizardRadioButtonGroup",
            shufflebox: () => "WizardShuffleBox",
            shufflelist: () => "WizardShuffleList",
            checkboxgroup: () => "WizardCheckBoxGroup",
            listbox: () => "WizardListBox",
            html: () => "WizardRawHtml",
            _: () => "WizardUnsupported",
          })
        )
      )
    );
    const componentCount = pipe(
      elements,
      countBy<JSX.Element>((e) => e.type.name)
    );

    expect(componentCount).toStrictEqual(expectedComponentCount);
  });

  it("creates WizardUnsupported components for unknown/unsupported ones", () => {
    const elements: JSX.Element[] = render(controls, new Map(), logOnChange);
    expect(
      elements.filter((e) => e.type.name === "WizardUnsupported")
    ).toHaveLength(1);
  });

  it("handles `controlType === 'unknown'` - i.e. `UnknownWizardControl`", () => {
    const elements: JSX.Element[] = render(
      [{ controlType: "unknown" }],
      new Map(),
      logOnChange
    );
    expect(
      elements.filter((e) => e.type.name === "WizardUnsupported")
    ).toHaveLength(1);
  });

  it.each<[string, FieldValue, number]>([
    [
      "singular",
      {
        target: nameEditboxTarget,
        value: ["an editbox - name"],
      },
      1,
    ],
    [
      "multiple",
      {
        target: { type: "editbox", schemaNode: ["/item/description"] },
        value: ["an editbox - description"],
      },
      2,
    ],
  ])(
    "correctly sets the initial value when provided (%s)",
    (_, value, fieldsSet) => {
      // smoke test the test data
      expect(value.value[0]).toBeTruthy();

      const elements = render(
        controls,
        M.singleton(value.target, value.value),
        logOnChange
      );
      // Test the field(s) were set
      expect(
        elements.filter((e) => e.props.value === value.value[0])
      ).toHaveLength(fieldsSet);
    }
  );

  it("throws an error if the incorrect control value type is provided", () => {
    expect(() =>
      render(controls, M.singleton(nameEditboxTarget, [1]), logOnChange)
    ).toThrow(TypeError);
  });
});

describe("generateRawLuceneQuery()", () => {
  jest
    .spyOn(TokenisationModule, "getTokensForText")
    .mockResolvedValue({ tokens: ["hello", "world"] });

  it("builds raw Lucene query for Wizard controls", async () => {
    const query = await generateRawLuceneQuery(mockedFieldValueMap);

    const expectedQuery =
      "(/controls/calendar1:[2021-11-01 TO *]) AND " +
      "(/controls/calendar2:[* TO 2021-11-01]) AND " +
      "(/controls/calendar3:[2021-11-01 TO 2021-11-11]) AND " +
      "(/controls/checkbox:(optionA, optionB)) AND " +
      "(/controls/editbox\\*:(hello world)) AND " +
      "(/controls/listbox:optionC) AND " +
      "(/controls/radiogroup:optionD) AND " +
      "(/controls/shufflebox:(optionE optionF)) AND " +
      "(/controls/shufflelist\\*:(hello world)) AND " +
      "(/controls/termselector:(programming)) AND " +
      "(/controls/userselector:(admin))";

    expect(query).toBe(expectedQuery);
  });
});
