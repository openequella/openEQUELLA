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
import * as React from "react";
import { useEffect, useState } from "react";
import {
  templateDefaults,
  templateError,
  TemplateUpdateProps,
} from "../mainui/Template";
import { languageStrings } from "../util/langstrings";
import {
  Card,
  CardContent,
  Grid,
  List,
  ListItem,
  ListSubheader,
  Typography,
} from "@material-ui/core";
import SearchBar from "../search/components/SearchBar";
import { searchItems } from "./SearchModule";
import * as OEQ from "@openequella/rest-api-client";
import SearchResult from "./components/SearchResult";
import { generateFromError } from "../api/errors";
import {
  getSearchSettingsFromServer,
  SearchSettings,
} from "../settings/Search/SearchSettingsModule";

const SearchPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const searchStrings = languageStrings.searchpage;
  const [searchResultItems, setSearchResultItems] = useState<
    OEQ.Search.SearchResultItem[]
  >([]);

  /**
   * Update the page title and do a default search.
   */
  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(searchStrings.title)(tp),
    }));

    getSearchSettingsFromServer().then((settings: SearchSettings) =>
      search({
        status: [OEQ.Common.ItemStatus.LIVE, OEQ.Common.ItemStatus.REVIEW],
        order: settings.defaultSearchSort,
      })
    );
  }, []);

  const handleSearch = (query: string) => {
    search({
      query,
      status: [OEQ.Common.ItemStatus.LIVE, OEQ.Common.ItemStatus.REVIEW],
    });
  };

  const handleError = (error: Error) => {
    updateTemplate(templateError(generateFromError(error)));
  };

  /**
   * Search items with specified search criteria.
   * @param params Search criteria
   */
  const search = (params?: OEQ.Search.SearchParams): void => {
    searchItems(params)
      .then((items: OEQ.Common.PagedResult<OEQ.Search.SearchResultItem>) =>
        setSearchResultItems(items.results)
      )
      .catch((error: Error) => handleError(error));
  };

  /**
   * A SearchResult that represents one of the search result items.
   */
  const searchResults = searchResultItems.map(
    (item: OEQ.Search.SearchResultItem) => (
      <ListItem key={item.uuid} divider>
        <SearchResult {...item} />
      </ListItem>
    )
  );

  /**
   * A list that consists of search result items.
   */
  const searchResultList = (
    <List subheader={<ListSubheader>{searchStrings.subtitle}</ListSubheader>}>
      {searchResults.length > 0 ? (
        searchResults
      ) : (
        <ListItem key={searchStrings.noResultsFound} divider>
          <Typography>{searchStrings.noResultsFound}</Typography>
        </ListItem>
      )}
    </List>
  );

  return (
    <Grid container direction="column" spacing={2}>
      <Grid item xs={9}>
        <Card>
          <CardContent>
            <SearchBar onChange={handleSearch} />
          </CardContent>
        </Card>
      </Grid>

      <Grid item xs={9}>
        <Card>
          <CardContent>{searchResultList}</CardContent>
        </Card>
      </Grid>
    </Grid>
  );
};

export default SearchPage;
