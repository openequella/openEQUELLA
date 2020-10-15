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
import { cloneDeep } from 'lodash';

/**
 * Performs inplace conversion of specified fields with supplied converter.
 *
 * @param input The object to be processed.
 * @param targetFields List of the names of fields to convert.
 * @param recursive True if processing nested objects is required.
 * @param converter A function converting fields' type.
 */
const convertFields = <T, R>(
  input: unknown,
  targetFields: string[],
  recursive: boolean,
  converter: (value: T) => R
): void => {
  const entries: [string, any][] = Object.entries(input as any);

  entries.forEach(([field, value]) => {
    if (typeof value === 'object' && recursive) {
      convertFields(value, targetFields, recursive, converter);
    } else {
      targetFields
        .filter((targetField) => targetField === field)
        .forEach(
          (targetField) => ((input as any)[targetField] = converter(value))
        );
    }
  });
};

/**
 * Return a clone of the provided object with specified fields converted to type Date. A deep clone
 * will be undertaken, and so nested fields with matching names will also be converted.
 *
 * @param input The object to be processed.
 * @param fields List of the names of fields to convert.
 */
export const convertDateFields = <T>(input: unknown, fields: string[]): T => {
  const inputClone: any = cloneDeep(input);
  convertFields(inputClone, fields, true, (value: string) =>
    isNaN(Date.parse(value)) ? undefined : new Date(value)
  );
  return inputClone;
};

/**
 * openEQUELLA has a `CsvList` type which is used to receive an array of items in a single param
 * comma delimited. This utility function assists with those cases.
 *
 * @param list the list of items to be stringified and comma delimited.
 */
export const asCsvList = <T>(list: T[] | undefined): string | undefined =>
  list?.join(',');
