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
import { pipe } from "fp-ts/function";
import {
  buildVisibilityScript,
  ScriptContext,
} from "../../../tsrc/modules/ScriptingModule";
import "../FpTsMatchers";
import { expectRight } from "../FpTsMatchers";

describe("buildVisibilityScript", () => {
  const testData = {
    path: "/item/name",
    value: "some value",
    uuid: "86a958ae-9166-4389-ab38-2c87169a5ebc",
  };

  // A static easy to test implementation closing over `testData` above.
  const scriptContext: ScriptContext = {
    user: {
      getEmail: () => undefined,
      getID: () => "tester",
      getFirstName: () => "Test",
      getLastName: () => "User",
      getUsername: () => "tester",
      hasRole: (uuid: string): boolean => uuid === testData.uuid,
    },
    xml: {
      contains: (xpath: string, value: string): boolean =>
        xpath === testData.path && value === testData.value,
      get: (xpath: string): string =>
        xpath === testData.path ? testData.value : "unknown xpath",
      getAll: (xpath: string): ReadonlyArray<string> =>
        xpath === testData.path ? [testData.value] : [],
    },
  };

  it("builds a script that can be used for evaluation", () => {
    const expectTrueScript = `
var bRet = false; // 'var bRet' is used to be similar to the UI generated scripts
const results = [
    xml.contains('${testData.path}', '${testData.value}'),
    xml.get('${testData.path}') == '${testData.value}',
    xml.getAll('${testData.path}').includes('${testData.value}'),
    user.hasRole('${testData.uuid}')
];
if (results.every(v => v === true)) {
    bRet = true;
}
return bRet;
`;

    const result = pipe(
      scriptContext,
      buildVisibilityScript(expectTrueScript),
      expectRight,
    );
    expect(result).toBe(true);
  });

  it("returns a left if there's issues with the script", () => {
    const result = pipe(
      scriptContext,
      buildVisibilityScript("this is not a valid script!!"),
    );
    expect(result).toBeLeft();
  });

  it.each<[string, string, boolean]>([
    ["truthy value", "return 'a string';", true],
    ["falsy value", "return 0;", false],
    ["return nothing", "const str = 'nothing returned explicitly';", false],
  ])(
    "forces a boolean when the script does not return a boolean - %s",
    (_, script, expectedResult) => {
      const result = pipe(
        scriptContext,
        buildVisibilityScript(script),
        expectRight,
      );

      expect(result).toBe(expectedResult);
    },
  );
});
