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

import FavoriteIcon from "@mui/icons-material/Favorite";
import { ListItem, ListItemText, Typography } from "@mui/material";
import { identity, pipe } from "fp-ts/function";
import * as TE from "fp-ts/TaskEither";
import { useEffect } from "react";
import * as React from "react";
import ConfirmDialog from "../../components/ConfirmDialog";
import HighlightField from "../../components/HighlightField";
import MetadataRow from "../../components/MetadataRow";
import { OEQLink } from "../../components/OEQLink";
import SettingsListAlert from "../../components/SettingsListAlert";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { deleteFavouriteSearch } from "../../modules/FavouriteModule";
import { languageStrings } from "../../util/langstrings";
import { Date as DateDisplay } from "../../components/Date";
import { pfTernary } from "../../util/pointfree";
import { isNonEmptyString } from "../../util/validation";
import {
  buildFavouritesSearchUrl,
  buildSearchOptionsSummary,
  FavouriteSearchOptionsSummary,
} from "./FavouritesSearchHelper";
import SearchOptions from "./SearchOptions";
import SearchOptionSkeleton from "./SearchOptionsSkeleton";
import * as OEQ from "@openequella/rest-api-client";
import * as T from "fp-ts/Task";

const {
  addedAt: addedAtLabel,
  remove: removeLabel,
  removeAlert,
} = languageStrings.favourites.favouritesSearch;

export interface FavouritesSearchProps {
  /**
   * The favourite search to display.
   */
  favouriteSearch: OEQ.Favourite.FavouriteSearch;
  /**
   * The list of words which should be highlighted.
   */
  highlights: string[];
  /**
   * A function that generates search page option labels from the URL.
   */
  favouriteSearchOptionsSummaryProvider?: (
    url: string,
  ) => Promise<FavouriteSearchOptionsSummary | undefined>;
  /**
   * Callback invoked when the favourite search is removed.
   */
  onFavouriteRemoved: () => void;
}

/**
 * Displays a favourite search.
 * It shows the title, a list of search options and added date.
 */
const FavouritesSearch = ({
  favouriteSearch: { name, url, addedAt, id: searchId },
  highlights,
  favouriteSearchOptionsSummaryProvider = buildSearchOptionsSummary,
  onFavouriteRemoved,
}: FavouritesSearchProps) => {
  const [isLoading, setIsLoading] = React.useState(true);
  const [errorMessage, setErrorMessage] = React.useState<string | undefined>(
    undefined,
  );
  const [searchOptionsSummary, setSearchOptionsSummary] = React.useState<
    FavouriteSearchOptionsSummary | undefined
  >(undefined);
  const [isRemoveDialogOpen, setIsRemoveDialogOpen] = React.useState(false);

  useEffect(() => {
    pipe(
      TE.tryCatch(() => favouriteSearchOptionsSummaryProvider(url), String),
      TE.match(setErrorMessage, setSearchOptionsSummary),
      T.tapIO(() => () => setIsLoading(false)),
    )();
  }, [favouriteSearchOptionsSummaryProvider, url]);

  const titleLink = () => {
    const realUrl = buildFavouritesSearchUrl(url);

    return (
      <OEQLink
        routeLinkUrlProvider={() => realUrl}
        muiLinkUrlProvider={() => realUrl}
      >
        <HighlightField content={name} highlights={highlights} />
      </OEQLink>
    );
  };

  const errorContent = (message: string) => (
    <SettingsListAlert
      sx={{ paddingLeft: 0 }}
      severity="error"
      messages={[message]}
    />
  );

  const renderSearchOptions = () =>
    isNonEmptyString(errorMessage) ? (
      errorContent(errorMessage)
    ) : (
      <SearchOptions searchOptionsSummary={searchOptionsSummary} />
    );

  const onRemoveFavouriteSearch = () => {
    deleteFavouriteSearch(searchId).then(() => {
      setIsRemoveDialogOpen(false);
      onFavouriteRemoved();
    });
  };

  return (
    <>
      <ListItem alignItems="flex-start" divider aria-label={name}>
        <ListItemText
          primary={titleLink()}
          secondary={
            <>
              {pipe(
                isLoading,
                pfTernary(
                  identity<boolean>,
                  () => <SearchOptionSkeleton />,
                  () => renderSearchOptions(),
                ),
              )}

              <MetadataRow>
                <Typography>
                  {addedAtLabel}&nbsp;
                  <DateDisplay displayRelative date={addedAt} />
                </Typography>

                <TooltipIconButton
                  title={removeLabel}
                  onClick={() => setIsRemoveDialogOpen(true)}
                  size="small"
                >
                  <FavoriteIcon />
                </TooltipIconButton>
              </MetadataRow>
            </>
          }
          // Make it consistent with the SearchResult component
          slotProps={{
            primary: { color: "primary", variant: "h6" },
            secondary: { component: "section" },
          }}
        />
      </ListItem>
      <ConfirmDialog
        open={isRemoveDialogOpen}
        title={removeLabel}
        onConfirm={onRemoveFavouriteSearch}
        onCancel={() => setIsRemoveDialogOpen(false)}
        confirmButtonText={languageStrings.common.action.ok}
      >
        {removeAlert}
      </ConfirmDialog>
    </>
  );
};

export default FavouritesSearch;
