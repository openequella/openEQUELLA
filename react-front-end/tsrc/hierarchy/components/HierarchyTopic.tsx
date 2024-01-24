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
import { ExpandLess } from "@mui/icons-material";
import ExpandMore from "@mui/icons-material/ExpandMore";
import TopicOutlinedIcon from "@mui/icons-material/TopicOutlined";
import {
  Badge,
  Collapse,
  ListItem,
  ListItemAvatar,
  ListItemText,
} from "@mui/material";
import { styled } from "@mui/material/styles";
import { TreeItem, treeItemClasses } from "@mui/x-tree-view/TreeItem";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import HTMLReactParser from "html-react-parser";
import * as React from "react";
import { Link } from "react-router-dom";

export interface HierarchyTopicProps {
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
  name: `${PREFIX}-name`,
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
  [`& .${classes.name}`]: {
    textDecoration: "none",
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
  },
  expandedNodes,
}: HierarchyTopicProps): React.JSX.Element => {
  const isExpanded = expandedNodes.includes(compoundUuid);

  const expandIcon = () => (isExpanded ? <ExpandLess /> : <ExpandMore />);

  const itemLabel = () => (
    // Because it's parent TreeItem is using <li> as well.
    // Use div to avoid error: "<li> cannot appear as a descendant of <li>".
    <ListItem className={classes.label} component="div">
      <ListItemAvatar>
        <Badge badgeContent={matchingItemCount} color="primary">
          <TopicOutlinedIcon color="primary" />
        </Badge>
      </ListItemAvatar>
      <ListItemText
        primary={
          /*TODO: This should be a link to the hierarchy page*/
          <Link className={classes.name} to="">
            {name}
          </Link>
        }
        // Use `div` instead of default tag `p` to avoid HTML semantic error.
        secondaryTypographyProps={{ component: "div" }}
        // The short description field support raw html syntax.
        secondary={shortDescription && HTMLReactParser(shortDescription)}
      />
      {A.isNonEmpty(subHierarchyTopics) && expandIcon()}
    </ListItem>
  );

  return (
    <StyledTreeItem label={itemLabel()} nodeId={compoundUuid}>
      <Collapse in={isExpanded} timeout="auto" unmountOnExit>
        {subHierarchyTopics.map((subTopic) => (
          <HierarchyTopic
            key={subTopic.compoundUuid}
            topic={subTopic}
            expandedNodes={expandedNodes}
          />
        ))}
      </Collapse>
    </StyledTreeItem>
  );
};

export default HierarchyTopic;
