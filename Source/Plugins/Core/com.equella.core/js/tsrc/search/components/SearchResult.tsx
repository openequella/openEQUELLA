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
  Divider,
  Grid,
  Hidden,
  IconButton,
  List,
  ListItem,
  ListItemText,
  Theme,
  Typography,
} from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import DragIndicatorIcon from "@material-ui/icons/DragIndicator";
import FavoriteIcon from "@material-ui/icons/Favorite";
import FavoriteBorderIcon from "@material-ui/icons/FavoriteBorder";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { useState } from "react";
import ReactHtmlParser from "react-html-parser";
import { HashLink } from "react-router-hash-link";
import { sprintf } from "sprintf-js";
import { Date as DateDisplay } from "../../components/Date";
import { OeqLink } from "../../components/OeqLink";
import OEQThumb from "../../components/OEQThumb";
import { StarRating } from "../../components/StarRating";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { routes } from "../../mainui/routes";
import {
  addFavouriteItem,
  deleteFavouriteItem,
} from "../../modules/FavouriteModule";
import {
  buildSelectionSessionItemSummaryLink,
  getSearchPageItemClass,
  isSelectionSessionInStructured,
  isSelectionSessionOpen,
  isSelectSummaryButtonDisabled,
  selectResource,
} from "../../modules/LegacySelectionSessionModule";
import { getMimeTypeDefaultViewerDetails } from "../../modules/MimeTypesModule";
import { formatSize, languageStrings } from "../../util/langstrings";
import { highlight } from "../../util/TextUtils";
import { FavouriteItemDialog } from "./FavouriteItemDialog";
import type {
  FavDialogConfirmToAdd,
  FavDialogConfirmToDelete,
} from "./FavouriteItemDialog";
import { ResourceSelector } from "./ResourceSelector";
import { SearchResultAttachmentsList } from "./SearchResultAttachmentsList";

const {
  searchResult: searchResultStrings,
  comments: commentStrings,
  starRatings: ratingStrings,
  selectResource: selectResourceStrings,
  favouriteItem: favouriteItemStrings,
} = languageStrings.searchpage;

const useStyles = makeStyles((theme: Theme) => {
  return {
    inline: {
      display: "inline",
    },
    heading: {
      fontWeight: "bold",
      paddingRight: theme.spacing(1),
    },
    itemDescription: {
      paddingBottom: theme.spacing(1),
    },
    additionalDetails: {
      flexDirection: "row",
      display: "flex",
      paddingTop: theme.spacing(1),
      alignItems: "center",
    },
    status: {
      textTransform: "capitalize",
    },
    highlight: {
      color: theme.palette.secondary.main,
    },
  };
});

export interface SearchResultProps {
  /**
   * Optional function to dependency inject for retrieval of viewers (good for storybook etc). Will
   * default to using `getMimeTypeDefaultViewerDetails` from MimeTypesModule.
   *
   * @param mimeType MIME type to determine the viewer setup for
   */
  getViewerDetails?: (
    mimeType: string
  ) => Promise<OEQ.MimeType.MimeTypeViewerDetail>;
  /**
   * Error handler for standard management of errors during processing - especially comms errors.
   */
  handleError: (error: Error) => void;
  /**
   * The list of words which should be highlighted.
   */
  highlights: string[];
  /**
   * The details of the items to display.
   */
  item: OEQ.Search.SearchResultItem;
}

export default function SearchResult({
  getViewerDetails = getMimeTypeDefaultViewerDetails,
  handleError,
  highlights,
  item,
}: SearchResultProps) {
  const {
    name,
    version,
    uuid,
    description,
    displayFields,
    modifiedDate,
    status,
    displayOptions,
    attachments = [],
    commentCount = 0,
    starRatings,
    bookmarkId: bookmarkDefaultId,
    isLatestVersion,
  } = item;
  const itemKey = `${uuid}/${version}`;
  const classes = useStyles();
  const inSelectionSession: boolean = isSelectionSessionOpen();
  const inStructured = isSelectionSessionInStructured();

  const [
    showFavouriteItemDialog,
    setShowFavouriteItemDialog,
  ] = useState<boolean>(false);
  const [bookmarkId, setBookmarkId] = useState<number | undefined>(
    bookmarkDefaultId
  );

  const handleSelectResource = (
    itemKey: string,
    attachments: string[] = []
  ) => {
    selectResource(itemKey, attachments).catch((error) => handleError(error));
  };

  const handleAddFavouriteItem: FavDialogConfirmToAdd = {
    action: "add",
    onConfirm: (tags: string[], isAlwaysLatest: boolean) =>
      addFavouriteItem(`${uuid}/${version}`, tags, isAlwaysLatest)
        .then(({ bookmarkID }) => setBookmarkId(bookmarkID))
        .catch(handleError),
  };

  const handleDeleteFavouriteItem: FavDialogConfirmToDelete = {
    action: "delete",
    onConfirm: () => {
      if (!bookmarkId) {
        throw new Error("Bookmark ID can't be falsy.");
      }
      return deleteFavouriteItem(bookmarkId)
        .then(() => setBookmarkId(undefined))
        .catch(handleError);
    },
  };

  const generateItemMetadata = () => {
    const metaDataDivider = (
      <Divider
        flexItem
        component="span"
        variant="middle"
        orientation="vertical"
      />
    );

    return (
      <div className={classes.additionalDetails}>
        <Typography component="span" className={classes.status}>
          {status}
        </Typography>

        {metaDataDivider}
        <Typography component="span">
          {searchResultStrings.dateModified}&nbsp;
          <DateDisplay displayRelative date={new Date(modifiedDate)} />
        </Typography>

        {metaDataDivider}
        <TooltipIconButton
          title={
            bookmarkId
              ? favouriteItemStrings.title.remove
              : favouriteItemStrings.title.add
          }
          onClick={() => setShowFavouriteItemDialog(true)}
          size="small"
        >
          {bookmarkId ? <FavoriteIcon /> : <FavoriteBorderIcon />}
        </TooltipIconButton>

        {commentCount > 0 && (
          <Hidden smDown>
            {metaDataDivider}
            <Typography component="span">
              <HashLink
                to={`${routes.ViewItem.to(uuid, version)}#comments-list`}
                smooth
              >
                {formatSize(commentCount, commentStrings)}
              </HashLink>
            </Typography>
          </Hidden>
        )}

        {starRatings >= 0 && (
          <Hidden smDown>
            {metaDataDivider}
            <div aria-label={sprintf(ratingStrings.label, starRatings)}>
              <StarRating numberOfStars={5} rating={starRatings} />
            </div>
          </Hidden>
        )}
      </div>
    );
  };

  const customDisplayMetadata = displayFields.map(
    (element: OEQ.Search.DisplayFields, index: number) => {
      return (
        <ListItem disableGutters dense key={element.name + index}>
          <Typography
            component="span"
            variant="body2"
            className={classes.heading}
            color="textPrimary"
          >
            {element.name}
          </Typography>
          <Typography
            component="span"
            variant="body2"
            className={classes.inline}
            color="textPrimary"
          >
            {
              /**Custom metadata can contain html tags, we should make sure that is
          preserved */
              ReactHtmlParser(element.html)
            }
          </Typography>
        </ListItem>
      );
    }
  );

  const highlightField = (fieldValue: string) =>
    ReactHtmlParser(highlight(fieldValue, highlights, classes.highlight));

  const itemLink = () => {
    const itemTitle = name ? highlightField(name) : uuid;
    return (
      <OeqLink
        routeLinkUrlProvider={() => routes.ViewItem.to(uuid, version)}
        muiLinkUrlProvider={() =>
          buildSelectionSessionItemSummaryLink(uuid, version)
        }
      >
        {itemTitle}
      </OeqLink>
    );
  };

  // In Selection Session, if the Select Summary button is enabled, add 'ResourceSelector'
  // to the content. On top of that, if Selection Session is in 'structured', add one
  // 'Drag indicator' icon.
  const selectSessionItemContent = (
    <Grid
      id={uuid}
      container
      alignItems="center"
      className={getSearchPageItemClass()} // Give a class so each item can be dropped to the course list.
      data-itemuuid={uuid}
      data-itemversion={version}
    >
      {inStructured && (
        <Grid item>
          <IconButton>
            <DragIndicatorIcon />
          </IconButton>
        </Grid>
      )}
      <Grid item>{itemLink()}</Grid>
      <Grid item>
        <ResourceSelector
          labelText={selectResourceStrings.summaryPage}
          isStopPropagation
          onClick={() => {
            handleSelectResource(itemKey);
          }}
        />
      </Grid>
    </Grid>
  );

  const itemPrimaryContent =
    inSelectionSession && !isSelectSummaryButtonDisabled()
      ? selectSessionItemContent
      : itemLink();

  return (
    <>
      <ListItem
        alignItems="flex-start"
        divider
        aria-label={searchResultStrings.ariaLabel}
      >
        <OEQThumb
          attachment={attachments[0]}
          showPlaceholder={displayOptions?.disableThumbnail ?? false}
        />
        <ListItemText
          primary={itemPrimaryContent}
          secondary={
            <>
              <Typography className={classes.itemDescription}>
                {highlightField(description ?? "")}
              </Typography>
              <List disablePadding>{customDisplayMetadata}</List>
              <SearchResultAttachmentsList
                item={item}
                handleError={handleError}
                getViewerDetails={getViewerDetails}
              />
              {generateItemMetadata()}
            </>
          }
          primaryTypographyProps={{ color: "primary", variant: "h6" }}
          secondaryTypographyProps={{ component: "section" }}
        />
      </ListItem>
      {showFavouriteItemDialog && (
        <FavouriteItemDialog
          isAddedToFavourite={bookmarkId !== undefined}
          isLatestVersion={isLatestVersion}
          onConfirmProps={
            bookmarkId ? handleDeleteFavouriteItem : handleAddFavouriteItem
          }
          open={showFavouriteItemDialog}
          closeDialog={() => setShowFavouriteItemDialog(false)}
        />
      )}
    </>
  );
}
