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
import ExpandLess from "@mui/icons-material/ExpandLess";
import ExpandMore from "@mui/icons-material/ExpandMore";
import FolderIcon from "@mui/icons-material/Folder";
import {
  Collapse,
  ListItem,
  ListItemAvatar,
  ListItemText,
} from "@mui/material";
import { styled } from "@mui/material/styles";
import { TreeItem, treeItemClasses } from "@mui/x-tree-view/TreeItem";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import HTMLReactParser from "html-react-parser";
import * as React from "react";
import { OEQLink } from "../../components/OEQLink";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { routes } from "../../mainui/routes";
import { buildSelectionSessionHierarchyLink } from "../../modules/LegacySelectionSessionModule";
import { languageStrings } from "../../util/langstrings";

const { expandHierarchy: expandText, collapseHierarchy: collapseText } =
  languageStrings.hierarchy;

/**
 * Shared props for both Hierarchy Topic and Hierarchy Tree.
 */
export interface HierarchyTopicBasicProps {
  /** `true` to display the title only. */
  onlyShowTitle?: boolean;
  /** `true` to display the title as plain text. */
  disableTitleLink?: boolean;
  /** Custom action button to be displayed on the right side of the topic. */
  customActionBuilder?: (hierarchyCompoundUuid: string) => React.ReactNode;
}

export interface HierarchyTopicProps extends HierarchyTopicBasicProps {
  /**
   * Hierarchy topic summary which represents this node.
   */
  topic: OEQ.BrowseHierarchy.HierarchyTopicSummary;
  /**
   * List of expanded nodes.
   */
  expandedNodes: string[];
}

const PREFIX = "HierarchyTopic";
export const classes = {
  label: `${PREFIX}-label`,
  icon: `${PREFIX}-icon`,
  name: `${PREFIX}-name`,
  count: `${PREFIX}-count`,
  description: `${PREFIX}-description`,
};

export const StyledTreeItem = styled(TreeItem)(({ theme }) => ({
  // hide tree expand icon
  [`& .${treeItemClasses.content} .${treeItemClasses.iconContainer}`]: {
    width: 0,
  },
  [`& .${treeItemClasses.group}`]: {
    marginLeft: theme.spacing(3),
    paddingLeft: theme.spacing(4),
  },
  [`& .${classes.label}`]: {
    padding: theme.spacing(1, 0),
  },
  [`& .${classes.icon}`]: {
    color: theme.palette.text.secondary,
  },
  [`& .${classes.count}`]: {
    marginLeft: theme.spacing(1),
    color: theme.palette.text.secondary,
  },
  [`& .${classes.description}`]: {
    marginLeft: theme.spacing(2),
  },
}));

/**
 * Each Hierarchy Topic Represents an item in the hierarchy tree.
 * And it can have sub topics.
 */
const HierarchyTopic = ({
  topic: {
    name,
    matchingItemCount,
    compoundUuid,
    shortDescription,
    subHierarchyTopics,
    hideSubtopicsWithNoResults,
  },
  expandedNodes,
  onlyShowTitle = false,
  disableTitleLink = false,
  customActionBuilder,
}: HierarchyTopicProps): React.JSX.Element => {
  const isExpanded = expandedNodes.includes(compoundUuid);

  const filteredSubTopics = hideSubtopicsWithNoResults
    ? pipe(
        subHierarchyTopics,
        A.filter((subTopic) => subTopic.matchingItemCount > 0),
      )
    : subHierarchyTopics;

  const expandIcon = () =>
    isExpanded ? (
      <TooltipIconButton title={collapseText}>
        <ExpandLess />
      </TooltipIconButton>
    ) : (
      <TooltipIconButton title={expandText}>
        <ExpandMore />
      </TooltipIconButton>
    );

  const itemTitle = () => {
    const title = name ?? compoundUuid;

    return disableTitleLink ? (
      title
    ) : (
      <OEQLink
        muiLinkUrlProvider={() =>
          buildSelectionSessionHierarchyLink(compoundUuid)
        }
        routeLinkUrlProvider={() => routes.Hierarchy.to(compoundUuid)}
      >
        {title}
      </OEQLink>
    );
  };

  const itemLabel = () => (
    // Because it's parent TreeItem is using <li> as well.
    // Use div to avoid error: "<li> cannot appear as a descendant of <li>".
    <ListItem
      data-testid={compoundUuid}
      className={classes.label}
      component="div"
      secondaryAction={
        <>
          {A.isNonEmpty(filteredSubTopics) && expandIcon()}
          {customActionBuilder?.(compoundUuid)}
        </>
      }
    >
      <ListItemAvatar>
        <FolderIcon className={classes.icon} />
      </ListItemAvatar>
      <ListItemText
        primary={
          <>
            {itemTitle()}
            <span className={classes.count}>({matchingItemCount})</span>
          </>
        }
        // Use `div` instead of default tag `p` to avoid HTML semantic error.
        secondaryTypographyProps={{ component: "div" }}
        // The short description field support raw html syntax.
        secondary={
          !onlyShowTitle &&
          shortDescription &&
          HTMLReactParser(shortDescription)
        }
      />
    </ListItem>
  );

  return (
    <StyledTreeItem label={itemLabel()} nodeId={compoundUuid}>
      <Collapse in={isExpanded} timeout="auto" unmountOnExit>
        {filteredSubTopics.map((subTopic) => (
          <HierarchyTopic
            key={subTopic.compoundUuid}
            topic={subTopic}
            expandedNodes={expandedNodes}
            onlyShowTitle={onlyShowTitle}
            disableTitleLink={disableTitleLink}
            customActionBuilder={customActionBuilder}
          />
        ))}
      </Collapse>
    </StyledTreeItem>
  );
};

export default HierarchyTopic;
