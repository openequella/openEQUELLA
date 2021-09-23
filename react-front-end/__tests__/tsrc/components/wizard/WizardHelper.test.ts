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
import * as M from "fp-ts/Map";
import { controls } from "../../../../__mocks__/WizardHelper.mock";
import {
  ControlTarget,
  FieldValue,
  render,
} from "../../../../tsrc/components/wizard/WizardHelper";

describe("render()", () => {
  const logOnChange = (update: FieldValue): void =>
    console.debug("onChange called", update);

  const nameEditboxTarget: ControlTarget = {
    type: "editbox",
    schemaNode: ["/item/name"],
  };

  it("creates JSX.Elements matching definition", () => {
    // We currently only support editboxes, so just filter to them for now
    // Each time we add a control, we need to expand this out
    const supportedControls = controls.filter(
      (c) => c.controlType === "editbox"
    );
    expect(supportedControls.length).toBeGreaterThan(1); // quick assertions we have good test data

    const elements: JSX.Element[] = render(
      supportedControls,
      new Map(),
      logOnChange
    );
    expect(elements).toHaveLength(supportedControls.length);
    // for now, we just expect Editboxes, we'll need to elaborate on this in the future
    expect(elements.every((e) => e.type.name === "WizardEditBox")).toBeTruthy();
  });

  it("creates WizardUnsupported components for unknown/unsupported ones", () => {
    const elements: JSX.Element[] = render(controls, new Map(), logOnChange);
    // Current the 'controls' include a radio group which we've not yet written support for
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
