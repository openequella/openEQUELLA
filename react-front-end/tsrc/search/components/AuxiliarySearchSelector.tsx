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
import { FormControl, InputLabel, MenuItem, Select } from "@mui/material";
import { styled } from "@mui/material/styles";
import LinkIcon from "@mui/icons-material/Link";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { useEffect, useState } from "react";
import { useHistory } from "react-router";
import { isSelectionSessionOpen } from "../../modules/LegacySelectionSessionModule";

const PREFIX = "AuxiliarySearchSelector";

const classes = {
  linkIcon: `${PREFIX}-linkIcon`,
};

const StyledFormControl = styled(FormControl)(({ theme }) => ({
  [`& .${classes.linkIcon}`]: {
    marginRight: theme.spacing(2),
  },
}));

export interface AuxiliarySearchSelectorProps {
  /**
   * Label for describing what can be selected from this component.
   */
  label: string;
  /**
   * Function to provide a list of auxiliary searches
   */
  auxiliarySearchesSupplier: () => Promise<OEQ.Common.BaseEntitySummary[]>;

  /**
   * Function to produce the URL for React Route Link using the UUID returned in the
   * `OEQ.Common.BaseEntitySummary` from the `auxiliarySearchesSupplier` function.
   */
  urlGeneratorForRouteLink: (uuid: string) => string;
  /**
   * Function to produce the URL for MUI Link using the UUID returned in the
   * `OEQ.Common.BaseEntitySummary` from the `auxiliarySearchesSupplier` function.
   * Typically, the MUI Link is used in Selection Session.
   */
  urlGeneratorForMuiLink: (uuid: string) => string;
}
/**
 * A search 'selector' control to provide a list of auxiliary searches (e.g. Remote search and advanced search).
 * Clicking on an auxiliary search (currently) navigates you to the legacy UI page.
 */
export const AuxiliarySearchSelector = ({
  label,
  auxiliarySearchesSupplier,
  urlGeneratorForRouteLink,
  urlGeneratorForMuiLink,
}: AuxiliarySearchSelectorProps) => {
  const history = useHistory();
  // Replace all the spaces with hyphens to create a unique ID for the label.
  const labelId = `${PREFIX}-${label.replace(/\s+/g, "-")}`;

  const [auxiliarySearches, setAuxiliarySearches] = useState<
    OEQ.Common.BaseEntitySummary[]
  >([]);

  useEffect(() => {
    auxiliarySearchesSupplier().then((searches) =>
      setAuxiliarySearches(searches),
    );
  }, [auxiliarySearchesSupplier]);

  const buildSearchMenuItems = () =>
    auxiliarySearches.map((summary) => (
      // There is a known accessibility issue (https://github.com/mui/material-ui/issues/33268)
      // where MUI MenuItem does not work properly with keyboard navigation as a Link.
      <MenuItem
        key={summary.uuid}
        onClick={() => {
          isSelectionSessionOpen()
            ? window.open(urlGeneratorForMuiLink(summary.uuid), "_self")
            : history.push(urlGeneratorForRouteLink(summary.uuid));
        }}
      >
        <LinkIcon className={classes.linkIcon} />
        {summary.name}
      </MenuItem>
    ));

  return (
    <StyledFormControl variant="outlined" fullWidth>
      <InputLabel id={labelId}>{label}</InputLabel>
      <Select value="" labelId={labelId} label={label}>
        {buildSearchMenuItems()}
      </Select>
    </StyledFormControl>
  );
};
