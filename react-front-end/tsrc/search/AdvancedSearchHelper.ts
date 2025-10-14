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
import { absurd, flow, identity, pipe } from "fp-ts/function";
import * as M from "fp-ts/Map";
import * as NEA from "fp-ts/NonEmptyArray";
import * as O from "fp-ts/Option";
import { not } from "fp-ts/Predicate";
import * as RA from "fp-ts/ReadonlyArray";
import * as S from "fp-ts/string";
import { createContext } from "react";
import {
  ControlTarget,
  ControlValue,
  controlValueToStringArray,
  FieldValueMap,
  getStringArrayControlValue,
  isControlValueNonEmpty,
  isNonEmptyString,
  isPathValueMap,
  isStringArray,
  PathValueMap,
} from "../components/wizard/WizardHelper";
import { OrdAsIs } from "../util/Ord";
import { pfTernaryTypeGuard } from "../util/pointfree";
import type { SearchPageOptions } from "./SearchPageHelper";

const nop = () => {};

interface AdvancedSearchPageContextProps {
  /**
   * Function to update each control's value.
   */
  updateFieldValueMap: (fieldValueMap: FieldValueMap) => void;
  /**
   * Function to control whether the Advanced search panel is open.
   */
  openAdvancedSearchPanel: (open: boolean) => void;
  /**
   * `true` when the Advanced search definition is retrieved from server.
   */
  definitionRetrieved: boolean;
}

export const AdvancedSearchPageContext =
  createContext<AdvancedSearchPageContextProps>({
    updateFieldValueMap: nop,
    openAdvancedSearchPanel: nop,
    definitionRetrieved: false,
  });

/**
 *  Function to pull values of PathValueMap and copy to FieldValueMap for each unique Schema node.
 *
 * @param pathValueMap PathValueMap which provides values of all Schema nodes
 * @param fieldValueMap FieldValueMap where values will be updated based on the supplied PathValueMap.
 */
export const buildFieldValueMapFromPathValueMap = (
  pathValueMap: PathValueMap,
  fieldValueMap: FieldValueMap,
): FieldValueMap => {
  const pMap = pipe(pathValueMap, M.map(controlValueToStringArray));

  // Given a list of Schema nodes, find out the array of values for each node and then flatten all values into
  // one array.
  const fromPathValueMap = (
    { schemaNode }: ControlTarget,
    defaultValue: ReadonlyArray<string>,
  ): ControlValue =>
    pipe(
      schemaNode,
      A.map((node) =>
        pipe(
          pMap,
          M.lookup(S.Eq)(node),
          O.getOrElse(() => defaultValue),
        ),
      ),
      RA.flatten,
      RA.toArray,
    );

  return pipe(
    fieldValueMap,
    M.map(controlValueToStringArray),
    M.mapWithIndex<ControlTarget, ReadonlyArray<string>, ControlValue>(
      fromPathValueMap,
    ),
  );
};

/**
 * Function to confirm the initial FieldValueMap.
 * 1. Extract FieldValueMap or PathValueMap from query strings. If none is returned, fall back to the FieldValueMap
 *    provided by the state.
 * 2. If the first steps returns PathValueMap then converts it to FieldValueMap.
 * 3. Return the FieldValueMap if it is defined. Otherwise, return the supplied default FieldValueMap.
 *
 * @param defaultValues The default
 * @param stateSearchOptions The SearchPageOptions managed by State.
 * @param queryStringSearchOptions The SearchPageOptions transformed from query strings.
 *
 * @return The FieldValueMap used in the initial Search.
 */
export const confirmInitialFieldValueMap = (
  defaultValues: FieldValueMap,
  stateSearchOptions: SearchPageOptions,
  queryStringSearchOptions?: SearchPageOptions,
): FieldValueMap =>
  pipe(
    queryStringSearchOptions,
    O.fromNullable,
    O.map(
      ({ advFieldValue, legacyAdvSearchCriteria }) =>
        advFieldValue ?? legacyAdvSearchCriteria,
    ),
    O.getOrElseW(() => stateSearchOptions.advFieldValue),
    flow(
      O.fromNullable,
      O.map(
        pfTernaryTypeGuard<PathValueMap, FieldValueMap, FieldValueMap>(
          isPathValueMap,
          (m) => buildFieldValueMapFromPathValueMap(m, defaultValues),
          identity,
        ),
      ),
    ),
    O.getOrElse(() => defaultValues),
  );

// Function to create an Advanced search criterion for each control type.
const queryFactory = (
  { type, schemaNode, isValueTokenised }: ControlTarget,
  values: ControlValue,
): OEQ.Search.WizardControlFieldValue | undefined => {
  const buildTextFieldQuery = (): OEQ.Search.WizardControlFieldValue => ({
    schemaNodes: schemaNode,
    values: getStringArrayControlValue(values),
    queryType: isValueTokenised ? "Tokenised" : "Phrase",
  });

  // Validate the Calendar value type.
  const validDateRangeOrThrow: (_: ControlValue) => NEA.NonEmptyArray<string> =
    flow(
      E.fromPredicate(
        isStringArray,
        () => "Calendar must have at least one value",
      ),
      E.chain(
        E.fromPredicate(
          (dates) => A.size(dates) <= 2,
          () => "Calendar cannot have more than 2 values",
        ),
      ),
      E.fold((e) => {
        throw new TypeError(e);
      }, identity),
    );

  switch (type) {
    case "calendar":
      return pipe(
        values,
        validDateRangeOrThrow,
        O.fromPredicate(A.exists(not(S.isEmpty))), // No query needed when both are empty strings.
        O.map<NEA.NonEmptyArray<string>, OEQ.Search.WizardControlFieldValue>(
          (vs) => ({
            schemaNodes: schemaNode,
            values: vs,
            queryType: "DateRange",
          }),
        ),
        O.toUndefined,
      );
    case "checkboxgroup":
      return buildTextFieldQuery();
    case "editbox":
      return buildTextFieldQuery();
    case "html":
      return undefined;
    case "listbox":
      return buildTextFieldQuery();
    case "radiogroup":
      return buildTextFieldQuery();
    case "shufflebox":
      return buildTextFieldQuery();
    case "shufflelist":
      return buildTextFieldQuery();
    case "termselector":
      return buildTextFieldQuery();
    case "userselector":
      return buildTextFieldQuery();
    default:
      return absurd(type);
  }
};

/**
 * Function to build Advanced search criteria based on Wizard controls' schema nodes and values.
 *
 * @param m Map which provides ControlTarget and ControlValue.
 */
export const generateAdvancedSearchCriteria = (
  m: FieldValueMap,
): OEQ.Search.WizardControlFieldValue[] =>
  pipe(
    m,
    M.filter<ControlValue>(isControlValueNonEmpty),
    M.collect<ControlTarget>(OrdAsIs)(queryFactory),
    A.filter(
      (criterion): criterion is OEQ.Search.WizardControlFieldValue =>
        criterion !== undefined,
    ),
  );

/**
 * Check if any Advanced search criteria has been set.
 *
 * @param queryValues FieldValueMap which contains a list of ControlTargets and their values.
 */
export const isAdvSearchCriteriaSet = (queryValues: FieldValueMap): boolean => {
  const isAnyFieldSet = pipe(
    queryValues,
    // Some controls like Calendar may have an empty string as their default values which should be
    // filtered out.
    M.map(A.filter(isNonEmptyString)),
    M.values<ControlValue>(OrdAsIs),
    A.some(isControlValueNonEmpty),
  );
  const isValueMapNotEmpty = !M.isEmpty(queryValues);

  return isValueMapNotEmpty && isAnyFieldSet;
};
