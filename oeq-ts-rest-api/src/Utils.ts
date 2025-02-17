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
import * as E from 'fp-ts/Either';
import { constTrue, identity, pipe } from 'fp-ts/function';
import * as T from 'fp-ts/Task';
import * as TE from 'fp-ts/TaskEither';
import * as t from 'io-ts';
import * as PathReporter from 'io-ts/PathReporter';
import { cloneDeep, isArray, set } from 'lodash';

export const STANDARD_DATE_FIELDS = [
  'createdDate',
  'modifiedDate',
  'lastActionDate',
  'submittedDate',
];

/**
 * Function that receives an IO-TS codec and returns a new function which uses the codec to validate the provided data.
 *
 * @param codec Codec used to do a runtime type checking.
 */
export const validate =
  <T>(codec: t.Type<T>) =>
  (data: unknown): data is T =>
    pipe(
      data,
      codec.decode,
      E.match((e) => {
        console.log('Data validation failed: ' + PathReporter.failure(e));
        return false;
      }, constTrue)
    );

/**
 * Function that receives an IO-TS codec and returns a type guard for the specific type.
 *
 * @param codec Codec used to perform the type guard.
 */
export const typeGuard =
  <T>(codec: t.Type<T>) =>
  (data: unknown): data is T =>
    codec.is(data);

/**
 * The idiomatic modelling for an `object` in Typescript is to use a `Record<string, unknown>`, so
 * this is a simple type guard to help below where we want to go from an unknown potential object
 * to a type more suitable for TS contexts.
 *
 * @param r a potential `object` / `Record`
 */
const isRecord = typeGuard(t.record(t.string, t.unknown));

/**
 * Performs inplace conversion of specified fields with supplied converter.
 *
 * @param input The object to be processed.
 * @param targetFields List of the names of fields to convert.
 * @param recursive True if processing nested objects is required.
 * @param converter A function converting fields' type.
 */
const convertFields = <R>(
  input: Record<string, unknown>,
  targetFields: string[],
  recursive: boolean,
  converter: (value: unknown) => R
): void => {
  Object.entries(input).forEach(([field, value]) => {
    if (isRecord(value) && recursive) {
      convertFields(value, targetFields, recursive, converter);
    } else if (isArray(value)) {
      value.map((element) =>
        convertFields(element, targetFields, recursive, converter)
      );
    } else {
      targetFields
        .filter((targetField) => targetField === field)
        .forEach((targetField) => set(input, targetField, converter(value)));
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
  const cloneRecord = (obj: unknown): Record<string, unknown> => {
    const cloned = cloneDeep(obj);
    if (isRecord(cloned)) {
      return cloned;
    } else {
      throw new TypeError('Cloned object was not an object!');
    }
  };

  const potentialStringToDate = (
    maybeDateString: unknown
  ): Date | undefined => {
    if (typeof maybeDateString === 'string') {
      return isNaN(Date.parse(maybeDateString))
        ? undefined
        : new Date(maybeDateString);
    } else {
      return undefined;
    }
  };

  const inputClone: Record<string, unknown> = cloneRecord(input);
  convertFields(inputClone, fields, true, potentialStringToDate);

  return inputClone as T;
};

/**
 * openEQUELLA has a `CsvList` type which is used to receive an array of items in a single param
 * comma delimited. This utility function assists with those cases.
 *
 * @param list the list of items to be stringified and comma delimited.
 */
export const asCsvList = <T>(list: T[] | undefined): string | undefined =>
  list?.join(',');

/**
 * Waits for a given callback to succeed before the specified timeout.
 * Retry the callback at a given interval if it fails.
 *
 * @template T - The type of the value that `callback` resolves to.
 * @param callback - A function that returns either a value of type T or Promise<T>.
 * @param options
 * @param options.timeout - The maximum time (in milliseconds) to wait before timing out.
 * @param options.interval - The delay (in milliseconds) between retry attempts.
 * @returns A Promise that resolves with the value from `callback` once it succeeds,
 *          or rejects with an Error if the timeout is reached first.
 */
export const waitFor = async <T>(
  callback: () => Promise<T>,
  options: { timeout?: number; interval?: number } = {}
): Promise<T> => {
  // Flag to stop the retry task when the timeout is reached.
  const isTimeout = false;

  // Consider the default timeout for each test is only 5000ms, use 2000ms for the default timeout value.
  const { timeout = 2000, interval = 100 } = options;

  const raceMonoid = T.getRaceMonoid<E.Either<string, T>>();

  const timeoutTask: TE.TaskEither<string, never> = pipe(
    TE.left(`Callback is not successful after ${timeout}ms`),
    T.delay(timeout)
  );

  const retryTask: TE.TaskEither<string, T> = pipe(
    TE.tryCatch(callback, () => 'Callback failed'),
    TE.orElse(() =>
      pipe(
        T.of(undefined),
        T.delay(interval),
        T.chain(() => (isTimeout ? TE.left('Timeout') : retryTask))
      )
    )
  );

  return pipe(
    await raceMonoid.concat(retryTask, timeoutTask)(),
    E.match((e) => {
      throw new Error(e);
    }, identity)
  );
};
