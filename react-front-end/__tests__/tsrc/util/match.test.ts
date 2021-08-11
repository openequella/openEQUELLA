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
import { simpleMatch, simpleMatchD } from "../../../tsrc/util/match";

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
    identity
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
