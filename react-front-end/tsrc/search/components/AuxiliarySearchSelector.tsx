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
import { FormControl, MenuItem, Select, Theme } from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import LinkIcon from "@material-ui/icons/Link";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { useEffect, useState } from "react";
import { OeqLink } from "../../components/OeqLink";

const useStyles = makeStyles((theme: Theme) => ({
  linkIcon: {
    marginRight: theme.spacing(2),
  },
}));

export interface AuxiliarySearchSelectorProps {
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
  auxiliarySearchesSupplier,
  urlGeneratorForRouteLink,
  urlGeneratorForMuiLink,
}: AuxiliarySearchSelectorProps) => {
  const classes = useStyles();
  const [auxiliarySearches, setAuxiliarySearches] = useState<
    OEQ.Common.BaseEntitySummary[]
  >([]);

  useEffect(() => {
    auxiliarySearchesSupplier().then((searches) =>
      setAuxiliarySearches(searches)
    );
  }, [auxiliarySearchesSupplier]);

  const getLinkContent = (summary: OEQ.Common.BaseEntitySummary) => (
    <MenuItem value={summary.uuid}>
      <LinkIcon className={classes.linkIcon} />
      {summary.name}
    </MenuItem>
  );

  const buildSearchMenuItems = () =>
    auxiliarySearches.map((summary) => (
      <OeqLink
        routeLinkUrlProvider={() => urlGeneratorForRouteLink(summary.uuid)}
        muiLinkUrlProvider={() => urlGeneratorForMuiLink(summary.uuid)}
        key={summary.uuid}
      >
        {getLinkContent(summary)}
      </OeqLink>
    ));

  return (
    <FormControl variant="outlined" fullWidth>
      <Select value="">{buildSearchMenuItems()}</Select>
    </FormControl>
  );
};
