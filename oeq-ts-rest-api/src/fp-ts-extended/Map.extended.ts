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
import { pipe } from 'fp-ts/function';
import * as M from 'fp-ts/Map';
import * as R from 'fp-ts/Record';
import * as S from 'fp-ts/string';

export * from 'fp-ts/Map';

/**
 * convert a Map to a Record with applying the provided function to each value.
 */
export const toRecord = <V1, V2>(
  map: Map<string, V1>,
  f: (value: V1) => V2
): Record<string, V2> => pipe(map, M.toArray(S.Ord), R.fromEntries, R.map(f));

/**
 * convert a Record to a Map with applying the provided function to each value.
 */
export const fromRecord = <V1, V2>(
  record: Record<string, V1>,
  f: (value: V1) => V2
): Map<string, V2> =>
  pipe(record, R.toEntries, (entries) => new Map(entries), M.map(f));
