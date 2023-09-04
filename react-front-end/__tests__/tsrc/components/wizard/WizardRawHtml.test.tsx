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
import * as React from "react";
import "@testing-library/jest-dom";
import { render } from "@testing-library/react";
import type { FieldValueMap } from "../../../../tsrc/components/wizard/WizardHelper";
import { WizardRawHtml } from "../../../../tsrc/components/wizard/WizardRawHtml";

describe("<WizardRawHtml/>", () => {
  it("supports displaying metadata", () => {
    const xpath = "/item/name";
    const mockedMap: FieldValueMap = new Map([
      [
        {
          schemaNode: [xpath],
          type: "editbox",
        },
        ["test"],
      ],
    ]);
    const { queryByText } = render(
      <WizardRawHtml
        mandatory={false}
        fieldValueMap={mockedMap}
        description={`name: {${xpath}}`}
      />
    );

    expect(queryByText("name: test")).toBeInTheDocument();
  });
});
