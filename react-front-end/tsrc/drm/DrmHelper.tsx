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
import * as TE from "fp-ts/TaskEither";
import * as T from "fp-ts/Task";
import * as React from "react";
import MessageDialog from "../components/MessageDialog";
import {
  acceptDrmTerms,
  defaultDrmStatus,
  listDrmTerms,
  listDrmViolations,
} from "../modules/DrmModule";
import { languageStrings } from "../util/langstrings";
import { DrmAcceptanceDialog } from "./DrmAcceptanceDialog";
import * as OEQ from "@openequella/rest-api-client";

const { title: violationTitle, prefix: violationPrefix } =
  languageStrings.drm.violation;
/**
 * Given a handler which cannot be used until a DRM check is completed,
 * this function checks DRM status and based on the result builds different DRM dialogs.
 * Depending on which dialog is triggered and which button the user clicks, the handler will then be called.
 *
 * Examples:
 * 1. The DRM check says the user needs to accept terms, then the accept terms dialog will be shown and if the user
 *    accepts, then the handler will be called;
 * 2. The DRM check says the user is _not_ authorised, then a dialog will be shown and after which the handler will
 *    never be called; (TODO: Coming soon.)
 * 3. DRM checks are all in order, no dialog is displayed and the handler is called immediately.
 *
 * @param uuid Item's UUID.
 * @param version Item's version.
 * @param drmStatus Item's DRM status which defaults to authorised and terms accepted.
 * @param updateDrmStatus Function to update DRM status on client side. Typically, this is a React state setter.
 * @param closeDrmDialog Function to close the resultant DRM dialog.
 * @param drmProtectedHandler Handler that can't be used until a DRM check is completed.
 */
export const createDrmDialog = async (
  uuid: string,
  version: number,
  drmStatus: OEQ.Search.DrmStatus = defaultDrmStatus,
  updateDrmStatus: (status: OEQ.Search.DrmStatus) => void,
  closeDrmDialog: () => void,
  drmProtectedHandler: () => void,
): Promise<React.JSX.Element | undefined> => {
  const { isAuthorised, termsAccepted } = drmStatus;
  if (isAuthorised) {
    if (termsAccepted) {
      drmProtectedHandler();
      return;
    } else {
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
    }
  } else {
    const violation = await listDrmViolationsInTask(uuid, version);
    return (
      <MessageDialog
        open
        title={violationTitle}
        messages={[violationPrefix, violation]}
        close={closeDrmDialog}
      />
    );
  }
};

// Call the API to list DRM violations in a TaskEither where Left is why fail to
// list violations and Right is the retrieved violation.
const listDrmViolationsInTask = (
  uuid: string,
  version: number,
): Promise<string> =>
  pipe(
    TE.tryCatch<Error, OEQ.Drm.DrmViolation>(
      () => listDrmViolations(uuid, version),
      (error) => new Error(`${error}`),
    ),
    TE.map<OEQ.Drm.DrmViolation, string>(({ violation }) => violation),
    TE.getOrElse((error) =>
      T.of(`Failed to list DRM violations due to ${error.message}`),
    ),
  )();
