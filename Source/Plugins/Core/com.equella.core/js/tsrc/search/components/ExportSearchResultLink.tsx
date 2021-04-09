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
import { Link, Tooltip } from "@material-ui/core";
import DoneIcon from "@material-ui/icons/Done";
import GetAppIcon from "@material-ui/icons/GetApp";
import * as React from "react";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { languageStrings } from "../../util/langstrings";

export interface ExportSearchResultLinkProps {
  /**
   * The full URL to download a search result
   */
  url: string;
  /**
   * Handler fired to check an export. Return `false` to indicate an export is invalid.
   */
  onExport: () => boolean;
  /**
   * `true` if an export is not allowed.
   */
  exportDisabled: boolean;
}

const exportStrings = languageStrings.searchpage.export;

/**
 * Build a Download icon button wrapped by a link to export a search result,
 * or a Tick icon to indicate the search result is downloaded already.
 */
export const ExportSearchResultLink = ({
  url,
  onExport,
  exportDisabled,
}: ExportSearchResultLinkProps) => {
  // Prevent the link from following the URL if an export is invalid.
  const onClick = (e: React.MouseEvent<HTMLAnchorElement>) => {
    if (!onExport()) {
      e.preventDefault();
      return false;
    }
    return true;
  };

  return exportDisabled ? (
    // Just need an Icon instead of an Icon button.
    <Tooltip title={exportStrings.exportCompleted}>
      <DoneIcon color="secondary" />
    </Tooltip>
  ) : (
    <Link download href={url} onClick={onClick}>
      <TooltipIconButton title={exportStrings.title}>
        <GetAppIcon />
      </TooltipIconButton>
    </Link>
  );
};
