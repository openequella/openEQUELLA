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
import * as OEQ from "@openequella/rest-api-client";
import { render, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import { privateSearchPortlet } from "../../../__mocks__/Dashboard.mock";
import { LegacyPortlet } from "../../../tsrc/dashboard/portlet/LegacyPortlet";
import type { PortletBasicProps } from "../../../tsrc/dashboard/portlet/PortletHelper";
import type { JQueryDivProps } from "../../../tsrc/legacycontent/JQueryDiv";
import { legacyFormId } from "../../../tsrc/legacycontent/LegacyForm";
import * as LegacyContentModule from "../../../tsrc/modules/LegacyContentModule";
import * as LegacyPortletHelper from "../../../tsrc/dashboard/portlet/LegacyPortletHelper";

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
      <div {...otherProps} dangerouslySetInnerHTML={{ __html: html }} />
    ),
);

jest.spyOn(LegacyContentModule, "updateStylesheets").mockResolvedValue({});

jest
  .spyOn(LegacyContentModule, "resolveUrl")
  .mockImplementation((url: string) => url);

const mockSubmitResponse = jest.spyOn(LegacyContentModule, "submitRequest");
mockSubmitResponse.mockResolvedValue(legacyContentResponse);

describe("<LegacyPortlet />", () => {
  const portletUuid = "01f33f25-f3e3-4bb3-898f-2fa2410273f5";

  const cfg: OEQ.Dashboard.BasicPortlet = {
    ...privateSearchPortlet,
    portletType: "freemarker",
  };

  const props: PortletBasicProps = { cfg, position: { column: 0, order: 0 } };

  const renderLegacyPortlet = async () => {
    const result = render(<LegacyPortlet {...props} />);

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
    const mockUpdateExtraFiles = jest
      .spyOn(LegacyPortletHelper, "updateExtraFiles")
      .mockResolvedValue();
    const jsFiles = ["file1.js", "file2.js"];
    const cssFiles = ["file1.css"];
    mockSubmitResponse.mockResolvedValueOnce({
      ...legacyContentResponse,
      js: jsFiles,
      css: cssFiles,
    });

    await renderLegacyPortlet();

    expect(mockUpdateExtraFiles).toHaveBeenCalledTimes(1);
    expect(mockUpdateExtraFiles).toHaveBeenCalledWith(jsFiles, cssFiles);
  });

  describe("Error handling", () => {
    const generalErrorMsg = `An error occurred while displaying portlet ${portletUuid}`;
    const legacyButtonText = "legacy button";

    const mockConsole = jest
      .spyOn(console, "error")
      .mockImplementation(() => {});

    it("displays an alert if the legacy content request fails", async () => {
      mockConsole.mockClear();
      const contentError = "No Section Tree for this portlet";
      jest
        .spyOn(LegacyPortletHelper, "getPortletLegacyContent")
        .mockRejectedValueOnce(contentError);

      const { getByText } = render(<LegacyPortlet {...props} />);
      await waitFor(() => {
        expect(getByText(generalErrorMsg)).toBeInTheDocument();
        expect(console.error).toHaveBeenLastCalledWith(
          `${generalErrorMsg}: ${contentError}`,
        );
      });
    });

    it("displays an alert if an error occurs when using a legacy function defined in object 'EQ'", async () => {
      mockConsole.mockClear();
      // Firstly, mock the content response to include a button that uses the legacy EQ event.
      const event = "ppsp.doSearch";
      const legacyContent = `<button onclick="window['EQ-${portletUuid}'].event('${event}');return false;" >${legacyButtonText}</button>`;
      mockSubmitResponse.mockResolvedValueOnce({
        ...legacyContentResponse,
        html: { body: legacyContent },
      });

      // Secondly, get the button and mock the event handler to throw an error.
      const { getByText, findByText } = render(<LegacyPortlet {...props} />);
      const legacyBtn = await findByText(legacyButtonText);
      const eventError = "No Section event handler registered for this portlet";
      mockSubmitResponse.mockRejectedValueOnce(eventError);

      // Now, click the button and check the alert.
      await userEvent.click(legacyBtn);
      expect(getByText(generalErrorMsg)).toBeInTheDocument();
      expect(console.error).toHaveBeenLastCalledWith(
        generalErrorMsg,
        { event__: [event] },
        eventError,
      );
    });

    it("displays an alert if any other unknown error occurs", async () => {
      mockConsole.mockClear();
      // Firstly, mock the content response to include a button that uses a legacy JS function which is unavailable at runtime.
      const legacyContent = `<button onclick="legacyJS();">${legacyButtonText}</button>`;
      mockSubmitResponse.mockResolvedValueOnce({
        ...legacyContentResponse,
        html: { body: legacyContent },
      });

      // Secondly, get the button and click it
      const { getByText, findByText } = render(<LegacyPortlet {...props} />);
      const legacyBtn = await findByText(legacyButtonText);
      await userEvent.click(legacyBtn);

      // Check if the alert is displayed.
      expect(getByText(generalErrorMsg)).toBeInTheDocument();
      // Not using toHaveBeenLastCalledWith as there is one extra console.error triggered by RTL.
      expect(console.error).toHaveBeenCalledWith(
        generalErrorMsg,
        "legacyJS is not defined",
      );
    });
  });
});
