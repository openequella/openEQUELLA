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
import { absurd, constFalse, flow, identity, pipe } from "fp-ts/function";
import * as M from "fp-ts/Map";
import * as NEA from "fp-ts/NonEmptyArray";
import * as O from "fp-ts/Option";
import { not } from "fp-ts/Predicate";
import * as RSET from "fp-ts/ReadonlySet";
import { Refinement } from "fp-ts/Refinement";
import { first } from "fp-ts/Semigroup";
import * as SET from "fp-ts/Set";
import * as S from "fp-ts/string";
import * as T from "fp-ts/Task";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { getTokensForText } from "../../modules/TokenisationModule";
import { OrdAsIs } from "../../util/Ord";
import { pfTernary } from "../../util/pointfree";
import { WizardCalendar } from "./WizardCalendar";
import { WizardCheckBoxGroup } from "./WizardCheckBoxGroup";
import { WizardEditBox } from "./WizardEditBox";
import { WizardListBox } from "./WizardListBox";
import { WizardRadioButtonGroup } from "./WizardRadioButtonGroup";
import { WizardRawHtml } from "./WizardRawHtml";
import { WizardShuffleBox } from "./WizardShuffleBox";
import { WizardShuffleList } from "./WizardShuffleList";
import { WizardUnsupported } from "./WizardUnsupported";
import { WizardUserSelector } from "./WizardUserSelector";

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
  /**
   * Whether to tokenise the value.
   */
  isValueTokenised?: boolean;
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
 * Type for Map where key is the path of a schema node and value is a ControlValue.
 */
export type PathValueMap = Map<string, ControlValue>;

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
export const isControlValueNonEmpty = (xs: ControlValue): boolean =>
  xs.length > 0;

/**
 * Type guard which not only checks if a Wizard control value is string but also checks if the value
 * is not an empty string. (Not a general purpose string util!)
 *
 * @param s A Wizard control value that may be either a string or a number.
 */
export const isNonEmptyString = (s: string | number): s is string =>
  S.isString(s) && !S.isEmpty(s);

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

export const buildControlTarget = (
  c: OEQ.WizardControl.WizardBasicControl
): ControlTarget => ({
  /*
   * Target nodes are stored in several different ways, for our purposes we just need a single
   * fully qualified string as provided by `fullTarget`. This function reduces the array down to
   * such a simple array.
   */
  schemaNode: c.targetNodes.map((n) => n.fullTarget),
  type: c.controlType,
  /**
   * If the control is an EditBox it always supports tokenisation.
   * Or if it's ShuffleList, use its the flag `isTokenise`.
   * False for others.
   */
  isValueTokenised:
    c.controlType === "editbox" ||
    (OEQ.WizardControl.isWizardShuffleListControl(c) && c.isTokenise),
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
 * Function to transform a FieldValueMap into a PathValueMap.
 *
 * @param fieldValueMap FieldValueMap to be converted to PathValueMap.
 */
export const valuesByNode = (fieldValueMap: FieldValueMap): PathValueMap => {
  const buildPathValue = (
    k: ControlTarget,
    fullMap: PathValueMap,
    v: ControlValue
  ) =>
    pipe(
      k.schemaNode,
      A.reduce<string, PathValueMap>(new Map(), (m, path) =>
        pipe(m, M.upsertAt(S.Eq)(path, v))
      ),
      M.union<string, ControlValue>(S.Eq, first<ControlValue>())(fullMap)
    );

  return pipe(
    fieldValueMap,
    M.reduceWithIndex<ControlTarget>(OrdAsIs)<PathValueMap, ControlValue>(
      new Map(),
      buildPathValue
    )
  );
};

/**
 * Coverts `WizardControlOption` arrays to a simple Map, where the `value` is also used for the key
 * if `text` is `undefined`. (Note: The old Wizard UI seemed to support the idea of duplicate
 * entries - which had the same `value`. But that seemed odd so when processing here to a proper
 * Map obviously those are dropped and the last entry wins.)
 */
const wizardControlOptionsToMap: (
  _: OEQ.WizardCommonTypes.WizardControlOption[]
) => Map<string, string> = flow(
  A.reduce(new Map<string, string>(), (acc, { text, value }) =>
    pipe(acc, M.upsertAt(S.Eq)(value, text ?? value))
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
  fieldValueMap: FieldValueMap,
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

  const valueAsStringSet = (): Set<string> =>
    new Set(ifAvailable<string[]>(value, getStringArrayControlValue) ?? []);

  const { controlType, mandatory, title, description, size1, size2, options } =
    control;

  const commonProps = {
    id,
    label: title,
    description,
    mandatory,
  };

  // For controls that have only one value, when the value is an empty string, call
  // `onChange` with an empty array instead of an array that has an empty string only.
  const onChangeForSingleValue = (newValue: string) =>
    onChange(S.isEmpty(newValue) ? [] : [newValue]);

  // For controls which return a Set<string>, convert to plain ol' array and
  // call the standard onChange handler.
  const onChangeForStringSet = flow(SET.toArray<string>(OrdAsIs), onChange);

  switch (controlType) {
    case "editbox":
      return (
        <WizardEditBox
          {...commonProps}
          rows={size2}
          value={ifAvailable<string>(value, getStringControlValue)}
          onChange={onChangeForSingleValue}
        />
      );
    case "checkboxgroup":
      return (
        <WizardCheckBoxGroup
          {...commonProps}
          options={options}
          columns={size1}
          values={ifAvailable<string[]>(value, getStringArrayControlValue)}
          onSelect={onChange}
        />
      );
    case "radiogroup":
      return (
        <WizardRadioButtonGroup
          {...commonProps}
          options={options}
          columns={size1}
          value={ifAvailable<string>(value, getStringControlValue)}
          onSelect={onChangeForSingleValue}
        />
      );
    case "calendar":
      const { dateFormat, isRange } =
        control as OEQ.WizardControl.WizardCalendarControl;

      return (
        <WizardCalendar
          {...commonProps}
          values={ifAvailable<string[]>(value, getStringArrayControlValue)}
          onChange={(newValue) => onChange(newValue)}
          dateFormat={dateFormat}
          isRange={isRange}
        />
      );
    case "html":
      return <WizardRawHtml {...commonProps} fieldValueMap={fieldValueMap} />;
    case "listbox":
      return (
        <WizardListBox
          {...commonProps}
          options={options}
          value={ifAvailable<string>(value, getStringControlValue)}
          onSelect={onChangeForSingleValue}
        />
      );
    case "shufflebox":
      return (
        <WizardShuffleBox
          {...commonProps}
          options={wizardControlOptionsToMap(options)}
          values={valueAsStringSet()}
          onSelect={onChangeForStringSet}
        />
      );
    case "shufflelist":
      return (
        <WizardShuffleList
          {...commonProps}
          values={valueAsStringSet()}
          onChange={flow(RSET.toSet, onChangeForStringSet)}
        />
      );
    case "userselector":
      return pipe(
        control as OEQ.WizardControl.WizardUserSelectorControl,
        ({ isSelectMultiple, isRestricted, restrictedTo }) => (
          <WizardUserSelector
            {...commonProps}
            groupFilter={isRestricted ? new Set(restrictedTo) : new Set()}
            multiple={isSelectMultiple}
            onChange={flow(RSET.toSet, onChangeForStringSet)}
            users={valueAsStringSet()}
          />
        )
      );
    case "termselector":
      return <WizardUnsupported id={id} />;
    default:
      return absurd(controlType);
  }
};

/**
 * Produces an array of `JSX.Element`s representing the wizard defined by the provided `controls`.
 * Setting their values to those provided in `values` and configuring them with an onChange handler
 * which can set their value in the external instance of `values` - correctly targeted etc.
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
      values,
      retrieveControlsValue(c)
    )
  );
};

/**
 * Extract each Control's target schema node and default values, and build a FieldValueMap
 * based on extracted data.
 *
 * @param controls A list of Wizard controls.
 */
export const extractDefaultValues = (
  controls: OEQ.WizardControl.WizardControl[]
): FieldValueMap => {
  const addQueryValueToMap = (
    m: FieldValueMap,
    c: OEQ.WizardControl.WizardBasicControl
  ) =>
    fieldValueMapInsert<ControlValue>(
      buildControlTarget(c),
      c.defaultValues
    )(m);

  return pipe(
    controls,
    A.filter(OEQ.WizardControl.isWizardBasicControl),
    A.reduce<OEQ.WizardControl.WizardBasicControl, FieldValueMap>(
      new Map(),
      addQueryValueToMap
    )
  );
};

const AND = " AND ";

/**
 * Create the fieldname matching the format of the fields used in the oEQ lucene index. Specifically, for any
 * schema node which is tokenised, there are two fields. One which contains full raw values, and another which
 * has gone through the analyzer (and tokenizer). This name of this second type of has an asteriks (`*`) suffix.
 *
 * This function then looks after adding this special suffix when required based on the `isTokenised` flag.
 */
const fieldName = (field: string, isTokenised = false): string =>
  `${field}${isTokenised ? "\\*" : ""}`;

/**
 * Given an array of values, wrap each value by a pair of double quotes. This can make sure if a value is a phrase,
 * the phrase will not get separated into multiple words on server.
 */
const fieldValue = (value: ControlValue): string[] =>
  value.map((v) => `"${v}"`);

// Function to create a Task which builds a raw Lucene query for each control type.
const queryFactory = (
  { type, schemaNode, isValueTokenised }: ControlTarget,
  values: ControlValue
): TE.TaskEither<string, string> => {
  // Values processed to to have each one wrapped by double quotes. However, Calendar and those doing
  // tokenisation should not use this.
  const _values = fieldValue(values);

  // Function to build a query string containing only one 'field:value' for one path.
  const singleValueQueryBuilder = (
    path: string,
    values: ControlValue
  ): string => `${path}:${values[0]}`;

  // Function to build and join multiple 'field:value' into one query string for one path.
  const multipleValueQueryBuilder = (
    path: string,
    values: ControlValue
  ): string => `${path}:(${values.join(" ")})`; // Put all words in one group and separate each word by a whitespace.

  // Function to build query strings for multiple paths and join all into one query.
  const buildQueries = (
    schemaNode: string[],
    values: ControlValue,
    builder: (p: string, v: ControlValue) => string = singleValueQueryBuilder
  ): string =>
    `(${schemaNode
      .map((n) => builder(fieldName(n, isValueTokenised), values))
      .join(" OR ")})`;

  const factory = async (): Promise<string> => {
    switch (type) {
      case "calendar":
        // If the provided date is undefined or is an empty string, use `*` instead.
        const processDateString = (d: string | undefined): string =>
          pipe(
            O.fromNullable(d),
            O.filter(not(S.isEmpty)),
            O.getOrElse(() => "*")
          );

        const dateRangeQueryBuilder = (
          path: string,
          [startDate, endDate]: ControlValue
        ) => `${path}:[${startDate} TO ${endDate}]`;

        // Validate the Calendar value type.
        const dateRange: NEA.NonEmptyArray<string> = pipe(
          values,
          E.fromPredicate(
            isStringArray,
            () => "Calendar must have at least one value"
          ),
          E.chain(
            E.fromPredicate(
              (dates) => A.size(dates) <= 2,
              () => "Calendar cannot have more than 2 values"
            )
          ),
          E.fold((e) => {
            throw new TypeError(e);
          }, identity)
        );

        return pipe(
          dateRange,
          E.fromPredicate(A.exists(not(S.isEmpty)), () => ""), // No query needed when both are undefined so return an empty string.
          E.map(A.map(processDateString)),
          E.fold(identity, (dates) =>
            buildQueries(schemaNode, dates, dateRangeQueryBuilder)
          )
        );
      case "checkboxgroup":
        return buildQueries(schemaNode, _values, multipleValueQueryBuilder);
      case "editbox":
        const { tokens } = await getTokensForText(`${values[0]}`);
        return buildQueries(schemaNode, tokens, multipleValueQueryBuilder);
      case "html":
        return "";
      case "listbox":
        return buildQueries(schemaNode, _values);
      case "radiogroup":
        return buildQueries(schemaNode, _values);
      case "shufflebox":
        return buildQueries(schemaNode, _values, multipleValueQueryBuilder);
      case "shufflelist":
        const getShuffleTokens = async () => {
          const { tokens } = await getTokensForText(values.join(" "));
          return tokens;
        };

        return buildQueries(
          schemaNode,
          isValueTokenised ? await getShuffleTokens() : _values,
          multipleValueQueryBuilder
        );
      case "termselector":
        return buildQueries(schemaNode, _values, multipleValueQueryBuilder);
      case "userselector":
        return buildQueries(schemaNode, _values, multipleValueQueryBuilder);
      default:
        return absurd(type);
    }
  };

  return TE.tryCatch(
    factory,
    (e) =>
      `Failed to build Lucene query or path ${schemaNode.join()} (control type: ${type}) due to error: ${e}`
  );
};

/**
 * Function to build a raw Lucene query string based on the values of Wizard controls.
 * Since some controls support tokenisation, the function will send a request to server
 * to retrieve tokens. As a result, this function returns a Promise of string eventually.
 *
 * @param m Map which provides ControlTarget and ControlValue.
 */
export const generateRawLuceneQuery = async (
  m: FieldValueMap
): Promise<string | undefined> => {
  // Because it's likely to have multiple errors when building the whole query,
  // lift `queryFactory` so that it returns a TaskEither where left is an array of string.
  const liftedQueryFactory = (
    t: ControlTarget,
    v: ControlValue
  ): TE.TaskEither<string[], string> =>
    pipe(
      queryFactory(t, v),
      TE.mapLeft((a) => [a])
    );

  // A task to build and return raw Lucene queries for all the controls,
  // or throw errors.
  const queryTask = pipe(
    m,
    M.filter<ControlValue>(isControlValueNonEmpty),
    M.collect<ControlTarget>(OrdAsIs)(liftedQueryFactory),
    // Convert an array of TE to an TE where left and right are both string[].
    A.sequence(
      TE.getApplicativeTaskValidation(
        T.ApplicativeSeq,
        A.getSemigroup<string>()
      )
    ),
    T.map(
      flow(
        E.fold((e) => {
          throw new Error(e.join());
        }, identity)
      )
    )
  );

  // Now run the task.
  const result: string[] = await queryTask();
  // Remove any sub query that is just an empty string and then join the remaining with `AND`.
  const queries: string = result.filter(isNonEmptyString).join(AND);

  // If the final query is still an empty string, return undefined instead.
  return pipe(
    queries,
    pfTernary<string, string | undefined>(
      isNonEmptyString,
      identity,
      () => undefined
    )
  );
};
