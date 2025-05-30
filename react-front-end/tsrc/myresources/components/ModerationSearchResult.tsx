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
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import * as O from "fp-ts/Option";
import { pipe } from "fp-ts/function";
import { useState } from "react";
import * as React from "react";
import { useHistory } from "react-router";
import { Date as DateDisplay } from "../../components/Date";
import MessageDialog from "../../components/MessageDialog";
import { OEQLink } from "../../components/OEQLink";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { buildOpenSummaryPageHandler } from "../../search/SearchPageHelper";
import { languageStrings } from "../../util/langstrings";
import MessageIcon from "@mui/icons-material/Message";

const {
  ariaLabel,
  colTitle,
  colStatus,
  colSubmittedDate,
  colLastActionDate,
  rejectionCommentButton,
  rejectionCommentDialogTitle,
} = languageStrings.myResources.moderationItemTable;

/**
 * In this component we only want items which have moderation details, this type provides
 * a way to express this in type narrowing, refinements, etc.
 */
type SearchResultItemWithModerationDetails = OEQ.Search.SearchResultItem &
  Required<Pick<OEQ.Search.SearchResultItem, "moderationDetails">>;

export interface ModerationSearchResultProps {
  /**
   * The search result with items to be rendered in the component.
   */
  result: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>;
}

/**
 * Displays a collection of items returned from a Moderation search in a table with each item
 * title being a link to the item summary page.
 */
export const ModerationSearchResult = ({
  result,
}: ModerationSearchResultProps) => {
  const history = useHistory();
  const [rejectionMessage, setRejectionMessage] = useState<string>();

  const buildTitle = ({
    uuid,
    version,
    name,
  }: OEQ.Search.SearchResultItem): React.JSX.Element =>
    pipe(
      buildOpenSummaryPageHandler(uuid, version, history),
      ({ url, onClick }) => (
        <OEQLink
          routeLinkUrlProvider={() => url}
          muiLinkUrlProvider={() => url}
          onClick={(e: React.MouseEvent<HTMLAnchorElement>) => {
            e.preventDefault();
            onClick();
          }}
        >
          {name ?? uuid}
        </OEQLink>
      ),
    );

  const buildStatus = ({
    status,
    moderationDetails: { rejectionMessage },
  }: SearchResultItemWithModerationDetails): React.JSX.Element => (
    <>
      {
        // Uppercase the status to match the values in the status selector
        status.toUpperCase()
      }
      {pipe(
        rejectionMessage,
        O.fromNullable,
        O.map((msg) => (
          <>
            &nbsp;
            <TooltipIconButton
              title={rejectionCommentButton}
              onClick={() => setRejectionMessage(msg)}
              size="small"
            >
              <MessageIcon />
            </TooltipIconButton>
          </>
        )),
        O.toUndefined,
      )}
    </>
  );

  const rows = pipe(
    result.results,
    A.filter(
      (r): r is SearchResultItemWithModerationDetails =>
        r.moderationDetails !== undefined,
    ),
    A.map((r) => ({
      key: `${r.uuid}/${r.version}`,
      title: buildTitle(r),
      status: buildStatus(r),
      submitted: (
        <DateDisplay displayRelative date={r.moderationDetails.submittedDate} />
      ),
      lastAction: (
        <DateDisplay
          displayRelative
          date={r.moderationDetails.lastActionDate}
        />
      ),
    })),
  );

  return (
    <>
      <TableContainer>
        <Table aria-label={ariaLabel}>
          <TableHead>
            <TableRow>
              <TableCell>{colTitle}</TableCell>
              <TableCell>{colStatus}</TableCell>
              <TableCell>{colSubmittedDate}</TableCell>
              <TableCell>{colLastActionDate}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {pipe(
              rows,
              A.map(({ key, title, status, submitted, lastAction }) => (
                <TableRow key={key}>
                  <TableCell>{title}</TableCell>
                  <TableCell>{status}</TableCell>
                  <TableCell>{submitted}</TableCell>
                  <TableCell>{lastAction}</TableCell>
                </TableRow>
              )),
            )}
          </TableBody>
        </Table>
      </TableContainer>
      <MessageDialog
        open={rejectionMessage !== undefined}
        title={rejectionCommentDialogTitle}
        messages={rejectionMessage ? [rejectionMessage] : []}
        close={() => setRejectionMessage(undefined)}
      />
    </>
  );
};
