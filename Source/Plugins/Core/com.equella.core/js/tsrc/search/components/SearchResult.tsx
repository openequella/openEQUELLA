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
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Badge,
  Divider,
  Grid,
  Hidden,
  IconButton,
  List,
  ListItem,
  ListItemIcon,
  ListItemSecondaryAction,
  ListItemText,
  Theme,
  Typography,
} from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import Tooltip from "@material-ui/core/Tooltip";
import AttachFile from "@material-ui/icons/AttachFile";
import ExpandMore from "@material-ui/icons/ExpandMore";
import InsertDriveFile from "@material-ui/icons/InsertDriveFile";
import DragIndicatorIcon from "@material-ui/icons/DragIndicator";
import FavoriteIcon from "@material-ui/icons/Favorite";
import FavoriteBorderIcon from "@material-ui/icons/FavoriteBorder";
import Search from "@material-ui/icons/Search";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { SyntheticEvent, useEffect, useState } from "react";
import ReactHtmlParser from "react-html-parser";
import { HashLink } from "react-router-hash-link";
import { sprintf } from "sprintf-js";
import { Date as DateDisplay } from "../../components/Date";
import ItemAttachmentLink from "../../components/ItemAttachmentLink";
import { OeqLink } from "../../components/OeqLink";
import OEQThumb from "../../components/OEQThumb";
import { StarRating } from "../../components/StarRating";
import { TooltipIconButton } from "../../components/TooptipIconButton";
import { routes } from "../../mainui/routes";
import {
  addFavouriteItem,
  deleteFavouriteItem,
} from "../../modules/FavouriteModule";
import { getMimeTypeDefaultViewerDetails } from "../../modules/MimeTypesModule";
import {
  buildSelectionSessionItemSummaryLink,
  selectResource,
  isSelectionSessionOpen,
  isSelectSummaryButtonDisabled,
  prepareDraggable,
  getSearchPageItemClass,
  getSearchPageAttachmentClass,
  isSelectionSessionInStructured,
  isSelectionSessionInSkinny,
} from "../../modules/LegacySelectionSessionModule";
import { formatSize, languageStrings } from "../../util/langstrings";
import { highlight } from "../../util/TextUtils";
import type {
  FavDialogConfirmToAdd,
  FavDialogConfirmToDelete,
  FavouriteItemInfo,
} from "./FavouriteItemDialog";
import { ResourceSelector } from "./ResourceSelector";
import {
  determineAttachmentViewUrl,
  determineViewer,
} from "../../modules/ViewerModule";

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
    nested: {
      paddingLeft: theme.spacing(4),
    },
    attachmentExpander: {
      marginTop: theme.spacing(2),
      marginBottom: theme.spacing(2),
    },
    attachmentBadge: {
      backgroundColor: theme.palette.background.paper,
      color: theme.palette.secondary.main,
      borderRadius: "50%",
    },
    attachmentListItem: {
      width: "100%",
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
  /**
   * Function fired to help update props of FavouriteItemDialog.
   */
  onFavouriteItem: (itemInfo: FavouriteItemInfo) => void;
}

export default function SearchResult({
  getViewerDetails = getMimeTypeDefaultViewerDetails,
  handleError,
  highlights,
  item: {
    name,
    version,
    uuid,
    description,
    displayFields,
    modifiedDate,
    status,
    displayOptions,
    attachments = [],
    keywordFoundInAttachment,
    commentCount = 0,
    starRatings,
    bookmarkId: bookmarkDefaultId,
    isLatestVersion,
  },
  onFavouriteItem,
}: SearchResultProps) {
  interface AttachmentAndViewerDetails {
    attachment: OEQ.Search.Attachment;
    viewerDetails?: OEQ.MimeType.MimeTypeViewerDetail;
  }
  const itemKey = `${uuid}/${version}`;
  const classes = useStyles();
  const inSelectionSession: boolean = isSelectionSessionOpen();
  const inStructured = isSelectionSessionInStructured();
  const inSkinny = isSelectionSessionInSkinny();

  const [attachExpanded, setAttachExpanded] = useState(
    (inSelectionSession
      ? displayOptions?.integrationOpen
      : displayOptions?.standardOpen) ?? false
  );
  const [
    attachmentsWithViewerDetails,
    setAttachmentsWithViewerDetails,
  ] = useState<AttachmentAndViewerDetails[]>([]);

  const [bookmarkId, setBookmarkId] = useState<number | undefined>(
    bookmarkDefaultId
  );

  // Responsible for determining the MIME type viewer for the provided attachments
  useEffect(() => {
    let mounted = true;

    if (!attachments.length) {
      // If there are no attachments, skip this effect
      return;
    }

    const transform = async (
      a: OEQ.Search.Attachment
    ): Promise<AttachmentAndViewerDetails> => {
      let viewerDetails: OEQ.MimeType.MimeTypeViewerDetail | undefined;
      try {
        viewerDetails = a.mimeType
          ? await getViewerDetails(a.mimeType)
          : undefined;
      } catch (error) {
        handleError({
          ...error,
          message: `${searchResultStrings.errors.getAttachmentViewerDetailsFailure}: ${error.message}`,
        });
      }

      return {
        attachment: a,
        viewerDetails: viewerDetails,
      };
    };

    (async () => {
      const viewerDetails = await Promise.all(
        attachments.map<Promise<AttachmentAndViewerDetails>>(transform)
      );
      if (mounted) {
        setAttachmentsWithViewerDetails(viewerDetails);
      }
    })();

    return () => {
      // Short circuit if this component is unmounted before all its comms are done.
      mounted = false;
    };
  }, [attachments, getViewerDetails, handleError]);

  // In Selection Session, make each attachment draggable.
  useEffect(() => {
    if (inStructured) {
      attachmentsWithViewerDetails.forEach(({ attachment }) => {
        prepareDraggable(attachment.id, false);
      });
    }
  }, [attachmentsWithViewerDetails, inStructured]);

  const handleAttachmentPanelClick = (event: SyntheticEvent) => {
    /** prevents the SearchResult onClick from firing when attachment panel is clicked */
    event.stopPropagation();
    setAttachExpanded(!attachExpanded);
  };

  const handleSelectResource = (
    itemKey: string,
    attachments: string[] = []
  ) => {
    selectResource(itemKey, attachments).catch((error) => handleError(error));
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

    const favDialogConfirmToAdd: FavDialogConfirmToAdd = {
      action: "add",
      onConfirm: (tags: string[], isAlwaysLatest: boolean) =>
        addFavouriteItem(`${uuid}/${version}`, tags, isAlwaysLatest)
          .then(({ bookmarkID }) => setBookmarkId(bookmarkID))
          .catch(handleError),
    };

    const favDialogConfirmToDelete: FavDialogConfirmToDelete = {
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

    const favouriteItemButtonText = bookmarkId
      ? favouriteItemStrings.title.remove
      : favouriteItemStrings.title.add;

    const favouriteItemButtonIcon = bookmarkId ? (
      <FavoriteIcon />
    ) : (
      <FavoriteBorderIcon />
    );
    const favouriteItemButtonOnClick = () =>
      onFavouriteItem({
        isAddedToFavourite: bookmarkId !== undefined,
        isLatestVersion,
        onConfirmProps: bookmarkId
          ? favDialogConfirmToDelete
          : favDialogConfirmToAdd,
      });

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
          title={favouriteItemButtonText}
          onClick={favouriteItemButtonOnClick}
          size="small"
        >
          {favouriteItemButtonIcon}
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

  const generateAttachmentList = () => {
    const attachmentsList = attachmentsWithViewerDetails.map(
      ({
        attachment: {
          attachmentType,
          description,
          id,
          links: { view: defaultViewUrl },
          mimeType,
          filePath,
        },
        viewerDetails,
      }: AttachmentAndViewerDetails) => {
        const viewUrl = determineAttachmentViewUrl(
          uuid,
          version,
          attachmentType,
          defaultViewUrl,
          filePath
        );
        return (
          <ListItem
            key={id}
            id={id}
            button
            className={`${classes.nested} ${getSearchPageAttachmentClass()}`} // Give a class so each attachment can be dropped to the course list.
            data-itemuuid={uuid} // These 'data-xx' attributes are used in the 'dropCallBack' of 'courselist.js'.
            data-itemversion={version}
            data-attachmentuuid={id}
          >
            <ListItemIcon>
              {inStructured ? <DragIndicatorIcon /> : <InsertDriveFile />}
            </ListItemIcon>
            <ItemAttachmentLink
              description={description}
              mimeType={mimeType}
              viewerDetails={determineViewer(
                attachmentType,
                viewUrl,
                mimeType,
                viewerDetails?.viewerId
              )}
            >
              <ListItemText color="primary" primary={description} />
            </ItemAttachmentLink>
            {inSelectionSession && (
              <ListItemSecondaryAction>
                <ResourceSelector
                  labelText={selectResourceStrings.attachment}
                  isStopPropagation
                  onClick={() => {
                    handleSelectResource(itemKey, [id]);
                  }}
                />
              </ListItemSecondaryAction>
            )}
          </ListItem>
        );
      }
    );

    const accordionText = (
      <Typography>{searchResultStrings.attachments}</Typography>
    );

    const accordionSummaryContent = inSelectionSession ? (
      <Grid container alignItems="center">
        <Grid item>{accordionText}</Grid>
        <Grid>
          {!inSkinny && (
            <ResourceSelector
              labelText={selectResourceStrings.allAttachments}
              isStopPropagation
              onClick={() => {
                const attachments = attachmentsWithViewerDetails.map(
                  ({ attachment }) => attachment.id
                );
                handleSelectResource(itemKey, attachments);
              }}
            />
          )}
        </Grid>
      </Grid>
    ) : (
      accordionText
    );

    const attachFileBadge = (includeIndicator: boolean) => (
      <Badge
        anchorOrigin={{
          vertical: "bottom",
          horizontal: "right",
        }}
        overlap="circle"
        badgeContent={
          includeIndicator ? (
            <Tooltip
              title={searchResultStrings.keywordFoundInAttachment}
              aria-label={searchResultStrings.keywordFoundInAttachment}
            >
              <Search fontSize="small" className={classes.attachmentBadge} />
            </Tooltip>
          ) : undefined
        }
      >
        <AttachFile />
      </Badge>
    );

    if (attachmentsList.length > 0)
      return (
        <Accordion
          className={classes.attachmentExpander}
          expanded={attachExpanded}
          onClick={(event) => handleAttachmentPanelClick(event)}
        >
          <AccordionSummary expandIcon={<ExpandMore />}>
            <Grid container spacing={2} alignItems="center">
              <Grid item>{attachFileBadge(keywordFoundInAttachment)}</Grid>
              <Grid item>{accordionSummaryContent}</Grid>
            </Grid>
          </AccordionSummary>
          <AccordionDetails>
            <List disablePadding className={classes.attachmentListItem}>
              {attachmentsList}
            </List>
          </AccordionDetails>
        </Accordion>
      );
    return null;
  };

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
    <ListItem alignItems="flex-start" divider>
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
            {generateAttachmentList()}
            {generateItemMetadata()}
          </>
        }
        primaryTypographyProps={{ color: "primary", variant: "h6" }}
        secondaryTypographyProps={{ component: "section" }}
      />
    </ListItem>
  );
}
