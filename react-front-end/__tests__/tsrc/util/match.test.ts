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
import { identity } from "fp-ts/function";
import {
  simpleMatch,
  simpleMatchD,
  simpleUnionMatch,
} from "../../../tsrc/util/match";

describe("simpleMatch", () => {
  const unmatched = (s: string | number): string =>
    `there was no match for ${s}`;
  const testMatch = simpleMatch<string>({
    testing: (s) => s.toString(),
    1: (n) => n.toString(),
    _: unmatched,
  });

  it("executes the function matching the provided string", () => {
    const knownMatch = "testing";
    expect(testMatch(knownMatch)).toMatch(knownMatch);
  });

  it("executes the function matching the provided number", () => {
    const knownMatch = 1;
    expect(testMatch(knownMatch)).toMatch(`${knownMatch}`);
  });

  it("matches the default clause when no match found", () => {
    const noExistingValue = "blah";
    expect(testMatch(noExistingValue)).toMatch(unmatched(noExistingValue));
  });
});

describe("simpleMatchDynamic", () => {
  const createOption = (option: string | number): string => `option-${option}`;
  const testMatchD = simpleMatchD<string>(
    [
      [createOption(1), identity],
      [createOption("two"), identity],
    ],
    identity,
  );

  it("matches the default clause when no match found", () => {
    const noExistingValue = "blah";
    expect(testMatchD(noExistingValue)).toMatch(noExistingValue);
  });

  it("executes the function matching the provided value", () => {
    const knownMatch = createOption("two");
    expect(testMatchD(knownMatch)).toMatch(`${knownMatch}`);
  });
});

describe("simpleUnionTypeMatch", () => {
  type TestUnion = "hello" | "world" | 1;
  const cases: Partial<Record<TestUnion, () => string>> = {
    hello: () => "hello",
    1: () => "1",
  };
  const defaultValue = "default";
  const buildDefault = jest.fn().mockReturnValue(defaultValue);
  const testMatch = simpleUnionMatch(cases, buildDefault);

  it("executes the function matching the provided literal type", () => {
    const knownMatch = "hello";
    expect(testMatch(knownMatch)).toMatch(knownMatch);
  });

  it("executes the default function if the literal type does not have any function to be executed", () => {
    const knownMatch = "world";
    const result = testMatch(knownMatch);
    expect(buildDefault).toHaveBeenCalledTimes(1);
    expect(result).toMatch(defaultValue);
  });

  it("throws an error if the literal type does not have any function to be executed", () => {
    const noMatcher = simpleUnionMatch(cases);
    expect(() => noMatcher("world")).toThrow(
      "Missing matcher for literal type: world",
    );
  });
});
