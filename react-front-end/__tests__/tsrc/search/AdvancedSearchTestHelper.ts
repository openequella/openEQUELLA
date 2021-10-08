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
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as M from "fp-ts/Map";
import * as O from "fp-ts/Option";
import { contramap, Ord } from "fp-ts/Ord";
import * as S from "fp-ts/string";
import { concatAll } from "fp-ts/lib/Monoid";
import * as IO from "fp-ts/IO";
import * as Void from "fp-ts/void";

export const editBoxEssentials: BasicControlEssentials = {
  title: "Test Edit Box",
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
        {
          text: "CheckBox three",
          value: "3",
        },
        {
          text: "CheckBox four",
          value: "4",
        },
      ],
    },
    ["true", "true", "false", "false"], // The values determine whether to turn on or off a checkbox.
  ],
]);

// Alias for the Map including a Wizard control's labels and values. However, the value can refer to
// the real input value or the status of attribute `checked`.
type WizardControlLabelValue = Map<string, string>;

/**
 * Provide details of a control and its labels and values for tests.
 */
export type MockedControlValue = [
  OEQ.WizardControl.WizardBasicControl,
  WizardControlLabelValue
];

// Function to merge multiple `WizardControlLabelValue` into one.
const mergeLabelAndValues: (
  _: WizardControlLabelValue[]
) => WizardControlLabelValue = concatAll(M.getUnionMonoid(S.Eq, S.Semigroup));

// Function to build a `WizardControlLabelValue` for the supplied control and its values.
const buildLabelValue = (
  { controlType, title, options }: BasicControlEssentials,
  values: string[]
): WizardControlLabelValue => {
  switch (controlType) {
    case "editbox":
      return new Map([
        [title ?? `${values.toString()}`, values[0]], // EditBox just needs its title and the first value.
      ]);
    case "checkboxgroup":
      return pipe(
        options,
        A.zip(values), // Bind options and supplied values because we don't need the input's real value.
        A.map(([{ text }, value]) => new Map([[text ?? value, value]])),
        mergeLabelAndValues
      );
    case "calendar":
    case "html":
    case "listbox":
    case "radiogroup":
    case "shufflebox":
    case "shufflelist":
    case "termselector":
    case "userselector":
      return new Map<string, string>();
    default:
      return absurd(controlType);
  }
};

const buildControlValue = (
  control: BasicControlEssentials,
  values: string[]
): MockedControlValue => {
  return [mockWizardControlFactory(control), buildLabelValue(control, values)];
};

/**
 * Generate a list of mocked Wizard controls.
 */
export const generateMockedControls = (): MockedControlValue[] => {
  const orderByTitle: Ord<BasicControlEssentials> = contramap<
    O.Option<string>,
    BasicControlEssentials
  >((c: BasicControlEssentials) => O.fromNullable(c.title))(O.getOrd(S.Ord));

  const collectByTitle = M.collect<BasicControlEssentials>(orderByTitle);

  return pipe(
    controlValues,
    collectByTitle<string[], MockedControlValue>(buildControlValue)
  );
};

/**
 * Trigger a HTML DOM event for a Wizard control.
 *
 * @param container Root container where <AdvancedSearchPanel/> exists
 * @param updates A map where key is the label of a Control and value is the Control's new value.
 * @param controlType Type of the control.
 */
export const updateControlValue = (
  container: HTMLElement,
  updates: WizardControlLabelValue,
  controlType: OEQ.WizardControl.ControlType
): void => {
  const labels: string[] = pipe(updates, M.keys(S.Ord));
  const values: string[] = pipe(updates, M.values(S.Ord));

  switch (controlType) {
    case "editbox":
      userEvent.type(getByLabelText(container, labels[0]), values[0]);
      break;
    case "checkboxgroup":
      const select = (label: string, value: string): IO.IO<void> => {
        const checkbox = getByLabelText(container, label) as HTMLInputElement;
        if (
          (value === "true" && !checkbox.checked) ||
          (value === "false" && checkbox.checked)
        ) {
          userEvent.click(getByLabelText(container, label));
        } else {
          console.error("CheckBox status does not match the new value.");
        }

        return IO.of(Void.Monoid.empty);
      };
      const traverseMapWithIO = M.getTraversableWithIndex(
        S.Ord
      ).traverseWithIndex(IO.Applicative);

      traverseMapWithIO(updates, select);
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
 * Find out the values of a control based on the supplied labels.
 *
 * @param container Root container where <AdvancedSearchPanel/> exists
 * @param labels The control or its options' labels.
 * @param controlType Type of the control.
 */
export const getControlValue = (
  container: HTMLElement,
  labels: string[],
  controlType: OEQ.WizardControl.ControlType
): WizardControlLabelValue | undefined => {
  const getInput = (label: string) =>
    getByLabelText(container, label) as HTMLInputElement;
  const buildMap = (label: string, value: string) => new Map([[label, value]]);

  switch (controlType) {
    case "editbox":
      return pipe(
        labels,
        A.head, // EditBox only needs the first label which should be its title.
        O.map((label) => buildMap(label, getInput(label).value)),
        O.toUndefined
      );
    case "checkboxgroup":
      return pipe(
        labels,
        A.map((label) => buildMap(label, `${getInput(label).checked}`)),
        mergeLabelAndValues
      );
    case "calendar":
    case "html":
    case "listbox":
    case "radiogroup":
    case "shufflebox":
    case "shufflelist":
    case "termselector":
    case "userselector":
      return new Map<string, string>();
    default:
      return absurd(controlType);
  }
};
