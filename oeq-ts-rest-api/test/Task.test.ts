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
import * as A from 'fp-ts/Array';
import { constFalse, pipe } from 'fp-ts/function';
import * as O from 'fp-ts/Option';
import { not } from 'fp-ts/Predicate';
import * as S from 'fp-ts/string';
import * as OEQ from '../src';
import { getCounts } from '../src/Task';
import * as TC from './TestConfig';
import { logout } from './TestUtils';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));
afterAll(() => logout(TC.API_PATH));

describe('getCounts', () => {
  // There is a standard of 14 task and notification types - including parents
  const TOTAL_TASK_AND_NOTIFICATION_TYPES = 14;

  it('should return all item counts when ignoreZero is false', async () => {
    const result = await getCounts(TC.API_PATH, false, true);

    expect(result.start).toBe(0);

    expect(result).toHaveLength(TOTAL_TASK_AND_NOTIFICATION_TYPES);
    expect(result.available).toBe(TOTAL_TASK_AND_NOTIFICATION_TYPES);
    expect(result.results).toHaveLength(TOTAL_TASK_AND_NOTIFICATION_TYPES);

    const allToHaveName = pipe(
      result.results,
      A.every(({ name }) =>
        pipe(name, O.fromNullable, O.match(constFalse, not(S.isEmpty)))
      )
    );
    expect(allToHaveName).toBe(true);
  });

  it('should return only positive item counts when ignoreZero is true', async () => {
    const result = await getCounts(TC.API_PATH, true, true);

    // Based on the test data, we expect 7 types to have counts > 0
    const testUsersCount = 7;

    expect(result.start).toBe(0);
    expect(result).toHaveLength(testUsersCount);
    expect(result.available).toBe(testUsersCount);

    // When ignoreZero is true, all results should have count > 0
    const allItemsHaveNonZeroCount = pipe(
      result.results,
      A.every(({ count }) => count > 0)
    );
    expect(allItemsHaveNonZeroCount).toBe(true);
  });

  it('should return full list of ids and counts (including zeros) - but nothing else when both includeCounts and ignoreZero is false', async () => {
    const result = await getCounts(TC.API_PATH, false, false);

    expect(result.start).toBe(0);
    expect(result).toHaveLength(TOTAL_TASK_AND_NOTIFICATION_TYPES);
    expect(result.available).toBe(TOTAL_TASK_AND_NOTIFICATION_TYPES);

    const isAbsent = (value: unknown) => pipe(value, O.fromNullable, O.isNone);
    const allHaveOnlyIdAndCount = pipe(
      result.results,
      // id and count are required properties, so the validators will have already checked they're present.
      // We just need to check that name and parent are absent.
      A.every(({ name, parent }) => isAbsent(name) && isAbsent(parent))
    );
    expect(allHaveOnlyIdAndCount).toBe(true);
  });
});
