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
import { SyntheticEvent, useEffect, useState } from "react";
import * as React from "react";
import ItemAttachmentLink from "../../components/ItemAttachmentLink";
import { LightboxConfig } from "../../components/Lightbox";
import {
  getSearchPageAttachmentClass,
  isSelectionSessionInSkinny,
  isSelectionSessionInStructured,
  isSelectionSessionOpen,
  selectResource,
} from "../../modules/LegacySelectionSessionModule";
import {
  determineAttachmentViewUrl,
  determineViewer,
  ViewerDefinition,
} from "../../modules/ViewerModule";
import { languageStrings } from "../../util/langstrings";
import { ResourceSelector } from "./ResourceSelector";
import * as OEQ from "@openequella/rest-api-client";

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

export interface AttachmentAndViewer {
  /**
   * The details of an attachment.
   */
  attachment: OEQ.Search.Attachment;
  /**
   * Viewer information for the attachment, including which viewer to be used and the view URL.
   */
  viewer: ViewerDefinition;
}

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

  const [attachmentsAndViewers, setAttachmentsAndViewers] = useState<
    AttachmentAndViewer[]
  >([]);

  // Responsible for determining the MIME type viewer for the provided attachments
  useEffect(() => {
    let mounted = true;

    if (!attachments.length) {
      // If there are no attachments, skip this effect
      return;
    }

    const transform = async (
      attachment: OEQ.Search.Attachment
    ): Promise<AttachmentAndViewer> => {
      const {
        attachmentType,
        mimeType,
        links: { view: defaultViewUrl },
        filePath,
      } = attachment;
      const viewUrl = determineAttachmentViewUrl(
        uuid,
        version,
        attachmentType,
        defaultViewUrl,
        filePath
      );

      let viewerDetails: OEQ.MimeType.MimeTypeViewerDetail | undefined;
      try {
        viewerDetails = mimeType ? await getViewerDetails(mimeType) : undefined;
      } catch (error) {
        handleError({
          ...error,
          message: `${searchResultStrings.errors.getAttachmentViewerDetailsFailure}: ${error.message}`,
        });
      }

      return {
        attachment: attachment,
        viewer: determineViewer(
          attachmentType,
          viewUrl,
          mimeType,
          viewerDetails?.viewerId
        ),
      };
    };

    (async () => {
      const attachmentsAndViewers = await Promise.all(
        attachments.map<Promise<AttachmentAndViewer>>(transform)
      );
      if (mounted) {
        setAttachmentsAndViewers(attachmentsAndViewers);
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

  const lightboxAttachments = attachmentsAndViewers.filter(
    (av) => av.viewer[0] === "lightbox"
  );
  const buildLightboxNavigationHandler = (
    anotherAttachmentIndex: number
  ): (() => LightboxConfig) | undefined => {
    if (
      anotherAttachmentIndex > -1 &&
      anotherAttachmentIndex < lightboxAttachments.length
    ) {
      const previousAttachment = lightboxAttachments[anotherAttachmentIndex];
      return () => ({
        attachment: {
          src: previousAttachment.viewer[1],
          title: previousAttachment.attachment.description,
          mimeType: previousAttachment.attachment.mimeType ?? "",
        },
        onNext: buildLightboxNavigationHandler(anotherAttachmentIndex + 1),
        onPrevious: buildLightboxNavigationHandler(anotherAttachmentIndex - 1),
      });
    }

    return;
  };

  const attachmentsList = attachmentsAndViewers.map(
    (attachmentAndViewer: AttachmentAndViewer) => {
      const {
        attachment: { id, description },
      } = attachmentAndViewer;
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
            selectedAttachment={attachmentAndViewer}
            // Need the index in 'lightboxAttachments' to make sure the next or previous attachment must be viewable in Lightbox.
            onPrevious={buildLightboxNavigationHandler(
              lightboxAttachments.findIndex((v) => v.attachment.id === id) - 1
            )}
            onNext={buildLightboxNavigationHandler(
              lightboxAttachments.findIndex((v) => v.attachment.id === id) + 1
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
              const attachments = attachmentsAndViewers.map(
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
