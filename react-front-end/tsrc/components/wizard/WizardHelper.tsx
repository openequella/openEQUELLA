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
import * as E from "fp-ts/Either";
import { Eq, struct } from "fp-ts/Eq";
import { absurd, constFalse, flow, pipe } from "fp-ts/function";
import * as M from "fp-ts/Map";
import * as NEA from "fp-ts/NonEmptyArray";
import * as O from "fp-ts/Option";
import { Refinement } from "fp-ts/Refinement";
import * as S from "fp-ts/string";
import * as React from "react";
import { WizardCheckBoxGroup } from "./WizardCheckBoxGroup";
import { WizardEditBox } from "./WizardEditBox";
import { WizardRadioButtonGroup } from "./WizardRadioButtonGroup";
import { WizardUnsupported } from "./WizardUnsupported";

/**
 * Provide basic props a Wizard control component needs.
 */
export interface WizardControlBasicProps {
  /**
   * DOM id.
   */
  id?: string;
  /**
   * The label to display for the control.
   */
  label?: string;
  /**
   * A description to display alongside the control to assist users.
   */
  description?: string;
  /**
   * Indicate that this control is 'mandatory' to the user.
   */
  mandatory: boolean;
}

/**
 * Used to loosely target what a value (typically a `ControlValue`) is being used for.
 */
export interface ControlTarget {
  /**
   * The 'fullPath's for the targetNode.
   */
  schemaNode: string[];
  /**
   * The type of control that is being targeted.
   */
  type: OEQ.WizardControl.ControlType;
}

/**
 * Convenience type for our way of storing the two main value types across our controls. Represents
 * that some controls are textual, and some are numeric; and that some controls store more than one
 * value.
 */
export type ControlValue = number[] | string[];

/**
 * Identifies a Wizard 'field' and specifies its value.
 */
export interface FieldValue {
  target: ControlTarget;
  value: ControlValue;
}

/**
 * Collection type alias for specifying a group of field values.
 */
export type FieldValueMap = Map<ControlTarget, ControlValue>;

/**
 * Creates a function which checks the type of the head of an array using the supplied refinement
 * function. The resulting function returns true when it is passed an array who's head element
 * matches the refinement specifications, otherwise (including if the array is empty) it will
 * return false.
 *
 * @param refinement function used by returned function to validate head element.
 */
const isHeadType =
  <T,>(refinement: Refinement<unknown, T>) =>
  (xs: unknown[]): boolean =>
    pipe(xs, A.head, O.map(refinement), O.getOrElse(constFalse));

/**
 * Used to check if the `ControlValue` is of the string[] variety.
 * (Not a general purpose array util!)
 */
const isStringArray = (xs: ControlValue): xs is NEA.NonEmptyArray<string> =>
  pipe(xs as unknown[], isHeadType<string>(S.isString));

/**
 * Typically used to check if the `ControlValue` of an Option type control (e.g. CheckBox Group) is a non-empty array.
 * If you also want to confirm if the value is `string`, use `isStringArray`.
 */
const isControlValueNonEmpty = (xs: ControlValue): boolean => xs.length > 0;

const eqControlTarget: Eq<ControlTarget> = struct({
  schemaNode: A.getEq(S.Eq),
  type: S.Eq,
});

/**
 * Provides a function to insert values into a `FieldValueMap` returning a new Map instance - i.e.
 * original Map is unharmed/changed.
 */
export const fieldValueMapInsert = M.upsertAt(eqControlTarget);

/**
 * Provides a function to lookup values in a `FieldValueMap`.
 */
export const fieldValueMapLookup = M.lookup(eqControlTarget);

const buildControlTarget = (
  c: OEQ.WizardControl.WizardBasicControl
): ControlTarget => ({
  /*
   * Target nodes are stored in several different ways, for our purposes we just need a single
   * fully qualified string as provided by `fullTarget`. This function reduces the array down to
   * such a simple array.
   */
  schemaNode: c.targetNodes.map((n) => n.fullTarget),
  type: c.controlType,
});

/**
 * For a control which just needs a singular value, always retrieve the first value.
 *
 * @param value a potential string value
 */
const getStringControlValue = (value: ControlValue): string =>
  pipe(value, getStringArrayControlValue, NEA.head);

/**
 * For a control that can have one or more string values (e.g. CheckBox Group), validate and
 * retrieve the values from the unionised ControlValue.
 *
 * @param value A ControlValue which should have at least one string value.
 */
const getStringArrayControlValue = (
  value: ControlValue
): NEA.NonEmptyArray<string> =>
  pipe(
    value,
    E.fromPredicate(
      isStringArray,
      () => new TypeError("Expected non-empty string[] but got something else!")
    ),
    E.matchW(
      (error) => {
        throw error;
      },
      (value) => value
    )
  );

/**
 * Factory function responsible for taking a control definition and producing the correct React
 * component.
 */
const controlFactory = (
  id: string,
  control: OEQ.WizardControl.WizardControl,
  onChange: (_: ControlValue) => void,
  value?: ControlValue
): JSX.Element => {
  if (!OEQ.WizardControl.isWizardBasicControl(control)) {
    return <WizardUnsupported />;
  }

  const ifAvailable = <T,>(
    value: ControlValue | undefined,
    getter: (_: ControlValue) => T
  ): T | undefined =>
    pipe(
      value,
      O.fromNullable,
      O.filter(isControlValueNonEmpty),
      O.map(getter),
      O.toUndefined
    );

  const { controlType, mandatory, title, description, size1, size2, options } =
    control;

  const commonProps = {
    id,
    label: title,
    description,
    mandatory,
  };

  switch (controlType) {
    case "editbox":
      return (
        <WizardEditBox
          {...commonProps}
          rows={size2}
          value={ifAvailable<string>(value, getStringControlValue)}
          onChange={(newValue) => onChange([newValue])}
        />
      );
    case "checkboxgroup":
      return (
        <WizardCheckBoxGroup
          {...commonProps}
          options={options}
          columns={size1}
          values={ifAvailable<string[]>(value, getStringArrayControlValue)}
          onSelect={(newValue: string[]) => onChange(newValue)}
        />
      );
    case "radiogroup":
      return (
        <WizardRadioButtonGroup
          {...commonProps}
          options={options}
          columns={size1}
          value={ifAvailable<string>(value, getStringControlValue)}
          onSelect={(newValue: string) => onChange([newValue])}
        />
      );
    case "calendar":
    case "html":
    case "listbox":
    case "shufflebox":
    case "shufflelist":
    case "termselector":
    case "userselector":
      return <WizardUnsupported id={id} />;
    default:
      return absurd(controlType);
  }
};

/**
 * Produces an array of `JSX.Element`s representing the wizard defined by the provided `controls`.
 * Setting their values to those provided in `values` and configuring them with an onChange handler
 * which can set their value in the external instance of `values` - correctly targetted etc.
 *
 * Later, this will also be used for the evaluation and execution of visibility scripting.
 *
 * Later, later, this will also be able to support multi-page wizards as used during contribution.
 *
 * @param controls A collection of controls which make up a wizard.
 * @param values A collection of values for the provided controls - if a control currently has
 *               no value, then it should not be in the collection.
 * @param onChange The high level callback to use to update `values` - this will be wrapped so that
 *                 Wizard components only have to worry about calling a typical simple callback
 *                 with their updated value.
 */
export const render = (
  controls: OEQ.WizardControl.WizardControl[],
  values: FieldValueMap,
  onChange: (update: FieldValue) => void
): JSX.Element[] => {
  const buildOnChangeHandler = (
    c: OEQ.WizardControl.WizardControl
  ): ((value: ControlValue) => void) =>
    OEQ.WizardControl.isWizardBasicControl(c)
      ? (value: ControlValue) =>
          onChange({ target: buildControlTarget(c), value })
      : (_) => {
          throw new Error(
            "Unexpected onChange called for non-WizardBasicControl."
          );
        };

  const getValue = (target: ControlTarget): O.Option<ControlValue> =>
    pipe(values, fieldValueMapLookup(target));

  // Retrieve the value of the specified control
  const retrieveControlsValue: (
    _: OEQ.WizardControl.WizardControl
  ) => ControlValue | undefined = flow(
    O.fromPredicate(OEQ.WizardControl.isWizardBasicControl),
    O.chain(flow(buildControlTarget, getValue)),
    O.toUndefined
  );

  // Build the controls
  return controls.map((c, idx) =>
    controlFactory(
      `wiz-${idx}-${c.controlType}`,
      c,
      buildOnChangeHandler(c),
      retrieveControlsValue(c)
    )
  );
};
