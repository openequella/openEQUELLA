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
  mockControlOptionLabels,
  mockWizardControlFactory,
} from "../../../__mocks__/AdvancedSearchModule.mock";

export const editBoxTitle = "Test Edit Box";

export const editBoxEssentials: BasicControlEssentials = {
  title: editBoxTitle,
  mandatory: false,
  schemaNodes: [{ target: "/item/name", attribute: "" }],
  controlType: "editbox",
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
    },
    mockControlOptionLabels,
  ],
]);

export const controls: OEQ.WizardControl.WizardControl[] = Array.from(
  controlValues.keys()
).map(mockWizardControlFactory);

export const controlLabelsAndValues = Array.from(controlValues).map(
  ([{ title, controlType }, values]) => ({
    // for option type component like CheckBox Group, we need the labels of
    // control's options, not the label of the control itself.
    labels:
      controlType === "checkboxgroup"
        ? mockControlOptionLabels
        : [title ?? "!!BLANK LABEL!!"],
    values,
    controlType: controlType,
  })
);

/**
 * Trigger a HTML DOM event for a Wizard control.
 *
 * @param container Root container where <AdvancedSearchPanel/> exists
 * @param labels Depending on the control type, the label can be the control's own label or its options' labels.
 * @param values Depending on the control type, the value can be the control's value or its options' values.
 * @param controlType Type of the control.
 */
export const updateControlValue = (
  container: HTMLElement,
  labels: string[],
  values: string[],
  controlType: OEQ.WizardControl.ControlType
) => {
  const getLabel = (label: string): HTMLElement =>
    getByLabelText(container, label);
  switch (controlType) {
    case "editbox":
      userEvent.type(getLabel(labels[0]), values[0]);
      break;
    case "checkboxgroup":
      labels.forEach((l) => userEvent.click(getLabel(l)));
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
 * @param labels Depending on the control type, the label can be the control's own label or its options' labels.
 * @param values Depending on the control type, the value can be the control's value or its options' values.
 * @param controlType Type of the control.
 */
export const validateControlValue = (
  container: HTMLElement,
  labels: string[],
  values: string[],
  controlType: OEQ.WizardControl.ControlType
) => {
  const getLabel = (label: string): HTMLElement =>
    getByLabelText(container, label);
  switch (controlType) {
    case "editbox":
      expect(getLabel(labels[0])).toHaveValue(values[0]);
      break;
    case "checkboxgroup":
      labels.forEach((l) => expect(getLabel(l)).toBeChecked());
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
