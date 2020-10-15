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
import * as OEQ from '../src';
import { is } from 'typescript-is';
import { asCsvList } from '../src/Utils';

describe('Convert date fields', () => {
  interface StringDate {
    dates: {
      yesterday: string;
      today: string;
    };
  }
  interface StandardDate {
    dates: {
      yesterday: Date;
      today: Date;
    };
  }
  interface InvalidDate {
    dates: {
      yesterday: undefined;
      today: undefined;
    };
  }

  it('should convert valid date strings to objects of Date', () => {
    const validDates: StringDate = {
      dates: {
        yesterday: '2020-06-11T11:45:24.296+10:00',
        today: '2020-06-12T11:45:24.296+10:00',
      },
    };

    const dates = OEQ.Utils.convertDateFields<StandardDate>(validDates, [
      'yesterday',
      'today',
    ]);
    expect(is<StandardDate>(dates)).toBe(true);
  });

  it('should return undefined for fields that have invalid date strings', () => {
    const invalidDates: StringDate = {
      dates: {
        yesterday: 'hello',
        today: 'world',
      },
    };
    const dates = OEQ.Utils.convertDateFields<StandardDate>(invalidDates, [
      'yesterday',
      'today',
    ]);
    expect(is<InvalidDate>(dates)).toBe(true);
  });
});

describe('Support for server CsvList params', () => {
  it('with lists of strings', () =>
    expect(
      asCsvList<string>(['one', 'two', 'three'])
    ).toEqual('one,two,three'));

  it('with lists of numbers', () =>
    expect(
      asCsvList<number>([1, 2, 3])
    ).toEqual('1,2,3'));
});
