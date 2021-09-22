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
import * as N from "fp-ts/number";
import * as O from "fp-ts/Option";
import { Refinement } from "fp-ts/Refinement";
import * as S from "fp-ts/string";
import * as React from "react";
import { WizardEditBox } from "./WizardEditBox";
import { WizardUnsupported } from "./WizardUnsupported";

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
const isStringArray = (xs: ControlValue): xs is string[] =>
  pipe(xs as unknown[], isHeadType<string>(S.isString));

/**
 * Used to check if the `ControlValue` is of the number[] variety.
 * (Not a general purpose array util!)
 */
const isNumberArray = (xs: ControlValue): xs is number[] =>
  pipe(xs as unknown[], isHeadType<number>(N.isNumber));

const eqControlTarget: Eq<ControlTarget> = struct({
  schemaNode: A.getEq(S.Eq),
  type: S.Eq,
});

const eqControlValue: Eq<ControlValue> = {
  equals: (x, y) => {
    if (isStringArray(x) && isStringArray(y)) {
      return A.getEq(S.Eq).equals(x, y);
    } else if (isNumberArray(x) && isNumberArray(y)) {
      return A.getEq(N.Eq).equals(x, y);
    }
    // The two arrays are of different or unsupported types
    return false;
  },
};

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
 * For a control which just needs a singular value, validate and split out the value from the
 * unionised ControlValue.
 *
 * @param value a potential string value
 */
const getStringControlValue = (value?: ControlValue): string | undefined => {
  const potentialValue: E.Either<Error, string | undefined> = pipe(
    value,
    O.fromNullable,
    O.match(
      () => E.right(undefined),
      flow(
        E.fromPredicate(
          isStringArray,
          () => new TypeError("Expected string[] but got something else!")
        ),
        E.map(flow(A.head, O.toUndefined))
      )
    )
  );

  if (E.isLeft(potentialValue)) {
    throw potentialValue.left;
  }

  return potentialValue.right;
};

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

  const { controlType, mandatory, title, description, size2 } = control;

  switch (controlType) {
    case "editbox":
      return (
        <WizardEditBox
          id={id}
          label={title}
          description={description}
          mandatory={mandatory}
          rows={size2}
          value={getStringControlValue(value)}
          onChange={(newValue) => onChange([newValue])}
        />
      );
    case "calendar":
    case "checkboxgroup":
    case "html":
    case "listbox":
    case "radiogroup":
    case "shufflebox":
    case "shufflelist":
    case "termselector":
    case "userselector":
      return <WizardUnsupported />;
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
 *               no value, then it will not be in the collection.
 * @param onChange The high level callback to use to update `values` - this will be wrapped so that
 *                 Wizard components only have to worry about calling a typical simple callback
 *                 with their updated value.
 */
export const render = (
  controls: OEQ.WizardControl.WizardControl[],
  values: FieldValue[],
  onChange: (update: FieldValue) => void
): JSX.Element[] => {
  /**
   * There should only be one unique value across the different nodes the control is targeting,
   * so validate that and then extract that single value.
   */
  const mergeControlValues = (
    target: ControlTarget
  ): ControlValue | undefined => {
    const uniqueValues: ControlValue[] = pipe(
      values,
      A.filter((x) => eqControlTarget.equals(target, x.target)),
      A.map(({ value }: FieldValue) => value),
      A.uniq(eqControlValue)
    );

    // Make sure there is only one possible value
    if (uniqueValues.length > 1) {
      throw new Error(
        `There should only be one value per controlType/schemaNodes, however [${
          target.type
        } for ${target.schemaNode.join()}] has ${uniqueValues.length}.`
      );
    }

    // Return either the single value, or `undefined` if there were no matches
    return pipe(uniqueValues, A.head, O.toUndefined);
  };

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

  // Retrieve the value of the specified control
  const retrieveControlsValue = (
    c: OEQ.WizardControl.WizardControl
  ): ControlValue | undefined =>
    OEQ.WizardControl.isWizardBasicControl(c)
      ? pipe(c, buildControlTarget, mergeControlValues)
      : undefined;

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
