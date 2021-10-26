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
import { Eq } from "fp-ts/Eq";
import { pipe } from "fp-ts/function";
import * as M from "fp-ts/Map";
import * as O from "fp-ts/Option";
import * as S from "fp-ts/string";
import { controls } from "../../../../__mocks__/WizardHelper.mock";
import {
  ControlTarget,
  FieldValue,
  render,
} from "../../../../tsrc/components/wizard/WizardHelper";
import { simpleMatch } from "../../../../tsrc/util/match";

/**
 * Produces a `Map` summarising the number of occurrences in an array identified by keys generated
 * with `by`.
 */
const countBy =
  <T, K>(eq: Eq<K>, by: (a: T) => K) =>
  (as: T[]): Map<K, number> =>
    pipe(
      as,
      A.map(by),
      A.reduce<K, Map<K, number>>(new Map(), (m, k: K) => {
        const count: number = pipe(
          m,
          M.lookup(eq)(k),
          O.map((n) => n + 1),
          O.getOrElse<number>(() => 1)
        );

        return pipe(m, M.upsertAt(eq)(k, count));
      })
    );

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
      countBy<OEQ.WizardControl.WizardControl, string>(
        S.Eq,
        ({ controlType }) =>
          pipe(
            controlType,
            simpleMatch<string>({
              editbox: () => "WizardEditBox",
              radiogroup: () => "WizardRadioButtonGroup",
              shufflebox: () => "WizardShuffleBox",
              _: () => "WizardUnsupported",
            })
          )
      )
    );
    const componentCount = pipe(
      elements,
      countBy<JSX.Element, string>(S.Eq, (e) => e.type.name)
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
