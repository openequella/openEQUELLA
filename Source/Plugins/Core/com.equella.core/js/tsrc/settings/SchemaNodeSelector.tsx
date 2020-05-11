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
import * as React from "react";
import { TreeItem, TreeView } from "@material-ui/lab";
import { Add, Remove } from "@material-ui/icons";

interface SchemaNodeSelectorProps {
  /**
   * The schema tree this component will display. Made up of SchemaNodes objects which contain an id, a label and child SchemaNodes.
   */
  tree: SchemaNode;
  /**
   * Function that gets called upon selection of a node.
   * @param node The id of the selected node.
   */
  setSelectedNode: (node: string) => void;
}

export interface SchemaNode {
  /**
   * The ID of the node.
   */
  id: string;
  /**
   * The title of the node.
   */
  label: string;
  /**
   * Zero, one or multiple SchemaNodes as children of this node.
   */
  children?: SchemaNode[];
}

const renderTree = (nodes: SchemaNode) => {
  return (
    <TreeItem key={nodes.id} nodeId={nodes.id} label={nodes.label}>
      {Array.isArray(nodes.children)
        ? nodes.children.map((node) => renderTree(node))
        : null}
    </TreeItem>
  );
};
/**
 * This component defines a schema node selector, for the display of a schema and selection of its nodes. The schema itself passed into this should be of type SchemaNode.
 */
export default function SchemaNodeSelector({
  setSelectedNode,
  tree,
}: SchemaNodeSelectorProps) {
  return (
    <TreeView
      defaultExpandIcon={<Add />}
      defaultCollapseIcon={<Remove />}
      onNodeSelect={(event: React.ChangeEvent<{}>, nodeIds: string) => {
        setSelectedNode(nodeIds);
      }}
    >
      {renderTree(tree)}
    </TreeView>
  );
}
