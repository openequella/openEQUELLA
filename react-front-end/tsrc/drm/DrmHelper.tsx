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
import {
  acceptDrmTerms,
  defaultDrmStatus,
  listDrmTerms,
} from "../modules/DrmModule";
import { DrmAcceptanceDialog } from "./DrmAcceptanceDialog";
import * as OEQ from "@openequella/rest-api-client";

/**
 * Given a handler which cannot be used until a DRM check is completed,
 * this function checks DRM status and based on the result build different DRM dialogs.
 * The handler will then be used by interacting with the dialog.
 *
 * @param uuid Item's UUID.
 * @param version Item's version.
 * @param drmStatus Item's DRM status.
 * @param updateDrmStatus Function to update DRM status on client side. Typically, this is a React state setter.
 * @param closeDrmDialog Function to close the resultant DRM dialog.
 * @param drmProtectedHandler Handler that can't be used until a DRM check is completed.
 */
export const createDrmDialog = (
  uuid: string,
  version: number,
  drmStatus: OEQ.Search.DrmStatus,
  updateDrmStatus: (status: OEQ.Search.DrmStatus) => void,
  closeDrmDialog: () => void,
  drmProtectedHandler?: () => void
): JSX.Element | undefined => {
  // If there is nothing requiring DRM permission check then return undefined.
  if (drmProtectedHandler) {
    const { isAuthorised, termsAccepted } = drmStatus;
    if (isAuthorised && !termsAccepted) {
      return (
        <DrmAcceptanceDialog
          termsProvider={() => listDrmTerms(uuid, version)}
          onAccept={() => acceptDrmTerms(uuid, version)}
          onAcceptCallBack={() => {
            closeDrmDialog();
            updateDrmStatus(defaultDrmStatus);
            drmProtectedHandler();
          }}
          onReject={closeDrmDialog}
          open={!!drmProtectedHandler}
        />
      );
    } else {
      drmProtectedHandler();
      return;
    }
  }

  return;
};
