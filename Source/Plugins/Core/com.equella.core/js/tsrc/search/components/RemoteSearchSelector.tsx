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
import { Link } from "react-router-dom";
import { routes } from "../../mainui/routes";
import { getRemoteSearchesFromServer } from "../../modules/RemoteSearchModule";

const useStyles = makeStyles((theme: Theme) => ({
  linkIcon: {
    marginRight: theme.spacing(2),
  },
}));

export interface RemoteSearchSelectorProps {
  /**
   * Function to provide a list of remote searches - exposed to support injection for testing/storybooking.
   */
  remoteSearchesSupplier?: () => Promise<OEQ.Common.BaseEntitySummary[]>;
}
/**
 * A search 'selector' control to provide a list of remote searches. Clicking on a remote
 * searches (currently) navigates you to the legacy UI remote search page for that remote
 * search.
 */
export const RemoteSearchSelector = ({
  remoteSearchesSupplier = getRemoteSearchesFromServer,
}: RemoteSearchSelectorProps) => {
  const classes = useStyles();

  const [remoteSearches, setRemoteSearches] = useState<
    OEQ.Common.BaseEntitySummary[]
  >([]);

  useEffect(() => {
    remoteSearchesSupplier().then((searches) => setRemoteSearches(searches));
  }, [remoteSearchesSupplier]);

  const buildRemoteSearchMenuItems = () =>
    remoteSearches.map((summary) => (
      <Link to={routes.RemoteSearch.to(summary.uuid)}>
        <MenuItem value={summary.uuid}>
          <LinkIcon className={classes.linkIcon} />
          {summary.name}
        </MenuItem>
      </Link>
    ));

  return (
    <FormControl variant="outlined" fullWidth>
      <Select>{buildRemoteSearchMenuItems()}</Select>
    </FormControl>
  );
};
