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
import { getByLabelText } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { absurd } from "fp-ts/function";
import {
  BasicControlEssentials,
  getAdvancedSearchDefinition,
  mockWizardControlFactory,
} from "../../../__mocks__/AdvancedSearchModule.mock";

export const editBoxTitle = "Test Edit Box";

export const editBoxEssentials: BasicControlEssentials = {
  title: editBoxTitle,
  mandatory: false,
  schemaNodes: [{ target: "/item/name", attribute: "" }],
  controlType: "editbox",
  options: [{ text: "default value", value: "test" }],
};

export const oneEditBoxWizard = (
  mandatory: boolean
): OEQ.AdvancedSearch.AdvancedSearchDefinition => ({
  ...getAdvancedSearchDefinition,
  controls: [{ ...editBoxEssentials, mandatory }].map(mockWizardControlFactory),
});

// As we go forward, let's build on the below collection of controls,
// and add each control type as we build them.
const controlValues: Map<BasicControlEssentials, string[]> = new Map([
  [
    {
      ...editBoxEssentials,
      title: "Edit Box - name",
      schemaNodes: [{ target: "/item/name", attribute: "" }],
    },
    ["a name"],
  ],
  [
    {
      ...editBoxEssentials,
      title: "Edit Box - year",
      schemaNodes: [{ target: "/item/", attribute: "@year" }],
    },
    ["2021"],
  ],
  [
    {
      title: "CheckBox Group",
      schemaNodes: [{ target: "/item/options", attribute: "" }],
      mandatory: false,
      controlType: "checkboxgroup",
      options: [
        {
          text: "CheckBox one",
          value: "1",
        },
        {
          text: "CheckBox two",
          value: "2",
        },
      ],
    },
    [], //CheckBox Group does not worry about the values
  ],
]);

export const controls: OEQ.WizardControl.WizardControl[] = Array.from(
  controlValues.keys()
).map(mockWizardControlFactory);

export const controlLabelsAndValues = Array.from(controlValues).map(
  ([{ title, controlType, options }, values]) => ({
    label: title ?? `${controlType}-${values.toString()}`,
    values,
    controlType: controlType,
    optionLabels: options.map(({ text, value }) => text ?? value),
  })
);

/**
 * Trigger a HTML DOM event for a Wizard control.
 *
 * @param container Root container where <AdvancedSearchPanel/> exists
 * @param label Label of the control.
 * @param values Depending on the control type, the value can be the control's value or its options' values.
 * @param optionLabels Labels of the controls' options.
 * @param controlType Type of the control.
 */
export const updateControlValue = (
  container: HTMLElement,
  label: string,
  values: string[],
  optionLabels: string[],
  controlType: OEQ.WizardControl.ControlType
) => {
  switch (controlType) {
    case "editbox":
      userEvent.type(getByLabelText(container, label), values[0]);
      break;
    case "checkboxgroup":
      optionLabels.forEach((optionLabel) =>
        userEvent.click(getByLabelText(container, optionLabel))
      );
      break;
    case "calendar":
    case "html":
    case "listbox":
    case "radiogroup":
    case "shufflebox":
    case "shufflelist":
    case "termselector":
    case "userselector":
      break;
    default:
      return absurd(controlType);
  }
};

/**
 * Validate if a Wizard control or its options have correct values.
 *
 * @param container Root container where <AdvancedSearchPanel/> exists
 * @param label Label of the control.
 * @param values Depending on the control type, the value can be the control's value or its options' values.
 * @param optionLabels Labels of the controls' options.
 * @param controlType Type of the control.
 */
export const validateControlValue = (
  container: HTMLElement,
  label: string,
  values: string[],
  optionLabels: string[],
  controlType: OEQ.WizardControl.ControlType
) => {
  switch (controlType) {
    case "editbox":
      expect(getByLabelText(container, label)).toHaveValue(values[0]);
      break;
    case "checkboxgroup":
      optionLabels.forEach((optionLabel) =>
        expect(getByLabelText(container, optionLabel)).toBeChecked()
      );
      break;
    case "calendar":
    case "html":
    case "listbox":
    case "radiogroup":
    case "shufflebox":
    case "shufflelist":
    case "termselector":
    case "userselector":
      break;
    default:
      return absurd(controlType);
  }
};
