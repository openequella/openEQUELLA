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
import { mockWizardControlFactory } from "../../../../__mocks__/AdvancedSearchModule.mock";
import { controls } from "../../../../__mocks__/WizardHelper.mock";
import {
  buildVisibilityScriptContext,
  ControlTarget,
  FieldValue,
  FieldValueMap,
  render,
} from "../../../../tsrc/components/wizard/WizardHelper";
import { guestUser } from "../../../../tsrc/modules/UserModule";
import { simpleMatch } from "../../../../tsrc/util/match";
import * as React from "react";

/**
 * Produces a summary of the number of occurrences in an array identified by keys generated
 * with `by`.
 */
const countBy =
  <T>(by: (a: T) => string) =>
  (as: T[]): Record<string, number> =>
    pipe(as, NEA.groupBy(by), R.map(A.size));

describe("render()", () => {
  const logOnChange = (updates: FieldValue[]): void =>
    console.debug("onChange called", updates);

  const noFieldValues = new Map();

  const nameEditboxTarget: ControlTarget = {
    type: "editbox",
    schemaNode: ["/item/name"],
  };

  it("creates JSX.Elements matching definition", () => {
    const elements: React.JSX.Element[] = render(
      controls,
      noFieldValues,
      logOnChange,
      buildVisibilityScriptContext(noFieldValues, guestUser),
    );
    expect(elements).toHaveLength(controls.length);

    const expectedComponentCount = pipe(
      controls,
      countBy<OEQ.WizardControl.WizardControl>(({ controlType }) =>
        pipe(
          controlType,
          simpleMatch<string>({
            checkboxgroup: () => "WizardCheckBoxGroup",
            editbox: () => "WizardEditBox",
            html: () => "WizardRawHtml",
            listbox: () => "WizardListBox",
            radiogroup: () => "WizardRadioButtonGroup",
            shufflebox: () => "WizardShuffleBox",
            shufflelist: () => "WizardShuffleList",
            userselector: () => "WizardUserSelector",
            termselector: () => "WizardSimpleTermSelector",
            _: () => "WizardUnsupported",
          }),
        ),
      ),
    );
    const componentCount = pipe(
      elements,
      countBy<React.JSX.Element>((e) => e.type.name),
    );

    expect(componentCount).toStrictEqual(expectedComponentCount);
  });

  it("creates WizardUnsupported components for unknown/unsupported ones", () => {
    const elements: React.JSX.Element[] = render(
      controls,
      noFieldValues,
      logOnChange,
      buildVisibilityScriptContext(noFieldValues, guestUser),
    );
    expect(
      elements.filter((e) => e.type.name === "WizardUnsupported"),
    ).toHaveLength(1);
  });

  it("handles `controlType === 'unknown'` - i.e. `UnknownWizardControl`", () => {
    const elements: React.JSX.Element[] = render(
      [{ controlType: "unknown" }],
      noFieldValues,
      logOnChange,
      buildVisibilityScriptContext(noFieldValues, guestUser),
    );
    expect(
      elements.filter((e) => e.type.name === "WizardUnsupported"),
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

      const fieldValues = M.singleton(value.target, value.value);
      const elements = render(
        controls,
        fieldValues,
        logOnChange,
        buildVisibilityScriptContext(fieldValues, guestUser),
      );
      // Test the field(s) were set
      expect(
        elements.filter((e) => e.props.value === value.value[0]),
      ).toHaveLength(fieldsSet);
    },
  );

  it("throws an error if the incorrect control value type is provided", () => {
    const fieldValues = M.singleton(nameEditboxTarget, [1]);
    expect(() =>
      render(
        controls,
        fieldValues,
        logOnChange,
        buildVisibilityScriptContext(fieldValues, guestUser),
      ),
    ).toThrow(TypeError);
  });

  const checkBoxSchemaNode = "/item/checkbox";
  const checkboxValues = (values: string[]): FieldValueMap =>
    M.singleton(
      { schemaNode: [checkBoxSchemaNode], type: "checkboxgroup" },
      values,
    );
  const simpleContainsScript = `return xml.contains('${checkBoxSchemaNode}', 'one');`;
  const simpleGetScript = `return xml.get('${checkBoxSchemaNode}') === 'one';`;
  const aRoleUuid = "f618d610-2191-4a2c-b699-acb833d5b10f";
  it.each<[string, string | undefined, FieldValueMap, number]>([
    ["control case (no script)", undefined, new Map(), 2],
    [
      "simple xml.contains (visible)",
      simpleContainsScript,
      checkboxValues(["one"]),
      2,
    ],
    [
      "simple xml.contains (hidden)",
      simpleContainsScript,
      checkboxValues(["two"]),
      1,
    ],
    ["simple xml.get (visible)", simpleGetScript, checkboxValues(["one"]), 2],
    ["simple xml.get (hidden)", simpleGetScript, checkboxValues(["two"]), 1],
    [
      "xml.get when no values available is an empty string",
      `return xml.get('${checkBoxSchemaNode}') === '';`,
      new Map(),
      2,
    ],
    [
      "user.hasRole (visible)",
      `return user.hasRole('${aRoleUuid}');`,
      new Map(),
      2,
    ],
    [
      "user.hasRole (hidden - due to not having role)",
      `return user.hasRole('rubbish-role');`,
      new Map(),
      1,
    ],
    ["empty string (visible)", ``, new Map(), 2],
  ])(
    "hides controls based on visibility scripts - %s",
    (_, script, values, controlsVisible) => {
      const testControls: OEQ.WizardControl.WizardBasicControl[] = [
        mockWizardControlFactory({
          controlType: "checkboxgroup",
          mandatory: false,
          schemaNodes: [{ target: checkBoxSchemaNode, attribute: "" }],
          options: [
            { text: "opt1", value: "one" },
            { text: "opt2", value: "two" },
            { text: "opt3", value: "three" },
          ],
          defaultValues: [],
        }),
        {
          ...mockWizardControlFactory({
            controlType: "editbox",
            mandatory: false,
            schemaNodes: [{ target: "/item/editbox", attribute: "" }],
            options: [],
            defaultValues: [],
          }),
          visibilityScript: script,
        },
      ];

      const visibleControls = render(
        testControls,
        values,
        jest.fn(),
        buildVisibilityScriptContext(values, {
          ...guestUser,
          roles: [aRoleUuid],
        }),
      );
      expect(visibleControls).toHaveLength(controlsVisible);
    },
  );

  it("calls onChange to clear the value for any hidden control", () => {
    const testNode = "/item/hidden";
    const testTarget: ControlTarget = {
      schemaNode: [testNode],
      type: "editbox",
      isValueTokenised: true,
    };
    const testValues: FieldValueMap = M.singleton(testTarget, ["test value"]);
    const mockOnChange = jest.fn();

    render(
      [
        {
          ...mockWizardControlFactory({
            controlType: "editbox",
            mandatory: false,
            schemaNodes: [{ target: testNode, attribute: "" }],
            options: [],
            defaultValues: [],
          }),
          visibilityScript: "return false;", // i.e. always hidden - no need to get tricky
        },
      ],
      testValues,
      mockOnChange,
      buildVisibilityScriptContext(testValues, guestUser),
    );

    expect(mockOnChange).toHaveBeenCalledTimes(1);
    // Note value being cleared via `[]`
    expect(mockOnChange).toHaveBeenCalledWith([
      { target: testTarget, value: [] },
    ]);
  });
});
