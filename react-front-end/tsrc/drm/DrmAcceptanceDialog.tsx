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
import {
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
} from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as E from "fp-ts/Either";
import { flow, pipe } from "fp-ts/function";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { useEffect, useReducer } from "react";
import {
  Action,
  drmReducer,
  skeletonDialogStructure,
} from "./DrmAcceptanceDialogReducer";

export interface DrmAcceptanceDialogProps {
  /**
   * Function to retrieve DRM terms from server.
   */
  termsProvider: () => Promise<OEQ.Drm.ItemDrmDetails>;
  /**
   * Function fired when the Accept button is clicked. Typically to do an accept API call to the server.
   */
  onAccept: () => Promise<void>;
  /**
   * Function fired after the call of 'onAccept' is successful.
   */
  onAcceptCallBack: () => void;
  /**
   * Function fired when the Reject button is clicked.
   */
  onReject: () => void;
  /**
   * `true` to open the dialog.
   */
  open: boolean;
}

/**
 * Provide a dialog to allow users to accept DRM terms. The dialog has threes states. In initialising stage, it calls the supplied
 * term provider to retrieve DRM terms and shows a skeleton dialog before terms are retrieved. Then, depending on the call result,
 * it shows a dialog listing all terms or an error dialog describing why failed.
 */
export const DrmAcceptanceDialog = ({
  termsProvider,
  onReject,
  onAccept,
  onAcceptCallBack,
  open,
}: DrmAcceptanceDialogProps) => {
  const [state, dispatch] = useReducer(drmReducer, {
    status: "initialising",
    dialog: { ...skeletonDialogStructure },
  });

  // Run the given async task in a TaskEither and return an Either where Left is Error and Right is the specified type T.
  const runInTask = <T,>(task: () => Promise<T>): Promise<E.Either<Error, T>> =>
    pipe(TE.tryCatch<Error, T>(task, (cause) => new Error(`${cause}`)))();

  useEffect(() => {
    const buildActionForError = (cause: Error): Action => ({
      type: "error",
      cause,
      onClose: onReject,
    });

    // Call 'onAccept' first, and then call 'onAcceptCallBack' or show error message.
    const acceptDrmTerms = async () => {
      pipe(
        await runInTask(onAccept),
        E.fold<Error, void, void>(
          flow(buildActionForError, dispatch),
          onAcceptCallBack
        )
      );
    };

    if (state.status === "initialising") {
      (async () => {
        pipe(
          await runInTask(termsProvider),
          E.fold<Error, OEQ.Drm.ItemDrmDetails, Action>(
            buildActionForError,
            (drmDetails) => ({
              type: "termsRetrieved",
              drmDetails,
              rejectTerms: onReject,
              acceptTerms: acceptDrmTerms,
            })
          ),
          dispatch
        );
      })();
    }
  }, [state, termsProvider, onReject, onAccept, onAcceptCallBack]);

  const { title, content, buttons } = state.dialog;

  return (
    <Dialog open={open} fullWidth>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>{content}</DialogContent>
      <DialogActions>{buttons}</DialogActions>
    </Dialog>
  );
};
