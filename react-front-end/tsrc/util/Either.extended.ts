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
import { flow } from "fp-ts/function";
import * as A from "fp-ts/Array";
import * as E from "fp-ts/Either";
import * as S from "fp-ts/string";

import type { Errors } from "io-ts";

export * from "fp-ts/Either";

const buildErrorFromValidation: (e: Errors) => Error = flow(
  A.map(({ message }) => message),
  A.filter(S.isString),
  A.intercalate(S.Monoid)("\n"),
  (error) => new Error(error)
);

const buildErrorFromString = (e: string | Errors): Error =>
  typeof e === "string" ? new Error(e) : buildErrorFromValidation(e);

const buildError = (e: string | Error | Errors): Error =>
  e instanceof Error ? e : buildErrorFromString(e);

/**
 * Given an Either, return the value or throw an error.
 *
 * @param either Either where the left could be a string, an instance of Error, or an instance of io-ts Errors
 */
export const getOrThrow: <T>(
  either: E.Either<string | Error | Errors, T>
) => T = flow(
  E.mapLeft(buildError),
  E.getOrElseW((error) => {
    throw error;
  })
);
