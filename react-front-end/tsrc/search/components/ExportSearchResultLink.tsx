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
import { Tooltip } from "@mui/material";
import DoneIcon from "@mui/icons-material/Done";
import GetAppIcon from "@mui/icons-material/GetApp";
import * as React from "react";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { languageStrings } from "../../util/langstrings";

export interface ExportSearchResultLinkProps {
  /**
   * The full URL to download a search result
   */
  url: string;
  /**
   *  Handler for exporting search result.
   */
  onExport: () => void;
  /**
   * `true` to show a complete indicator and disable additional clicking.
   */
  alreadyExported: boolean;
  /**
   * Ref of HTMLAnchorElement to be used in the hidden link for downloading search result.
   */
  linkRef: React.Ref<HTMLAnchorElement>;
}

const exportStrings = languageStrings.searchpage.export;

/**
 * Build a Download icon button wrapped by a link to export a search result,
 * or a Tick icon to indicate the search result is downloaded already.
 */
export const ExportSearchResultLink = ({
  alreadyExported,
  url,
  onExport,
  linkRef,
}: ExportSearchResultLinkProps) => {
  return alreadyExported ? (
    // Just need an Icon instead of an Icon button.
    <Tooltip title={exportStrings.exportCompleted}>
      <DoneIcon color="secondary" id="exportCompleted" />
    </Tooltip>
  ) : (
    <>
      <TooltipIconButton
        title={exportStrings.title}
        onClick={onExport}
        id="exportSearchResult"
      >
        <GetAppIcon />
      </TooltipIconButton>
      {/* eslint-disable-next-line jsx-a11y/anchor-has-content */}
      <a hidden download href={url} ref={linkRef} />
    </>
  );
};
