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
import Share from "@mui/icons-material/Share";
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Badge,
  Chip,
  Grid,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemSecondaryAction,
  ListItemText,
  Typography,
} from "@mui/material";

import { styled } from "@mui/material/styles";
import Tooltip from "@mui/material/Tooltip";
import AttachFile from "@mui/icons-material/AttachFile";
import DragIndicatorIcon from "@mui/icons-material/DragIndicator";
import ErrorIcon from "@mui/icons-material/Error";
import ExpandMore from "@mui/icons-material/ExpandMore";
import InsertDriveFile from "@mui/icons-material/InsertDriveFile";
import Search from "@mui/icons-material/Search";
import Warning from "@mui/icons-material/Warning";
import { Skeleton } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as NEA from "fp-ts/NonEmptyArray";
import * as O from "fp-ts/Option";
import { not } from "fp-ts/Predicate";
import * as React from "react";
import { SyntheticEvent, useEffect, useState } from "react";
import {
  buildEmbedCode,
  ShareAttachment,
} from "../../components/embedattachment/EmbeddedAttachmentHelper";
import ItemAttachmentLink from "../../components/ItemAttachmentLink";
import { ShareAttachmentDialog } from "../../components/ShareAttachmentDialog";
import { TooltipIconButton } from "../../components/TooltipIconButton";
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
  determineAttachmentViewUrl,
} from "../../modules/ViewerModule";
import { languageStrings } from "../../util/langstrings";
import { ResourceSelector } from "./ResourceSelector";

const PREFIX = "SearchResultAttachmentsList";

const classes = {
  nested: `${PREFIX}-nested`,
  attachmentExpander: `${PREFIX}-attachmentExpander`,
  attachmentBadge: `${PREFIX}-attachmentBadge`,
  attachmentListItem: `${PREFIX}-attachmentListItem`,
};

const StyledAccordion = styled(Accordion)(({ theme }) => ({
  [`& .${classes.nested}`]: {
    paddingLeft: theme.spacing(4),
  },

  [`&.${classes.attachmentExpander}`]: {
    marginTop: theme.spacing(2),
    marginBottom: theme.spacing(2),
  },

  [`& .${classes.attachmentBadge}`]: {
    backgroundColor: theme.palette.background.paper,
    color: theme.palette.secondary.main,
    borderRadius: "50%",
  },

  [`& .${classes.attachmentListItem}`]: {
    width: "100%",
  },
}));

const {
  searchResult: searchResultStrings,
  selectResource: selectResourceStrings,
} = languageStrings.searchpage;

export interface SearchResultAttachmentsListProps {
  /**
   * The item to display attachments for.
   */
  item: OEQ.Search.SearchResultItem;
  /**
   * A function which can provide Viewer Details for attachments.
   */
  getViewerDetails: (
    mimeType: string,
  ) => Promise<OEQ.MimeType.MimeTypeViewerDetail>;
  /**
   * A function which can retrieve attachments for a specified item.
   */
  getItemAttachments: (
    uuid: string,
    version: number,
  ) => Promise<OEQ.Search.Attachment[]>;
  /**
   * `true` if the Item which the attachments belong to is live.
   */
  isItemLive: boolean;
}

export const SearchResultAttachmentsList = ({
  item,
  getViewerDetails,
  getItemAttachments,
  isItemLive,
}: SearchResultAttachmentsListProps) => {
  const {
    uuid,
    version,
    displayOptions,
    keywordFoundInAttachment,
    attachmentCount,
  } = item;
  const itemKey = `${uuid}/${version}`;

  const inSelectionSession: boolean = isSelectionSessionOpen();
  const inSkinny = isSelectionSessionInSkinny();
  const inStructured = isSelectionSessionInStructured();

  const [attachExpanded, setAttachExpanded] = useState<boolean>(
    (inSelectionSession
      ? displayOptions?.integrationOpen
      : displayOptions?.standardOpen) ?? false,
  );

  const [attachmentToShare, setAttachmentToShare] = useState<
    O.Option<ShareAttachment>
  >(O.none);
  const [attachmentsAndViewerConfigs, setAttachmentsAndViewerConfigs] =
    useState<AttachmentAndViewerConfig[]>([]);

  const [error, setError] = useState<Error>();

  // In Selection Session, make each intact attachment draggable.
  useEffect(() => {
    if (isItemLive && inStructured) {
      attachmentsAndViewerConfigs
        .filter(({ attachment }) => !attachment.brokenAttachment)
        .forEach(({ attachment }) => {
          prepareDraggable(attachment.id, false);
        });
    }
  }, [attachmentsAndViewerConfigs, inStructured, isItemLive]);

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
          version,
        );

        const attachmentsAndViewerDefinitions =
          await buildViewerConfigForAttachments(
            item,
            attachments,
            viewerDetails,
          );
        if (mounted) {
          setAttachmentsAndViewerConfigs(attachmentsAndViewerDefinitions);
        }
      } catch (error) {
        setError(
          error instanceof Error
            ? error
            : new Error(`${stringGetViewerDetailsFailure}: ${error}`),
        );
      }
    })();

    return () => {
      // Short circuit if this component is unmounted before all its comms are done.
      mounted = false;
    };
  }, [
    item,
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

  // Handler for sharing an Attachment.
  const onShare = ({
    attachmentType,
    description,
    mimeType,
    filePath,
    links: { view },
  }: OEQ.Search.Attachment) => {
    const src: string = determineAttachmentViewUrl(
      uuid,
      version,
      attachmentType,
      view,
      filePath,
    );
    const embedCode: O.Option<string> = pipe(
      mimeType,
      O.fromNullable,
      O.flatMap((m) => buildEmbedCode(m, src, description)),
    );

    setAttachmentToShare(O.of({ src, embedCode }));
  };

  const buildIcon = (broken: boolean) => {
    if (broken) {
      return (
        <Tooltip title={languageStrings.searchpage.deadAttachmentWarning}>
          <Warning color="secondary" />
        </Tooltip>
      );
    }

    // Only live Items and their attachments can be dragged in Selection Session structure mode, so show 'DragIndicatorIcon'.
    // For other situations, show 'InsertDriveFile'.
    return isItemLive && inStructured ? (
      <DragIndicatorIcon />
    ) : (
      <InsertDriveFile />
    );
  };

  const isAttachmentSelectable = (broken: boolean) =>
    inSelectionSession && isItemLive && !broken;

  const buildSkeletonList = (howMany: number): React.JSX.Element[] =>
    pipe(
      NEA.range(1, howMany),
      NEA.map((id) => (
        <ListItem key={id}>
          <ListItemIcon>
            <Skeleton
              variant="rectangular"
              width={24}
              height={24}
              animation="wave"
            />
          </ListItemIcon>
          <ListItemText>
            <Skeleton variant="text" animation="wave" />
          </ListItemText>
        </ListItem>
      )),
    );

  const attachmentsList = attachmentsAndViewerConfigs.map(
    (attachmentAndViewerConfig: AttachmentAndViewerConfig) => {
      const { attachment } = attachmentAndViewerConfig;
      const { id, description, brokenAttachment } = attachment;

      return (
        <ListItemButton
          onClick={(event) => {
            if (brokenAttachment) {
              event.stopPropagation();
            }
          }}
          key={id}
          id={id}
          // Give a class so each attachment can be dropped to the course list.
          className={`${classes.nested} ${getSearchPageAttachmentClass()}`}
          // These 'data-xx' attributes are used in the 'dropCallBack' of 'courselist.js'.
          data-itemuuid={uuid}
          data-itemversion={version}
          data-attachmentuuid={id}
        >
          <ListItemIcon>{buildIcon(brokenAttachment)}</ListItemIcon>
          <ItemAttachmentLink selectedAttachment={attachmentAndViewerConfig}>
            <ListItemText color="primary" primary={description} />
          </ItemAttachmentLink>
          <ListItemSecondaryAction>
            {isAttachmentSelectable(brokenAttachment) && (
              <ResourceSelector
                labelText={selectResourceStrings.attachment}
                isStopPropagation
                onClick={() => selectResource(itemKey, [id])}
              />
            )}
            <TooltipIconButton
              title={languageStrings.common.action.share}
              onClick={(event) => {
                event.stopPropagation();
                onShare(attachment);
              }}
            >
              <Share />
            </TooltipIconButton>
          </ListItemSecondaryAction>
        </ListItemButton>
      );
    },
  );

  const buildErrorListItem = (e: Error) => (
    <ListItem>
      <ListItemIcon>
        <ErrorIcon color="secondary" />
      </ListItemIcon>
      <ListItemText color="primary" primary={e.message} />
    </ListItem>
  );

  const buildAttachmentList = (): React.JSX.Element => (
    <List disablePadding className={classes.attachmentListItem}>
      {error
        ? buildErrorListItem(error)
        : pipe(
            attachmentsList,
            O.fromPredicate(not(A.isEmpty)),
            O.getOrElse(() => buildSkeletonList(attachmentCount)),
          )}
    </List>
  );

  const accordionText = (
    <Typography component="div">
      {searchResultStrings.attachments}&nbsp;&nbsp;
      <Chip label={attachmentCount} size="small" color="primary" />
    </Typography>
  );

  // Only show the Select All Attachments button if at least one attachment is not dead
  const atLeastOneIntactAttachment = attachmentsAndViewerConfigs.some(
    ({ attachment }) => !attachment.brokenAttachment,
  );

  const accordionSummaryContent = inSelectionSession ? (
    <Grid container alignItems="center">
      <Grid>{accordionText}</Grid>
      <Grid>
        {isItemLive && atLeastOneIntactAttachment && !inSkinny && (
          <ResourceSelector
            labelText={selectResourceStrings.allAttachments}
            isStopPropagation
            onClick={() => {
              const attachments = attachmentsAndViewerConfigs
                .filter(
                  // filter out dead attachments from select all function
                  ({ attachment }) => !attachment.brokenAttachment,
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

  const shareAttachmentDialog = pipe(
    attachmentToShare,
    O.map(({ src, embedCode }) => (
      <ShareAttachmentDialog
        open
        onCloseDialog={() => setAttachmentToShare(O.none)}
        src={src}
        embedCode={embedCode}
      />
    )),
    O.toUndefined,
  );

  return attachmentCount > 0 ? (
    <>
      <StyledAccordion
        id={`attachments-list-${uuid}:${version}`}
        className={classes.attachmentExpander}
        expanded={attachExpanded}
        onClick={(event) => handleAttachmentPanelClick(event)}
      >
        <AccordionSummary expandIcon={<ExpandMore />}>
          <Grid container spacing={2} alignItems="center">
            <Grid>{attachFileBadge(keywordFoundInAttachment)}</Grid>
            <Grid>{accordionSummaryContent}</Grid>
          </Grid>
        </AccordionSummary>
        <AccordionDetails>
          {attachExpanded && buildAttachmentList()}
        </AccordionDetails>
      </StyledAccordion>
      {shareAttachmentDialog}
    </>
  ) : null;
};
