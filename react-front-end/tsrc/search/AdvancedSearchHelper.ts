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
import * as RA from "fp-ts/ReadonlyArray";
import * as E from "fp-ts/Either";
import { absurd, identity, pipe } from "fp-ts/function";
import * as M from "fp-ts/Map";
import * as NEA from "fp-ts/NonEmptyArray";
import * as O from "fp-ts/Option";
import { not } from "fp-ts/Predicate";
import * as S from "fp-ts/string";
import {
  ControlTarget,
  ControlValue,
  controlValueToStringArray,
  extractDefaultValues,
  FieldValueMap,
  getStringArrayControlValue,
  isControlValueNonEmpty,
  isPathValueMap,
  isStringArray,
  PathValueMap,
} from "../components/wizard/WizardHelper";
import { OrdAsIs } from "../util/Ord";
import { pfTernaryTypeGuard } from "../util/pointfree";
import { Action as SearchPageModeAction } from "./SearchPageModeReducer";

/**
 *  Function to pull values of PathValueMap and copy to FieldValueMap for each unique Schema node.
 *
 * @param pathValueMap PathValueMap which provides values of all Schema nodes
 * @param fieldValueMap FieldValueMap where values will be updated based on the supplied PathValueMap.
 */
export const buildFieldValueMapFromPathValueMap = (
  pathValueMap: PathValueMap,
  fieldValueMap: FieldValueMap
): FieldValueMap => {
  const pMap = pipe(pathValueMap, M.map(controlValueToStringArray));

  // Given a list of Schema nodes, find out the array of values for each node and then flatten all values into
  // one array.
  const fromPathValueMap = (
    { schemaNode }: ControlTarget,
    defaultValue: ReadonlyArray<string>
  ): ControlValue =>
    pipe(
      schemaNode,
      A.map((node) =>
        pipe(
          pMap,
          M.lookup(S.Eq)(node),
          O.getOrElse(() => defaultValue)
        )
      ),
      A.map(RA.toArray),
      A.flatten
    );

  return pipe(
    fieldValueMap,
    M.map(controlValueToStringArray),
    M.mapWithIndex<ControlTarget, ReadonlyArray<string>, ControlValue>(
      fromPathValueMap
    )
  );
};

/**
 * Function to initialise an Advanced search. There are two tasks done here.
 *
 * 1. Confirm the initial FieldValueMap. If there is one, use it depending on whether it's a FieldValueMap
 * or a PathValueMap. Otherwise, build a new one by extracting the default Wizard control values.
 * 2. Update the state of SearchPageModeReducer to `initialiseAdvSearch`;
 *
 * @param advancedSearchDefinition The initial Advanced search definition.
 * @param dispatch The `dispatch` provided by SearchPageModeReducer.
 * @param currentFieldValue A Map which can be either a FieldValueMap or a PathValueMap.
 */
export const initialiseAdvancedSearch = (
  advancedSearchDefinition: OEQ.AdvancedSearch.AdvancedSearchDefinition,
  dispatch: (action: SearchPageModeAction) => void,
  currentFieldValue?: FieldValueMap | PathValueMap
): FieldValueMap => {
  const defaultValues = extractDefaultValues(advancedSearchDefinition.controls);

  const initialQueryValues = pipe(
    currentFieldValue,
    O.fromNullable,
    O.map(
      pfTernaryTypeGuard<PathValueMap, FieldValueMap, FieldValueMap>(
        isPathValueMap,
        (m) => buildFieldValueMapFromPathValueMap(m, defaultValues),
        identity
      )
    ),
    O.getOrElse(() => defaultValues)
  );

  dispatch({
    type: "initialiseAdvSearch",
    selectedAdvSearch: advancedSearchDefinition,
    initialQueryValues,
  });

  return initialQueryValues;
};

// Function to create an Advanced search criterion for each control type.
const queryFactory = (
  { type, schemaNode, isValueTokenised }: ControlTarget,
  values: ControlValue
): OEQ.Search.WizardControlFieldValue | undefined => {
  const buildTextFieldQuery = (): OEQ.Search.WizardControlFieldValue => ({
    schemaNodes: schemaNode,
    values: getStringArrayControlValue(values),
    queryType: isValueTokenised ? "Tokenised" : "Phrase",
  });

  switch (type) {
    case "calendar":
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
        O.fromPredicate(A.exists(not(S.isEmpty))), // No query needed when both are empty strings.
        O.map<NEA.NonEmptyArray<string>, OEQ.Search.WizardControlFieldValue>(
          (vs) => ({
            schemaNodes: schemaNode,
            values: vs,
            queryType: "DateRange",
          })
        ),
        O.toUndefined
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
  m: FieldValueMap
): OEQ.Search.WizardControlFieldValue[] =>
  pipe(
    m,
    M.filter<ControlValue>(isControlValueNonEmpty),
    M.collect<ControlTarget>(OrdAsIs)(queryFactory),
    A.filter(
      (criterion): criterion is OEQ.Search.WizardControlFieldValue =>
        criterion !== undefined
    )
  );
