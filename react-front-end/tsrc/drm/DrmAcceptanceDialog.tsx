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
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  PropTypes,
  Theme,
  Typography,
} from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import { Skeleton } from "@material-ui/lab";
import * as E from "fp-ts/Either";
import * as TE from "fp-ts/TaskEither";
import { range } from "lodash";
import { useEffect, useReducer } from "react";
import * as React from "react";
import * as OEQ from "@openequella/rest-api-client";
import { flow, pipe } from "fp-ts/function";
import { commonString } from "../util/commonstrings";
import { languageStrings } from "../util/langstrings";
import { pfTernaryTypeGuard } from "../util/pointfree";

const useStyles = makeStyles((theme: Theme) => ({
  li: {
    marginTop: theme.spacing(1),
    marginBottom: theme.spacing(1),
  },
}));

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

// Child component for displaying non-standard DRM term which usually has a title and a list of terms.
const NonStandardDrmTerms = ({
  title,
  terms,
}: {
  title: string;
  terms: string[];
}) => {
  const classes = useStyles();
  return (
    <li className={classes.li} key={title}>
      <Grid container direction="column">
        <Grid item>
          <Typography>{title}</Typography>
        </Grid>
        <Grid item>
          {terms.map((term) => (
            <Typography key={term}>{term}</Typography>
          ))}
        </Grid>
      </Grid>
    </li>
  );
};

// Child component for displaying all DRM terms.
const DrmTerms = ({
  regularPermission,
  additionalPermission,
  educationSector,
  parties,
  customTerms,
}: OEQ.Drm.DrmAgreements) => {
  const classes = useStyles();
  const standardTerms = [
    regularPermission,
    additionalPermission,
    educationSector,
  ]
    .filter((term) => term !== undefined)
    .map((term) => (
      <li className={classes.li} key={term}>
        <Typography>{term}</Typography>
      </li>
    ));

  const partyDetails = pipe(
    parties,
    pfTernaryTypeGuard(
      (
        parties: OEQ.Drm.DrmParties | undefined
      ): parties is OEQ.Drm.DrmParties => parties !== undefined,
      ({ title, partyList }) => (
        <NonStandardDrmTerms title={title} terms={partyList} />
      ),
      () => undefined
    )
  );

  const customTermDetails = pipe(
    customTerms,
    pfTernaryTypeGuard(
      (
        terms: OEQ.Drm.DrmCustomTerms | undefined
      ): terms is OEQ.Drm.DrmCustomTerms => terms !== undefined,
      ({ title, terms }) => (
        <NonStandardDrmTerms title={title} terms={terms.split("\n")} />
      ),
      () => undefined
    )
  );

  return (
    <ol start={1}>
      {standardTerms}
      {partyDetails}
      {customTermDetails}
    </ol>
  );
};

// Props for building the Skeleton dialog.
const skeletonDialogStructure = {
  title: <Skeleton variant="text" width={80} />,
  content: <Skeleton variant="rect" width="100%" height={200} />,
  buttons: range(2).map((index) => (
    <Button key={index}>
      <Skeleton variant="text" width={80} animation={false} />
    </Button>
  )),
};

const { reject: rejectString, accept: acceptString } =
  languageStrings.common.action;

type Action =
  | { type: "init" }
  | {
      type: "termsRetrieved";
      drmDetails: OEQ.Drm.ItemDrmDetails;
      acceptTerms: () => void; // Handler for accepting terms which consists of 'onAccept', 'onAcceptCallBack' and error handling.
      rejectTerms: () => void; // Handler for rejecting terms which is just an alias for 'onReject'.
    }
  | { type: "error"; cause: Error; onClose: () => void };

type State =
  | {
      status: "initialising";
      dialog: {
        title: JSX.Element;
        content: JSX.Element;
        buttons: JSX.Element[];
      };
    }
  | {
      status: "successful";
      dialog: {
        title: string;
        content: JSX.Element;
        buttons: JSX.Element[];
      };
    }
  | {
      status: "failed";
      dialog: {
        title: string;
        content: JSX.Element;
        buttons: JSX.Element;
      };
    };

const reducer = (state: State, action: Action): State => {
  switch (action.type) {
    case "init":
      return { status: "initialising", dialog: { ...skeletonDialogStructure } };
    case "termsRetrieved":
      const {
        acceptTerms,
        rejectTerms,
        drmDetails: { title, subtitle, agreements },
      } = action;
      return {
        status: "successful",
        dialog: {
          title,
          content: (
            <Grid container>
              <Grid item>
                <Typography variant="subtitle1">{subtitle}</Typography>
              </Grid>
              <Grid item>
                <DrmTerms {...agreements} />
              </Grid>
            </Grid>
          ),
          buttons: [
            {
              handler: rejectTerms,
              color: "secondary",
              text: rejectString,
              autoFocus: false,
            },
            {
              handler: acceptTerms,
              color: "primary",
              text: acceptString,
              autoFocus: true,
            },
          ].map(({ handler, color, text, autoFocus }) => (
            <Button
              key={text}
              onClick={(event) => {
                handler();
                event.stopPropagation();
              }}
              color={color as PropTypes.Color}
              autoFocus={autoFocus}
            >
              {text}
            </Button>
          )),
        },
      };
    case "error":
      return {
        status: "failed",
        dialog: {
          title: languageStrings.drm.getTerms.error,
          content: <Typography>{action.cause.message}</Typography>,
          buttons: (
            <Button onClick={action.onClose} color="primary">
              {commonString.action.ok}
            </Button>
          ),
        },
      };
    default:
      throw new TypeError("Unexpected action passed to reducer!");
  }
};

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
  const [state, dispatch] = useReducer(reducer, {
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
