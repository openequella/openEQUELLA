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
import { Button, Grid } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as T from "fp-ts/Task";
import * as TO from "fp-ts/TaskOption";
import { DateTime } from "luxon";
import * as React from "react";
import { useHistory } from "react-router";
import { routes } from "../../mainui/routes";
import { PortletPosition } from "../../modules/DashboardModule";
import { searchItemsBasic } from "../../modules/SearchModule";
import { languageStrings } from "../../util/langstrings";
import { DraggablePortlet } from "../components/DraggablePortlet";
import { PortletSearchResultList } from "../components/PortletSearchResultList";
import { PortletSearchResultNoneFound } from "../components/PortletSearchResultNoneFound";
import { convertSearchParamsToOptions } from "./PortletRecentContributionsHelper";

const strings = {
  ...languageStrings.dashboard.portlets.recentContributions,
  actionShowAll: languageStrings.common.action.showAll,
};

interface PortletRecentContributionsProps {
  /** The portlet configuration */
  cfg: OEQ.Dashboard.RecentContributionsPortlet;
  /** The actual position of the portlet in the page which is used for drag and drop operations. */
  position: PortletPosition;
  /** Optional search provider - primarily for testing. */
  searchProvider?: typeof searchItemsBasic;
}

/**
 * Portlet that displays recent contributions based on configured criteria.
 */
export const PortletRecentContributions: React.FC<
  PortletRecentContributionsProps
> = ({
  cfg,
  position,
  searchProvider = searchItemsBasic,
}: PortletRecentContributionsProps) => {
  const history = useHistory();

  const [loading, setLoading] = React.useState<boolean>(true);
  const [items, setItems] = React.useState<OEQ.Search.SearchResultItem[]>([]);

  const buildSearchParams = React.useCallback((): OEQ.Search.SearchParams => {
    const withStatus: OEQ.Common.ItemStatus[] | undefined = pipe(
      cfg.itemStatus,
      O.fromNullable,
      O.map(A.of<OEQ.Common.ItemStatus>),
      O.toUndefined,
    );

    // A string representing the maxAge (days) as a date in the format of yyyy-MM-dd
    // Defaults to 30 days if no maxAge is provided
    const withMaxDate: string = pipe(
      cfg.maxAge,
      O.fromNullable,
      O.getOrElse(() => 30),
      (days) => DateTime.now().minus({ days }).toISODate(),
    );

    return {
      query: cfg.query,
      collections: cfg.collectionUuids,
      status: withStatus,
      modifiedAfter: withMaxDate,
      order: "datemodified",
      length: 5,
    };
  }, [cfg]);

  React.useEffect(() => {
    const searchParams = buildSearchParams();

    const fetchRecentContributions = pipe(
      TO.tryCatch(() => searchProvider(searchParams)),
      TO.match(
        () => [],
        (items) => items.results,
      ),
      T.map(setItems),
      T.tapIO(() => () => setLoading(false)),
    );

    fetchRecentContributions();
  }, [buildSearchParams, searchProvider]);

  const goToSearchPage = () =>
    pipe(
      buildSearchParams(),
      convertSearchParamsToOptions,
      routes.SearchPage.withOptions,
      history.push,
    );

  return (
    <DraggablePortlet portlet={cfg} isLoading={loading} position={position}>
      <Grid container direction="column" spacing={2}>
        <Grid>
          {A.isEmpty(items) ? (
            <PortletSearchResultNoneFound
              noneFoundMessage={strings.noneFound}
            />
          ) : (
            <PortletSearchResultList
              results={items}
              hideDescription={cfg.isShowTitleOnly}
            />
          )}
        </Grid>
        <Grid display="flex" justifyContent="center">
          <Button variant="outlined" onClick={goToSearchPage}>
            {strings.actionShowAll}
          </Button>
        </Grid>
      </Grid>
    </DraggablePortlet>
  );
};
