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
import * as SET from 'fp-ts/Set';
import * as S from 'fp-ts/string';

export * from 'fp-ts/Set';

/**
 * Function to convert an array of string to a set of string.
 */
export const arrayToSet: (array: string[]) => Set<string> = SET.fromArray(
  S.Ord
);
/**
 * Function to convert a set of string to an array of string.
 */
export const setToArray: (set: Set<string>) => string[] = SET.toArray(S.Ord);
