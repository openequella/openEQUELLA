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
import BookmarkAddOutlinedIcon from "@mui/icons-material/BookmarkAddOutlined";
import DragIndicatorIcon from "@mui/icons-material/DragIndicator";
import FavoriteIcon from "@mui/icons-material/Favorite";
import FavoriteBorderIcon from "@mui/icons-material/FavoriteBorder";
import {
  Grid,
  IconButton,
  List,
  ListItem,
  ListItemText,
  Typography,
  useMediaQuery,
  Box,
  Chip,
} from "@mui/material";
import { styled, Theme } from "@mui/material/styles";
import * as OEQ from "@openequella/rest-api-client";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as A from "fp-ts/Array";
import HTMLReactParser from "html-react-parser";
import * as React from "react";
import { Fragment, useEffect, useState, type ReactNode } from "react";
import { useHistory } from "react-router";
import { HashLink } from "react-router-hash-link";
import { sprintf } from "sprintf-js";
import { Date as DateDisplay } from "../../components/Date";
import HighlightField from "../../components/HighlightField";
import MetadataRow from "../../components/MetadataRow";
import { OEQLink } from "../../components/OEQLink";
import OEQThumb from "../../components/OEQThumb";
import { StarRating } from "../../components/StarRating";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { createDrmDialog } from "../../drm/DrmHelper";
import { routes } from "../../mainui/routes";
import { defaultDrmStatus } from "../../modules/DrmModule";
import {
  addFavouriteItem,
  deleteFavouriteItem,
} from "../../modules/FavouriteModule";
import {
  getSearchPageItemClass,
  isSelectionSessionInStructured,
  isSelectionSessionOpen,
  isSelectSummaryButtonDisabled,
  selectResource,
} from "../../modules/LegacySelectionSessionModule";
import { getMimeTypeDefaultViewerDetails } from "../../modules/MimeTypesModule";
import { isLiveItem, searchItemAttachments } from "../../modules/SearchModule";
import { formatSize, languageStrings } from "../../util/langstrings";
import { buildOpenSummaryPageHandler } from "../SearchPageHelper";
import FavouriteItemDialog from "./FavouriteItemDialog";
import { SearchResultAttachmentsList } from "./SearchResultAttachmentsList";
import ModifyKeyResourceDialog from "./ModifyKeyResourceDialog";
import { ResourceSelector } from "./ResourceSelector";

const {
  searchResult: searchResultStrings,
  comments: commentStrings,
  starRatings: ratingStrings,
  selectResource: selectResourceStrings,
  favouriteItem: favouriteItemStrings,
  addToHierarchy: { title: addToHierarchyTitle },
} = languageStrings.searchpage;
const { tags: tagsLabel } = languageStrings.favourites.favouritesItem;

const PREFIX = "SearchResult";

const classes = {
  inline: `${PREFIX}-inline`,
  heading: `${PREFIX}-heading`,
  itemDescription: `${PREFIX}-itemDescription`,
  additionalDetails: `${PREFIX}-additionalDetails`,
  status: `${PREFIX}-status`,
  divider: `${PREFIX}-divider`,
};

const StyledDiv = styled("div")(({ theme }) => {
  return {
    [`& .${classes.inline}`]: {
      display: "inline",
    },
    [`& .${classes.heading}`]: {
      fontWeight: "bold",
      paddingRight: theme.spacing(1),
    },
    [`& .${classes.itemDescription}`]: {
      paddingBottom: theme.spacing(1),
    },
    [`& .${classes.additionalDetails}`]: {
      flexDirection: "row",
      display: "flex",
      paddingTop: theme.spacing(1),
      alignItems: "center",
    },
    [`& .${classes.status}`]: {
      textTransform: "capitalize",
    },
    [`& .${classes.divider}`]: {
      margin: "0px 16px",
    },
  };
});

/**
 * Props for controlling the visibility of action buttons for each SearchResult.
 */
interface SearchResultActionButtonConfig {
  /** `true` to show the Add to favourite button. */
  showAddToFavourite: boolean;
  /** `true` to show the Add to hierarchy button. */
  showAddToHierarchy: boolean;
}

export const defaultActionButtonProps: SearchResultActionButtonConfig = {
  showAddToFavourite: true,
  showAddToHierarchy: true,
};

export interface SearchResultProps {
  /**
   * Optional function to dependency inject for retrieval of viewers (good for storybook etc). Will
   * default to using `getMimeTypeDefaultViewerDetails` from MimeTypesModule.
   *
   * @param mimeType MIME type to determine the viewer setup for
   */
  getViewerDetails?: (
    mimeType: string,
  ) => Promise<OEQ.MimeType.MimeTypeViewerDetail>;
  /**
   * A function which can retrieve attachments for a specified item.
   */
  getItemAttachments?: (
    uuid: string,
    version: number,
  ) => Promise<OEQ.Search.Attachment[]>;
  /**
   * The list of words which should be highlighted.
   */
  highlights: string[];
  /**
   * The details of the items to display.
   */
  item: OEQ.Search.SearchResultItem;
  /**
   * Custom action buttons to be displayed. Each button will be separated by a vertical divider,
   * and they are displayed next to the FavoriteIcon.
   */
  customActionButtons?: React.JSX.Element[];
  /**
   * Custom handler for clicking the title of each SearchResult.
   */
  customOnClickTitleHandler?: () => void;
  /**
   * Props for display or hide the action buttons.
   */
  actionButtonConfig?: SearchResultActionButtonConfig;
  /**
   * Callback function when an item is removed from favourites.
   */
  onFavouriteRemoved?: () => void;
  /**
   * If `true`, displays bookmark tags for the item.
   */
  showBookmarkTags?: boolean;
}

/**
 * DRM is configured on Item level but it also affects how attachments work.
 * So create a DRM context to allow 'ItemAttachmentLink' to do a DRM permission check.
 */
export const ItemDrmContext = React.createContext<{
  /**
   * Function to do DRM permission check which will further control whether to show DRM dialog.
   *
   * @param onSuccess Handler that should be called once DRM permission check is successful.
   */
  checkDrmPermission: (onSuccess: () => void) => void;
}>({
  checkDrmPermission: () => {},
});

export default function SearchResult({
  getViewerDetails = getMimeTypeDefaultViewerDetails,
  getItemAttachments = searchItemAttachments,
  highlights,
  item,
  customActionButtons,
  customOnClickTitleHandler,
  actionButtonConfig = defaultActionButtonProps,
  onFavouriteRemoved,
  showBookmarkTags,
}: SearchResultProps) {
  const { showAddToHierarchy, showAddToFavourite } = actionButtonConfig;
  const isMdUp = useMediaQuery<Theme>((theme) => theme.breakpoints.up("md"));

  const {
    bookmark,
    commentCount = 0,
    description,
    displayFields,
    displayOptions,
    drmStatus: initialDrmStatus = defaultDrmStatus,
    isLatestVersion,
    modifiedDate,
    name,
    starRatings,
    status,
    thumbnailDetails,
    uuid,
    version,
  } = item;

  const isItemLive = isLiveItem(item);

  const itemKey = `${uuid}/${version}`;

  const inSelectionSession: boolean = isSelectionSessionOpen();
  const inStructured = isSelectionSessionInStructured();

  const [showFavouriteItemDialog, setShowFavouriteItemDialog] =
    useState<boolean>(false);
  const [showAddHierarchyDialog, setShowAddHierarchyDialog] =
    React.useState<boolean>(false);

  const [bookmarkId, setBookmarkId] = useState<number | undefined>(
    bookmark?.id,
  );

  const history = useHistory();

  const [drmDialog, setDrmDialog] = useState<React.JSX.Element | undefined>(
    undefined,
  );
  const [drmCheckOnSuccessHandler, setDrmCheckOnSuccessHandler] = useState<
    (() => void) | undefined
  >();
  const [drmStatus, setDrmStatus] =
    useState<OEQ.Search.DrmStatus>(initialDrmStatus);

  const highlight = (content: string) => (
    <HighlightField content={content} highlights={highlights} />
  );

  const checkDrmPermission = (onSuccess: () => void) =>
    setDrmCheckOnSuccessHandler(() => onSuccess);

  useEffect(() => {
    let mounted = true;
    (async () => {
      // If there is nothing requiring DRM permission check then return undefined.
      const dialog = drmCheckOnSuccessHandler
        ? await createDrmDialog(
            uuid,
            version,
            drmStatus,
            setDrmStatus,
            () => setDrmCheckOnSuccessHandler(undefined),
            drmCheckOnSuccessHandler,
          )
        : undefined;

      if (mounted) {
        setDrmDialog(dialog);
      }
    })();

    return () => {
      mounted = false;
    };
  }, [drmCheckOnSuccessHandler, uuid, version, drmStatus]);

  // Handlers for adding and deleting favourite items.
  const updateFavouriteItem = (
    isAdded: boolean,
    isAlwaysLatest: boolean,
    tags?: string[],
  ) => {
    // Add favourite item
    if (!isAdded) {
      return addFavouriteItem(
        `${uuid}/${version}`,
        tags ?? [],
        isAlwaysLatest,
      ).then(({ bookmarkID }) => setBookmarkId(bookmarkID));
    }

    // Delete favourite item
    if (!bookmarkId) {
      throw new Error("Bookmark ID can't be falsy.");
    }
    return deleteFavouriteItem(bookmarkId).then(() => {
      setBookmarkId(undefined);
      onFavouriteRemoved?.();
    });
  };

  const generateItemMetadata = () => (
    <MetadataRow>
      <Typography component="span" className={classes.status}>
        {status}
      </Typography>

      <Typography component="span">
        {searchResultStrings.dateModified}&nbsp;
        <DateDisplay displayRelative date={modifiedDate} />
      </Typography>

      {showAddToFavourite && (
        <TooltipIconButton
          title={
            bookmarkId ? favouriteItemStrings.remove : favouriteItemStrings.add
          }
          onClick={() => setShowFavouriteItemDialog(true)}
          size="small"
        >
          {bookmarkId ? <FavoriteIcon /> : <FavoriteBorderIcon />}
        </TooltipIconButton>
      )}

      {showAddToHierarchy && (
        <TooltipIconButton
          title={addToHierarchyTitle}
          onClick={() => setShowAddHierarchyDialog(true)}
          size="small"
        >
          <BookmarkAddOutlinedIcon />
        </TooltipIconButton>
      )}

      {customActionButtons?.map((button, index) => (
        <Fragment key={index}>{button}</Fragment>
      ))}

      {commentCount > 0 && isMdUp && (
        <Typography component="span">
          <HashLink
            to={`${routes.ViewItem.to(uuid, version)}#comments-list`}
            smooth
          >
            {formatSize(commentCount, commentStrings)}
          </HashLink>
        </Typography>
      )}

      {starRatings >= 0 && isMdUp && (
        <div aria-label={sprintf(ratingStrings.label, starRatings)}>
          <StarRating numberOfStars={5} rating={starRatings} />
        </div>
      )}
    </MetadataRow>
  );

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
              // Custom metadata can contain html tags,
              // we should make sure that is preserved
              HTMLReactParser(element.html)
            }
          </Typography>
        </ListItem>
      );
    },
  );

  const itemLink = () => {
    const itemTitle = name ? highlight(name) : uuid;
    const { url, onClick } = buildOpenSummaryPageHandler(
      uuid,
      version,
      history,
    );

    return (
      <OEQLink
        routeLinkUrlProvider={() => url}
        muiLinkUrlProvider={() => url}
        onClick={(e: React.MouseEvent<HTMLAnchorElement>) => {
          pipe(
            customOnClickTitleHandler,
            O.fromNullable,
            O.alt(() =>
              drmStatus.isAllowSummary
                ? O.none
                : O.of(() => checkDrmPermission(onClick)),
            ),
            O.map((handler) => {
              e.preventDefault();
              return handler();
            }),
          );
        }}
      >
        {itemTitle}
      </OEQLink>
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
      {isItemLive && inStructured && (
        <Grid>
          <IconButton size="large">
            <DragIndicatorIcon />
          </IconButton>
        </Grid>
      )}
      <Grid>{itemLink()}</Grid>
      {isItemLive && (
        <Grid>
          <ResourceSelector
            labelText={selectResourceStrings.summaryPage}
            isStopPropagation
            onClick={() => selectResource(itemKey)}
          />
        </Grid>
      )}
    </Grid>
  );

  const itemPrimaryContent =
    inSelectionSession && !isSelectSummaryButtonDisabled()
      ? selectSessionItemContent
      : itemLink();

  const displayBookmarkTags: ReactNode = pipe(
    O.fromNullable(bookmark?.tags),
    O.filter(A.isNonEmpty),
    O.map((tagList) => (
      <ListItem dense disableGutters>
        <Typography
          component="span"
          variant="body2"
          className={classes.heading}
          color="textPrimary"
          aria-label={tagsLabel}
        >
          {tagsLabel}&nbsp;
        </Typography>
        <Box>
          {tagList.map((tag, index) => (
            <Chip
              component="span"
              sx={{ mr: 0.5 }}
              key={index}
              label={tag}
              aria-label={tag}
              color="secondary"
              size="small"
            />
          ))}
        </Box>
      </ListItem>
    )),
    O.toUndefined,
  );

  return (
    <StyledDiv>
      <ListItem
        alignItems="flex-start"
        divider
        aria-label={searchResultStrings.ariaLabel}
        data-item-id={uuid}
        data-item-version={version}
      >
        <OEQThumb
          details={
            displayOptions?.disableThumbnail ? undefined : thumbnailDetails
          }
        />
        <ListItemText
          primary={itemPrimaryContent}
          secondary={
            <>
              <Typography className={classes.itemDescription}>
                {highlight(description ?? "")}
              </Typography>
              <List disablePadding>{customDisplayMetadata}</List>
              {showBookmarkTags && displayBookmarkTags}
              <ItemDrmContext.Provider
                value={{
                  checkDrmPermission,
                }}
              >
                <SearchResultAttachmentsList
                  item={item}
                  getViewerDetails={getViewerDetails}
                  getItemAttachments={getItemAttachments}
                  isItemLive={isItemLive}
                />
              </ItemDrmContext.Provider>
              {generateItemMetadata()}
            </>
          }
          slotProps={{
            primary: { color: "primary", variant: "h6" },
            secondary: { component: "section" },
          }}
        />
      </ListItem>

      {showFavouriteItemDialog && (
        <FavouriteItemDialog
          isAdded={bookmarkId !== undefined}
          isLatestVersion={isLatestVersion}
          updateFavouriteItem={updateFavouriteItem}
          open={showFavouriteItemDialog}
          closeDialog={() => setShowFavouriteItemDialog(false)}
        />
      )}

      {showAddHierarchyDialog && (
        <ModifyKeyResourceDialog
          item={item}
          open={showAddHierarchyDialog}
          onClose={() => setShowAddHierarchyDialog(false)}
        />
      )}

      {drmDialog}
    </StyledDiv>
  );
}
