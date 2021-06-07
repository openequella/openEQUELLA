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
import ExpandMore from "@material-ui/icons/ExpandMore";
import InsertDriveFile from "@material-ui/icons/InsertDriveFile";
import Search from "@material-ui/icons/Search";
import Warning from "@material-ui/icons/Warning";
import * as OEQ from "@openequella/rest-api-client";
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
  AttachmentAndViewerDefinition,
  buildLightboxNavigationHandler,
  getViewerDefinitionForAttachment,
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
  item: OEQ.Search.SearchResultItem;
  getViewerDetails: (
    mimeType: string
  ) => Promise<OEQ.MimeType.MimeTypeViewerDetail>;
  /**
   * Error handler for standard management of errors during processing - especially comms errors.
   */
  handleError: (error: Error) => void;
}

export const SearchResultAttachmentsList = ({
  item: {
    uuid,
    version,
    displayOptions,
    keywordFoundInAttachment,
    attachments = [],
  },
  getViewerDetails,
  handleError,
}: SearchResultAttachmentsListProps) => {
  const itemKey = `${uuid}/${version}`;

  const classes = useStyles();
  const inSelectionSession: boolean = isSelectionSessionOpen();
  const inSkinny = isSelectionSessionInSkinny();
  const inStructured = isSelectionSessionInStructured();

  const [attachExpanded, setAttachExpanded] = useState(
    (inSelectionSession
      ? displayOptions?.integrationOpen
      : displayOptions?.standardOpen) ?? false
  );

  const [
    attachmentsAndViewerConfigs,
    setAttachmentsAndViewerConfigs,
  ] = useState<AttachmentAndViewerConfig[]>([]);

  // In Selection Session, make each attachment draggable.
  useEffect(() => {
    if (inStructured) {
      attachmentsAndViewerConfigs.forEach(({ attachment }) => {
        prepareDraggable(attachment.id, false);
      });
    }
  }, [attachmentsAndViewerConfigs, inStructured]);

  // Responsible for determining the MIME type viewer for the provided attachments
  useEffect(() => {
    let mounted = true;
    if (!attachments.length) {
      // If there are no attachments, skip this effect
      return;
    }

    const getViewerID = async (broken: boolean, mimeType: string) => {
      let viewerDetails: OEQ.MimeType.MimeTypeViewerDetail | undefined;
      if (broken) {
        return undefined;
      }
      try {
        viewerDetails = await getViewerDetails(mimeType);
      } catch (error) {
        handleError({
          ...error,
          message: `${searchResultStrings.errors.getAttachmentViewerDetailsFailure}: ${error.message}`,
        });
      }
      return viewerDetails?.viewerId;
    };

    (async () => {
      const attachmentsAndViewerDefinitions = await Promise.all(
        attachments.map<Promise<AttachmentAndViewerDefinition>>(
          async (attachment) => {
            const { mimeType, brokenAttachment } = attachment;
            const viewerId = mimeType
              ? await getViewerID(brokenAttachment, mimeType)
              : undefined;
            return {
              attachment,
              viewerDefinition: getViewerDefinitionForAttachment(
                uuid,
                version,
                attachment,
                viewerId
              ),
            };
          }
        )
      );

      const lightboxAttachments = attachmentsAndViewerDefinitions.filter(
        ({ viewerDefinition: [viewer] }) => viewer === "lightbox"
      );

      // Transform AttachmentAndViewerDefinition to AttachmentAndViewerConfig.
      const attachmentsAndConfigs: AttachmentAndViewerConfig[] = attachmentsAndViewerDefinitions.map(
        ({ viewerDefinition: [viewer, viewUrl], attachment }) => {
          const lightboxAttachmentIndex = lightboxAttachments.findIndex(
            (a) => a.attachment.id === attachment.id
          );
          return viewer === "lightbox"
            ? {
                attachment,
                viewerConfig: {
                  viewerType: viewer,
                  config: {
                    src: viewUrl,
                    title: attachment.description,
                    mimeType: attachment.mimeType ?? "",
                    onNext: buildLightboxNavigationHandler(
                      lightboxAttachments,
                      lightboxAttachmentIndex + 1
                    ),
                    onPrevious: buildLightboxNavigationHandler(
                      lightboxAttachments,
                      lightboxAttachmentIndex - 1
                    ),
                  },
                },
              }
            : {
                attachment,
                viewerConfig: {
                  viewerType: viewer,
                  url: viewUrl,
                },
              };
        }
      );

      if (mounted) {
        setAttachmentsAndViewerConfigs(attachmentsAndConfigs);
      }
    })();

    return () => {
      // Short circuit if this component is unmounted before all its comms are done.
      mounted = false;
    };
  }, [attachments, getViewerDetails, handleError, uuid, version]);

  const handleSelectResource = (
    itemKey: string,
    attachments: string[] = []
  ) => {
    selectResource(itemKey, attachments).catch(handleError);
  };

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
          <ItemAttachmentLink selectedAttachment={attachmentAndViewerConfig}>
            <ListItemText color="primary" primary={description} />
          </ItemAttachmentLink>
          {isAttachmentSelectable(brokenAttachment) && (
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
    <Typography component="div">
      {searchResultStrings.attachments}&nbsp;&nbsp;
      <Chip label={attachments.length} size="small" color="primary" />
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

  return attachmentsList.length > 0 ? (
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
  ) : null;
};
