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
import userEvent from "@testing-library/user-event";
import SearchSettingFormControl from "../../../tsrc/components/SearchSettingFormControl";
import { TextField } from "@material-ui/core";
import * as React from "react";
import { render, RenderResult } from "@testing-library/react";

describe("SearchSettingFormControl.tsx", () => {
  const onChange = jest.fn();
  let page: RenderResult;
  beforeEach(() => {
    page = render(
      <SearchSettingFormControl
        control={
          <TextField id="testTextField" value="test" onChange={onChange} />
        }
        onChange={onChange}
      />
    );
  });

  it("should not be triggered before call", () => {
    expect(onChange).not.toHaveBeenCalled();
  });

  it("should be triggered after call", () => {
    const textField = page.getByRole("textbox");
    userEvent.type(textField, "text");
    expect(onChange.mock.calls).toHaveLength(4);
  });
});
