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
import * as OEQ from "@openequella/rest-api-client";
import { mockedFieldValueMap } from "../../../__mocks__/WizardHelper.mock";
import type {
  ControlTarget,
  FieldValueMap,
  PathValueMap,
} from "../../../tsrc/components/wizard/WizardHelper";
import {
  buildFieldValueMapFromPathValueMap,
  generateAdvancedSearchCriteria,
} from "../../../tsrc/search/AdvancedSearchHelper";

describe("generateAdvancedSearchCriteria()", () => {
  it("builds SearchAdditionalParams for Wizard controls", () => {
    const query = generateAdvancedSearchCriteria(mockedFieldValueMap);

    const expectedCriteria: OEQ.Search.WizardControlFieldValue[] = [
      {
        queryType: "DateRange",
        schemaNodes: ["/controls/calendar1"],
        values: ["2021-11-01", ""],
      },
      {
        queryType: "DateRange",
        schemaNodes: ["/controls/calendar2"],
        values: ["", "2021-11-01"],
      },
      {
        queryType: "DateRange",
        schemaNodes: ["/controls/calendar3"],
        values: ["2021-11-01", "2021-11-11"],
      },
      {
        queryType: "Phrase",
        schemaNodes: ["/controls/checkbox"],
        values: ["optionA", "optionB"],
      },
      {
        queryType: "Tokenised",
        schemaNodes: ["/controls/editbox"],
        values: ["hello world"],
      },
      {
        queryType: "Phrase",
        schemaNodes: ["/controls/listbox"],
        values: ["optionC"],
      },
      {
        queryType: "Phrase",
        schemaNodes: ["/controls/radiogroup"],
        values: ["optionD"],
      },
      {
        queryType: "Phrase",
        schemaNodes: ["/controls/shufflebox"],
        values: ["optionE", "optionF"],
      },
      {
        queryType: "Tokenised",
        schemaNodes: ["/controls/shufflelist"],
        values: ["The house is nice", "walking"],
      },
      {
        queryType: "Phrase",
        schemaNodes: ["/controls/termselector"],
        values: ["programming"],
      },
      {
        queryType: "Phrase",
        schemaNodes: ["/controls/userselector"],
        values: ["admin"],
      },
    ];

    expect(query).toStrictEqual(expectedCriteria);
  });
});

describe("buildFieldValueMapFromPathValueMap", () => {
  it("updates the values of FieldValueMap by pulling the values of PathValueMap", () => {
    const path = "/item/name";
    const controlTarget: ControlTarget = {
      schemaNode: [path],
      type: "editbox",
    };
    const fieldValueMap: FieldValueMap = new Map([[controlTarget, []]]);
    const pathValueMap: PathValueMap = new Map([[path, ["hello"]]]);
    const updatedMap = buildFieldValueMapFromPathValueMap(
      pathValueMap,
      fieldValueMap,
    );

    expect(updatedMap).toStrictEqual(new Map([[controlTarget, ["hello"]]]));
  });
});
