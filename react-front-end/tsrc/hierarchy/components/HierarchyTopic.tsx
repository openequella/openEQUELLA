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
  Box,
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
import { useEffect, useState } from "react";
import * as React from "react";
import { OEQLink } from "../../components/OEQLink";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { routes } from "../../mainui/routes";
import { getSubHierarchies } from "../../modules/HierarchyModule";
import { buildSelectionSessionHierarchyLink } from "../../modules/LegacySelectionSessionModule";
import { languageStrings } from "../../util/langstrings";
import HierarchyTopicSkeleton from "./HierarchyTopicSkeleton";

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
  [`& .${treeItemClasses.groupTransition}`]: {
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
    hasSubTopic,
    hideSubtopicsWithNoResults,
    showResults,
  },
  expandedNodes,
  onlyShowTitle = false,
  disableTitleLink = false,
  customActionBuilder,
}: HierarchyTopicProps): React.JSX.Element => {
  const isExpanded = expandedNodes.includes(compoundUuid);
  const [subHierarchyTopics, setSubHierarchyTopics] =
    useState<OEQ.BrowseHierarchy.HierarchyTopicSummary[]>();
  const [isLoading, setIsLoading] = useState(false);
  const [showSkeleton, setShowSkeleton] = useState(false);

  // If the request loading time exceeds 100ms, show the skeleton.
  // Reference: https://design.gitlab.com/components/spinner/#behavior
  useEffect(() => {
    const timer = setTimeout(() => {
      if (isLoading) {
        setShowSkeleton(true);
      }
    }, 100);

    return () => {
      setShowSkeleton(false);
      clearTimeout(timer);
    };
  }, [isLoading]);

  const subTopicsLoaded =
    subHierarchyTopics !== undefined && A.isNonEmpty(subHierarchyTopics);

  const handleExpandTopic = async () => {
    if (hasSubTopic && subHierarchyTopics === undefined && !isLoading) {
      setIsLoading(true);
      const subTopics = await getSubHierarchies(compoundUuid);

      const filteredSubTopics = hideSubtopicsWithNoResults
        ? pipe(
            subTopics,
            A.filter((subTopic) => subTopic.matchingItemCount > 0),
          )
        : subTopics;

      setSubHierarchyTopics(filteredSubTopics);

      setIsLoading(false);
    }
  };

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
    >
      <ListItemAvatar>
        <FolderIcon className={classes.icon} />
      </ListItemAvatar>
      <ListItemText
        primary={
          <>
            {itemTitle()}
            {showResults && (
              <span className={classes.count}>({matchingItemCount})</span>
            )}
          </>
        }
        // Use `div` instead of default tag `p` to avoid HTML semantic error.
        slotProps={{ secondary: { component: "div" } }}
        // The short description field support raw html syntax.
        secondary={
          !onlyShowTitle &&
          shortDescription &&
          HTMLReactParser(shortDescription)
        }
      />
      {/* Use FlexShrink to avoid the influence of long title */}
      <Box sx={{ flexShrink: 0 }}>
        {hasSubTopic && expandIcon()}
        {customActionBuilder?.(compoundUuid)}
      </Box>
    </ListItem>
  );

  return (
    <StyledTreeItem
      label={itemLabel()}
      itemId={compoundUuid}
      onClick={handleExpandTopic}
    >
      <Collapse in={isExpanded} timeout="auto" unmountOnExit>
        {showSkeleton ? (
          <HierarchyTopicSkeleton />
        ) : (
          subTopicsLoaded &&
          subHierarchyTopics.map((subTopic) => (
            <HierarchyTopic
              key={subTopic.compoundUuid}
              topic={subTopic}
              expandedNodes={expandedNodes}
              onlyShowTitle={onlyShowTitle}
              disableTitleLink={disableTitleLink}
              customActionBuilder={customActionBuilder}
            />
          ))
        )}
      </Collapse>
    </StyledTreeItem>
  );
};

export default HierarchyTopic;
