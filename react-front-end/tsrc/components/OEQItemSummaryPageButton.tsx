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
import { PropTypes } from "@material-ui/core";
import InfoIcon from "@material-ui/icons/Info";
import * as React from "react";
import { useHistory } from "react-router";
import { routes } from "../mainui/routes";
import {
  buildSelectionSessionItemSummaryLink,
  isSelectionSessionOpen,
} from "../modules/LegacySelectionSessionModule";
import { TooltipIconButton } from "./TooltipIconButton";
import * as OEQ from "@openequella/rest-api-client";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";

export interface OEQItemSummaryPageButtonProps {
  /**
   * Title of the button.
   */
  title: string;
  /**
   * Item whose Summary page will be open by clicking the button.
   */
  item: {
    /**
     * UUID of the Item.
     */
    uuid: string;
    /**
     * Version of the Item.
     */
    version: number;
    /**
     * DRM related to help handle DRM acceptance.
     */
    drm?: {
      /**
       * Item's DRM status.
       */
      drmStatus: OEQ.Search.DrmStatus;
      /**
       * Function to update the callback called after DRM terms are accepted.
       */
      setOnDrmAcceptCallback: (_: (() => void) | undefined) => void;
    };
  };
  /**
   * Color to be used for the button.
   */
  color?: PropTypes.Color;
}

/**
 * Provide a dedicated button for opening Item Summary page. In the context of Selection Session New Search UI,
 * there are no routes matching Item Summary page so we use `window.open`. In normal pages, we use `history.push`.
 */
export const OEQItemSummaryPageButton = ({
  title,
  item: { uuid, version, drm },
  color = "default",
}: OEQItemSummaryPageButtonProps) => {
  const history = useHistory();
  const buildOpenSummaryPageHandler = () => () => {
    isSelectionSessionOpen()
      ? window.open(
          buildSelectionSessionItemSummaryLink(uuid, version),
          "_self"
        )
      : history.push(routes.ViewItem.to(uuid, version));
  };

  const onClick = pipe(
    drm,
    O.fromNullable,
    O.chain(
      ({
        drmStatus: { isAuthorised, termsAccepted },
        setOnDrmAcceptCallback,
      }) =>
        isAuthorised && !termsAccepted
          ? O.some(() => setOnDrmAcceptCallback(buildOpenSummaryPageHandler))
          : O.none
    ),
    O.getOrElse(buildOpenSummaryPageHandler)
  );

  return (
    <TooltipIconButton
      color={color}
      title={title}
      onClick={(event) => {
        event.stopPropagation();
        onClick();
      }}
    >
      <InfoIcon />
    </TooltipIconButton>
  );
};
