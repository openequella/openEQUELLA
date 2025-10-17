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
import "@testing-library/jest-dom";
import { render, waitFor } from "@testing-library/react";
import * as React from "react";
import { LegacyPortlet } from "../../../tsrc/dashboard/portlet/LegacyPortlet";
import type { JQueryDivProps } from "../../../tsrc/legacycontent/JQueryDiv";
import { legacyFormId } from "../../../tsrc/legacycontent/LegacyForm";
import * as LegacyContentModule from "../../../tsrc/modules/LegacyContentModule";

const portletContent = "This is a testing portlet";
const legacyContentResponse: LegacyContentModule.LegacyContentResponse = {
  fullscreenMode: "",
  hideAppBar: false,
  html: { body: portletContent },
  js: [],
  menuMode: "",
  metaTags: "",
  noForm: false,
  preventUnload: false,
  script: "",
  state: {} as LegacyContentModule.StateData,
  title: "",
  userUpdated: false,
};

jest.mock(
  "../../../tsrc/legacycontent/JQueryDiv",
  () =>
    ({ html, ...otherProps }: JQueryDivProps) => (
      <div {...otherProps}>{html}</div>
    ),
);

const mockUpdateIncludes = jest
  .spyOn(LegacyContentModule, "updateIncludes")
  .mockResolvedValue({});

const mockSubmitResponse = jest.spyOn(LegacyContentModule, "submitRequest");
mockSubmitResponse.mockResolvedValue(legacyContentResponse);

describe("<LegacyPortlet />", () => {
  const portletUuid = "01f33f25-f3e3-4bb3-898f-2fa2410273f5";
  const renderLegacyPortlet = async () => {
    const result = render(<LegacyPortlet portletId={portletUuid} />);

    await waitFor(() =>
      expect(result.getByText(portletContent)).toBeInTheDocument(),
    );
    return result;
  };

  it("prepares a unique form for the portlet", async () => {
    const { container } = await renderLegacyPortlet();
    const form = container.querySelector(`#${legacyFormId}-${portletUuid}`);
    expect(form).toBeInTheDocument();
  });

  it("defines legacy functions under object 'EQ-{portletId}'", () => {
    renderLegacyPortlet();

    const eq = window[`EQ-${portletUuid}`];
    expect(eq).toBeDefined();
    expect(eq.event).toBeDefined();
    expect(eq.postAjax).toBeDefined();
    expect(eq.updateForm).toBeDefined();
    expect(eq.updateIncludes).toBeDefined();
  });

  it("loads additional JS and CSS files after portlet content is retrieved", async () => {
    mockUpdateIncludes.mockClear();
    const jsFiles = ["file1.js", "file2.js"];
    const cssFiles = ["file1.css"];
    mockSubmitResponse.mockResolvedValueOnce({
      ...legacyContentResponse,
      js: jsFiles,
      css: cssFiles,
    });

    await renderLegacyPortlet();

    expect(mockUpdateIncludes).toHaveBeenCalledTimes(1);
    expect(LegacyContentModule.updateIncludes).toHaveBeenCalledWith(
      jsFiles,
      cssFiles,
    );
  });
});
