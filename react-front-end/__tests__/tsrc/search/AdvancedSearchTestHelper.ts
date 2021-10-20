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
import * as E from "fp-ts/Either";
import { contramap, Ord } from "fp-ts/Ord";
import * as S from "fp-ts/string";
import * as IO from "fp-ts/IO";
import { selectOption } from "../MuiTestHelpers";

export const wizardControlBlankLabel = "!!BLANK LABEL!!";
export const editBoxEssentials: BasicControlEssentials = {
  title: "Test Edit Box",
  mandatory: false,
  schemaNodes: [{ target: "/item/name", attribute: "" }],
  controlType: "editbox",
  options: [{ text: "default value", value: "test" }],
  defaultValues: ["EditBox default value"],
};

export const oneEditBoxWizard = (
  mandatory: boolean,
  defaultValues: string[] = []
): OEQ.AdvancedSearch.AdvancedSearchDefinition => ({
  ...getAdvancedSearchDefinition,
  controls: [{ ...editBoxEssentials, mandatory, defaultValues }].map(
    mockWizardControlFactory
  ),
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
      defaultValues: ["1"],
      options: [
        {
          text: "CheckBox one",
          value: "1",
          isDefaultValue: true,
        },
        {
          text: "CheckBox two",
          value: "2",
          isDefaultValue: false,
        },
        {
          text: "CheckBox three",
          value: "3",
          isDefaultValue: false,
        },
        {
          text: "CheckBox four",
          value: "4",
          isDefaultValue: false,
        },
      ],
    },
    ["true", "true", "false", "false"], // The values determine whether to turn on or off a checkbox.
  ],
  [
    {
      title: "RadioButton Group",
      schemaNodes: [{ target: "/item/option", attribute: "" }],
      mandatory: false,
      controlType: "radiogroup",
      defaultValues: ["1"],
      options: [
        {
          text: "RadioButton one",
          value: "1",
        },
        {
          text: "RadioButton two",
          value: "2",
        },
        {
          text: "RadioButton three",
          value: "3",
        },
      ],
    },
    ["true", "false", "false"], // RadioButton group can have only one option selected.
  ],
  [
    {
      title: "ListBox",
      schemaNodes: [{ target: "/item/language", attribute: "" }],
      mandatory: false,
      controlType: "listbox",
      defaultValues: ["option two"],
      options: [
        {
          text: "option one",
          // Make value the same as text so later on when we call `selectOption`
          // we can use value as the option label.
          value: "option one",
        },
        {
          text: "option two",
          value: "option two",
        },
        {
          text: "option three",
          value: "option three",
        },
      ],
    },
    ["option one"],
  ],
]);

// Alias for the Map including a Wizard control's labels and values. However, the value can refer to
// the real input value or the status of attribute `checked`.
type WizardControlLabelValue = Map<string, string>;

/**
 * Used to specify the values for a control identified by their labels. Where the first element of
 * the tuple is the control definition, and the second is an array of labels and values.
 */
export type MockedControlValue = [
  OEQ.WizardControl.WizardBasicControl,
  WizardControlLabelValue
];

// Function to build a `WizardControlLabelValue` for an Option type control with supplied values.
const buildLabelValueForOption = (
  options: OEQ.WizardCommonTypes.WizardControlOption[],
  values: string[]
): WizardControlLabelValue =>
  pipe(
    options,
    A.zip(values),
    A.reduce(new Map<string, string>(), (m, [{ text }, value]) =>
      pipe(m, M.upsertAt(S.Eq)(text ?? wizardControlBlankLabel, value))
    )
  );

// Function to build a `WizardControlLabelValue` for controls that only need title and one value.
const buildLabelValueForControl = (
  title: string = wizardControlBlankLabel,
  value: string
) => new Map([[title, value]]);

// Function to build a `WizardControlLabelValue` for the supplied control and its values.
const buildLabelValue = (
  { controlType, title, options }: BasicControlEssentials,
  values: string[]
): WizardControlLabelValue => {
  switch (controlType) {
    case "editbox":
      return buildLabelValueForControl(title, values[0]);
    case "checkboxgroup":
      return buildLabelValueForOption(options, values);
    case "radiogroup":
      return buildLabelValueForOption(options, values);
    case "listbox":
      return buildLabelValueForControl(title, values[0]);
    case "calendar":
    case "html":
    case "shufflebox":
    case "shufflelist":
    case "termselector":
    case "userselector":
      return new Map<string, string>();
    default:
      return absurd(controlType);
  }
};

/**
 * Generate a list of mocked Wizard controls.
 *
 * @param useDefaultValues `true` to use each mocked control's default values.
 */
export const generateMockedControls = (
  useDefaultValues: boolean
): MockedControlValue[] => {
  const orderByTitle: Ord<BasicControlEssentials> = contramap<
    O.Option<string>,
    BasicControlEssentials
  >((c: BasicControlEssentials) => O.fromNullable(c.title))(O.getOrd(S.Ord));

  const collectByTitle = M.collect<BasicControlEssentials>(orderByTitle);
  const buildControlValue = (
    control: BasicControlEssentials,
    values: string[]
  ): MockedControlValue => [
    mockWizardControlFactory(control),
    buildLabelValue(control, useDefaultValues ? control.defaultValues : values),
  ];

  return pipe(
    controlValues,
    collectByTitle<string[], MockedControlValue>(buildControlValue)
  );
};

/**
 * Change the value(s) of a control using the appropriate DOM events mimicking user interaction.
 *
 * @param container Root container where <AdvancedSearchPanel/> exists
 * @param updates A Map where the keys are labels for the controls to updated, and the values are
 * the values the specified controls should be set to.
 * @param controlType Type of the control which is used to determine the method of setting the values.
 */
export const updateControlValue = (
  container: HTMLElement,
  updates: WizardControlLabelValue,
  controlType: OEQ.WizardControl.ControlType
): void => {
  const [labels, values] = A.unzip(M.toArray(S.Ord)(updates));
  // Function to apply a side effect to each update in IO.
  const traverseUpdates = (f: (label: string, value: string) => IO.IO<void>) =>
    M.getTraversableWithIndex(S.Ord).traverseWithIndex(IO.Applicative)(
      updates,
      f
    );

  switch (controlType) {
    case "editbox":
      const editBox = getByLabelText(container, labels[0]);
      userEvent.clear(editBox);
      userEvent.type(editBox, values[0]);
      break;
    case "checkboxgroup":
      const selectCheckBox =
        (label: string, value: string): IO.IO<void> =>
        () => {
          const checkbox = getByLabelText(container, label) as HTMLInputElement;
          pipe(
            value,
            E.fromPredicate<string, string>(
              (v) => ["true", "false"].includes(v),
              () => "Non-boolean specifier provided"
            ),
            E.map<string, boolean>((v) => v === "true"),
            E.chain<string, boolean, boolean>(
              E.fromPredicate(
                (toBeSelected) => toBeSelected !== checkbox.checked,
                () => "CheckBox status does not match the new value"
              )
            ),
            E.fold<string, boolean, void>(console.error, () =>
              userEvent.click(checkbox)
            )
          );
        };

      traverseUpdates(selectCheckBox)();
      break;
    case "radiogroup":
      // Radiogroup should have onle one label provided.
      userEvent.click(getByLabelText(container, labels[0]));
      break;
    case "listbox":
      selectOption(container, `#${labels[0]}-select`, values[0]);
      break;
    case "calendar":
    case "html":
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
 * @param useOptionStatus `true` to use the attribute `checked` as the value.
 */
export const getControlValue = (
  container: HTMLElement,
  labels: string[],
  controlType: OEQ.WizardControl.ControlType,
  useOptionStatus: boolean = false
): WizardControlLabelValue | undefined => {
  const getInput = (label: string) =>
    getByLabelText(container, label) as HTMLInputElement;
  const buildMap = (label: string, value: string) => new Map([[label, value]]);

  // Function to build WizardControlLabelValue for CheckBox type controls.
  const getOptionValues = (_labels: string[]) =>
    pipe(
      _labels,
      A.map((label) =>
        pipe(label, getInput, (input) => ({
          label,
          value: `${useOptionStatus ? input.checked : input.value}`,
        }))
      ),
      A.reduce(new Map<string, string>(), (m, { label, value }) =>
        pipe(m, M.upsertAt(S.Eq)(label, value))
      )
    );

  switch (controlType) {
    case "editbox":
      return pipe(
        labels,
        A.head, // EditBox only needs the first label which should be its title.
        O.map((label) => buildMap(label, getInput(label).value)),
        O.toUndefined
      );
    case "checkboxgroup":
      return getOptionValues(labels);
    case "radiogroup":
      return getOptionValues(labels);
    case "listbox":
      return pipe(
        labels,
        A.head, // EditBox only needs the first label which should be its title.
        O.map((label) => {
          return buildMap(label, getInput(label).value);
        }),
        O.toUndefined
      );
    case "calendar":
    case "html":
    case "shufflebox":
    case "shufflelist":
    case "termselector":
    case "userselector":
      return new Map<string, string>();
    default:
      return absurd(controlType);
  }
};
