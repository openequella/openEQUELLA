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
import { Divider } from "@mui/material";
import { TreeView } from "@mui/x-tree-view/TreeView";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as React from "react";
import { Fragment } from "react";
import { languageStrings } from "../../util/langstrings";
import HierarchyTopic, { HierarchyTopicBasicProps } from "./HierarchyTopic";

const viewHierarchyText = languageStrings.hierarchy.viewHierarchy;

export interface HierarchyTreeProps extends HierarchyTopicBasicProps {
  /**
   * Hierarchy topic summaries which represents all nodes in the tree.
   */
  hierarchies: OEQ.BrowseHierarchy.HierarchyTopicSummary[];
}

/**
 * A tree view of all provided Hierarchy Topic Summary with expandable nodes to show sub topics with optional title and short description.
 */
const HierarchyTree = ({
  hierarchies,
  onlyShowTitle,
  customActionBuilder,
  disableTitleLink,
}: HierarchyTreeProps) => {
  const [expanded, setExpanded] = React.useState<string[]>([]);

  const handleToggle = (event: React.SyntheticEvent, nodeIds: string[]) =>
    setExpanded(nodeIds);

  const topics = pipe(
    hierarchies,
    A.map((topic) => (
      <HierarchyTopic
        key={topic.compoundUuid}
        topic={topic}
        expandedNodes={expanded}
        onlyShowTitle={onlyShowTitle}
        disableTitleLink={disableTitleLink}
        customActionBuilder={customActionBuilder}
      />
    )),
    A.intersperse(<Divider />),
    A.mapWithIndex((index, component) => {
      return <Fragment key={index}> {component} </Fragment>;
    }),
  );

  return (
    <TreeView
      aria-label={viewHierarchyText}
      expanded={expanded}
      onNodeToggle={handleToggle}
    >
      {topics}
    </TreeView>
  );
};

export default HierarchyTree;
