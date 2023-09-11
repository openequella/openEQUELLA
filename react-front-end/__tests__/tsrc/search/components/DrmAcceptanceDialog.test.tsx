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
import { queryByText, render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { drmTerms, drmTermsResolved } from "../../../../__mocks__/Drm.mock";
import { DrmAcceptanceDialog } from "../../../../tsrc/drm/DrmAcceptanceDialog";
import * as OEQ from "@openequella/rest-api-client";

describe("<DrmAcceptanceDialog />", () => {
  const renderDrmDialog = (
    termsProvider: () => Promise<OEQ.Drm.ItemDrmDetails>
  ) =>
    render(
      <DrmAcceptanceDialog
        termsProvider={termsProvider}
        onAccept={jest.fn()}
        onAcceptCallBack={jest.fn()}
        onReject={jest.fn()}
        open
      />
    );

  it("shows a list of DRM terms", async () => {
    renderDrmDialog(drmTermsResolved);
    await waitFor(() =>
      expect(
        queryByText(screen.getByRole("dialog"), drmTerms.title)
      ).toBeInTheDocument()
    );
  });

  it("shows an error message when failed to retrieve DRM terms", async () => {
    const error = "network error";
    renderDrmDialog(() => Promise.reject(error));

    await waitFor(() =>
      expect(queryByText(screen.getByRole("dialog"), error)).toBeInTheDocument()
    );
  });
});
