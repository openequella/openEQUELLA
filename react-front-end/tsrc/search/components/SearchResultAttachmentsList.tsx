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
  Chip,
  Grid,
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
import DragIndicatorIcon from "@material-ui/icons/DragIndicator";
import ErrorIcon from "@material-ui/icons/Error";
import ExpandMore from "@material-ui/icons/ExpandMore";
import InsertDriveFile from "@material-ui/icons/InsertDriveFile";
import Search from "@material-ui/icons/Search";
import Warning from "@material-ui/icons/Warning";
import { Skeleton } from "@material-ui/lab";
import * as OEQ from "@openequella/rest-api-client";
import { pipe } from "fp-ts/function";
import * as NEA from "fp-ts/NonEmptyArray";
import * as React from "react";
import { SyntheticEvent, useEffect, useState } from "react";
import ItemAttachmentLink from "../../components/ItemAttachmentLink";
import {
  getSearchPageAttachmentClass,
  isSelectionSessionInSkinny,
  isSelectionSessionInStructured,
  isSelectionSessionOpen,
  prepareDraggable,
  selectResource,
} from "../../modules/LegacySelectionSessionModule";
import {
  AttachmentAndViewerConfig,
  buildViewerConfigForAttachments,
} from "../../modules/ViewerModule";
import { languageStrings } from "../../util/langstrings";
import { ResourceSelector } from "./ResourceSelector";

const {
  searchResult: searchResultStrings,
  selectResource: selectResourceStrings,
} = languageStrings.searchpage;

const useStyles = makeStyles((theme: Theme) => ({
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
}));

export interface SearchResultAttachmentsListProps {
  /**
   * The item to display attachments for.
   */
  item: OEQ.Search.SearchResultItem;
  /**
   * A function which can provide Viewer Details for attachments.
   */
  getViewerDetails: (
    mimeType: string
  ) => Promise<OEQ.MimeType.MimeTypeViewerDetail>;
  /**
   * A function which can retrieve attachments for a specified item.
   */
  getItemAttachments: (
    uuid: string,
    version: number
  ) => Promise<OEQ.Search.Attachment[]>;
}

export const SearchResultAttachmentsList = ({
  item: {
    uuid,
    version,
    displayOptions,
    keywordFoundInAttachment,
    attachmentCount,
  },
  getViewerDetails,
  getItemAttachments,
}: SearchResultAttachmentsListProps) => {
  const itemKey = `${uuid}/${version}`;

  const classes = useStyles();
  const inSelectionSession: boolean = isSelectionSessionOpen();
  const inSkinny = isSelectionSessionInSkinny();
  const inStructured = isSelectionSessionInStructured();

  const [attachExpanded, setAttachExpanded] = useState<boolean>(
    (inSelectionSession
      ? displayOptions?.integrationOpen
      : displayOptions?.standardOpen) ?? false
  );

  const [attachmentsAndViewerConfigs, setAttachmentsAndViewerConfigs] =
    useState<AttachmentAndViewerConfig[]>([]);

  const [error, setError] = useState<Error>();

  // In Selection Session, make each intact attachment draggable.
  useEffect(() => {
    if (inStructured) {
      attachmentsAndViewerConfigs
        .filter(({ attachment }) => !attachment.brokenAttachment)
        .forEach(({ attachment }) => {
          prepareDraggable(attachment.id, false);
        });
    }
  }, [attachmentsAndViewerConfigs, inStructured]);

  // Responsible for retrieving the attachments and then determining the MIME type viewer for each.
  useEffect(() => {
    let mounted = true;
    if (!attachExpanded) {
      // clear any previous errors on collapse of attachments
      setError(undefined);

      return;
    } else if (attachmentCount < 1 || attachmentsAndViewerConfigs.length > 0) {
      // If there are no attachments or if they've already been processed, skip this effect
      return;
    }

    const stringGetViewerDetailsFailure =
      languageStrings.searchpage.searchResult.errors
        .getAttachmentViewerDetailsFailure;

    // A wrapper for 'getViewerDetails' to ensure it returns a rejected promise with proper error message
    // rather than throws an error.
    const viewerDetails = async (mimeType: string) => {
      try {
        return getViewerDetails(mimeType);
      } catch (error) {
        const cause =
          error instanceof Error ? error.message : (error as string);
        throw new Error(`${stringGetViewerDetailsFailure}: ${cause}`);
      }
    };

    (async () => {
      try {
        const attachments: OEQ.Search.Attachment[] = await getItemAttachments(
          uuid,
          version
        );

        const attachmentsAndViewerDefinitions =
          await buildViewerConfigForAttachments(
            attachments,
            uuid,
            version,
            viewerDetails
          );
        if (mounted) {
          setAttachmentsAndViewerConfigs(attachmentsAndViewerDefinitions);
        }
      } catch (error) {
        setError(
          error instanceof Error
            ? error
            : new Error(`${stringGetViewerDetailsFailure}: ${error}`)
        );
      }
    })();

    return () => {
      // Short circuit if this component is unmounted before all its comms are done.
      mounted = false;
    };
  }, [
    attachExpanded,
    attachmentCount,
    attachmentsAndViewerConfigs.length,
    getItemAttachments,
    getViewerDetails,
    setError,
    uuid,
    version,
  ]);

  const handleAttachmentPanelClick = (event: SyntheticEvent) => {
    /** prevents the SearchResult onClick from firing when attachment panel is clicked */
    event.stopPropagation();
    setAttachExpanded(!attachExpanded);
  };

  const buildIcon = (broken: boolean) => {
    if (broken) {
      return (
        <Tooltip title={languageStrings.searchpage.deadAttachmentWarning}>
          <Warning color="secondary" />
        </Tooltip>
      );
    }
    return inStructured ? <DragIndicatorIcon /> : <InsertDriveFile />;
  };

  const isAttachmentSelectable = (broken: boolean) =>
    inSelectionSession && !broken;

  const buildSkeletonList = (howMany: number): JSX.Element[] =>
    pipe(
      NEA.range(1, howMany),
      NEA.map((id) => (
        <ListItem key={id}>
          <ListItemIcon>
            <Skeleton variant="rect" width={24} height={24} animation="wave" />
          </ListItemIcon>
          <ListItemText>
            <Skeleton variant="text" animation="wave" />
          </ListItemText>
        </ListItem>
      ))
    );

  const attachmentsList = attachmentsAndViewerConfigs.map(
    (attachmentAndViewerConfig: AttachmentAndViewerConfig) => {
      const {
        attachment: { id, description, brokenAttachment },
      } = attachmentAndViewerConfig;

      return (
        <ListItem
          onClick={(event) => {
            if (brokenAttachment) {
              event.stopPropagation();
            }
          }}
          key={id}
          id={id}
          button
          className={`${classes.nested} ${getSearchPageAttachmentClass()}`} // Give a class so each attachment can be dropped to the course list.
          data-itemuuid={uuid} // These 'data-xx' attributes are used in the 'dropCallBack' of 'courselist.js'.
          data-itemversion={version}
          data-attachmentuuid={id}
        >
          <ListItemIcon>{buildIcon(brokenAttachment)}</ListItemIcon>
          <ItemAttachmentLink
            selectedAttachment={attachmentAndViewerConfig}
            item={{
              uuid,
              version,
            }}
          >
            <ListItemText color="primary" primary={description} />
          </ItemAttachmentLink>
          {isAttachmentSelectable(brokenAttachment) && (
            <ListItemSecondaryAction>
              <ResourceSelector
                labelText={selectResourceStrings.attachment}
                isStopPropagation
                onClick={() => selectResource(itemKey, [id])}
              />
            </ListItemSecondaryAction>
          )}
        </ListItem>
      );
    }
  );

  const buildErrorListItem = (e: Error) => (
    <ListItem>
      <ListItemIcon>
        <ErrorIcon color="secondary" />
      </ListItemIcon>
      <ListItemText color="primary" primary={e.message} />
    </ListItem>
  );

  const buildAttachmentList = (): JSX.Element => {
    const items =
      attachmentsList.length > 0
        ? attachmentsList
        : buildSkeletonList(attachmentCount);

    return (
      <List disablePadding className={classes.attachmentListItem}>
        {error ? buildErrorListItem(error) : items}
      </List>
    );
  };

  const accordionText = (
    <Typography component="div">
      {searchResultStrings.attachments}&nbsp;&nbsp;
      <Chip label={attachmentCount} size="small" color="primary" />
    </Typography>
  );

  // Only show the Select All Attachments button if at least one attachment is not dead
  const atLeastOneIntactAttachment = attachmentsAndViewerConfigs.some(
    ({ attachment }) => !attachment.brokenAttachment
  );

  const showSelectAllAttachments = atLeastOneIntactAttachment && !inSkinny;

  const accordionSummaryContent = inSelectionSession ? (
    <Grid container alignItems="center">
      <Grid item>{accordionText}</Grid>
      <Grid>
        {showSelectAllAttachments && (
          <ResourceSelector
            labelText={selectResourceStrings.allAttachments}
            isStopPropagation
            onClick={() => {
              const attachments = attachmentsAndViewerConfigs
                .filter(
                  // filter out dead attachments from select all function
                  ({ attachment }) => !attachment.brokenAttachment
                )
                .map(({ attachment }) => attachment.id);
              return selectResource(itemKey, attachments);
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
      overlap="circular"
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

  return attachmentCount > 0 ? (
    <Accordion
      id={`attachments-list-${uuid}:${version}`}
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
        {attachExpanded && buildAttachmentList()}
      </AccordionDetails>
    </Accordion>
  ) : null;
};
