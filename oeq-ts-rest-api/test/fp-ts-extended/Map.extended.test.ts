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
import {
  evalMapToRecord,
  evalRecordToMap,
} from '../../src/fp-ts-extended/Map.extended';

describe('Map extended', () => {
  const updateValue = (value: number): string => `${value * 2}`;

  it('evalMapToRecord', () => {
    const map = new Map([
      ['a', 1],
      ['b', 2],
      ['c', 3],
    ]);

    const result = evalMapToRecord(map, updateValue);
    expect(result).toEqual({ a: '2', b: '4', c: '6' });
  });

  it('evalRecordToMap', () => {
    const record = { a: 1, b: 2, c: 3 };

    const result = evalRecordToMap(record, updateValue);
    expect(result).toEqual(
      new Map([
        ['a', '2'],
        ['b', '4'],
        ['c', '6'],
      ])
    );
  });
});
