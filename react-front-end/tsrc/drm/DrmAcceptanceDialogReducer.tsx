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
import { Button, Grid, Typography } from "@mui/material";
import { Skeleton } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import { pipe } from "fp-ts/function";
import { range } from "lodash";
import * as React from "react";
import { commonString } from "../util/commonstrings";
import { languageStrings } from "../util/langstrings";
import { DrmTerms } from "./DrmTerms";
import { ButtonProps } from "@mui/material";

const { reject: rejectString, accept: acceptString } =
  languageStrings.common.action;

// Dialog structure for the Skeleton dialog.
export const skeletonDialogStructure = {
  title: <Skeleton variant="text" width={80} />,
  content: <Skeleton variant="rectangular" width="100%" height={200} />,
  buttons: range(2).map((index) => (
    <Button key={index}>
      <Skeleton variant="text" width={80} animation={false} />
    </Button>
  )),
};

export type Action =
  | { type: "init" }
  | {
      type: "termsRetrieved";
      drmDetails: OEQ.Drm.ItemDrmDetails;
      acceptTerms: () => void; // Handler for accepting terms which consists of 'onAccept', 'onAcceptCallBack' and error handling.
      rejectTerms: () => void; // Handler for rejecting terms which is just an alias for 'onReject'.
    }
  | { type: "error"; cause: Error; onClose: () => void };

export type State =
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

export const drmReducer = (state: State, action: Action): State => {
  const buildButtons = (
    buttons: {
      handler: () => void;
      color: ButtonProps["color"];
      text: string;
      autoFocus: boolean;
    }[],
  ): JSX.Element[] =>
    buttons.map(({ handler, color, text, autoFocus }) => (
      <Button
        key={text}
        onClick={(event) => {
          handler();
          event.stopPropagation();
        }}
        color={color}
        autoFocus={autoFocus}
      >
        {text}
      </Button>
    ));

  switch (action.type) {
    case "init":
      return { status: "initialising", dialog: { ...skeletonDialogStructure } };
    case "termsRetrieved":
      return pipe(
        action,
        ({
          acceptTerms,
          rejectTerms,
          drmDetails: { title, subtitle, agreements },
        }) => ({
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
            buttons: buildButtons([
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
            ]),
          },
        }),
      );
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
