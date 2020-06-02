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
import { pathForNode, SchemaNode } from "../schema/SchemaModule";

interface SchemaNodeSelectorProps {
  /**
   * The schema tree this component will display. Made up of SchemaNodes objects which contain an id, a label and child SchemaNodes.
   */
  tree: SchemaNode | undefined;
  /**
   * Function that gets called upon selection of a node.
   * @param node The path of the selected node.
   */
  setSelectedNode: (node: string) => void;
}
const getAllPaths = (nodes: SchemaNode, paths: string[]) => {
  paths.push(pathForNode(nodes, false));
  nodes.children?.forEach((childNode) => {
    paths.concat(getAllPaths(childNode, paths));
  });
  return paths;
};
const renderTree = (nodes: SchemaNode) => {
  return (
    <TreeItem
      key={nodes.name}
      nodeId={pathForNode(nodes, false)}
      label={nodes.name}
    >
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
  const [selectedNode, setSelected] = React.useState("");
  const [expanded, setExpanded] = React.useState<string[]>([]);
  const [renderedTree, setRenderedTree] = React.useState<JSX.Element>(<div />);
  React.useEffect(() => {
    if (tree !== undefined) {
      setRenderedTree(renderTree(tree));
      setExpanded(getAllPaths(tree, []));
      setSelectedNode("");
    }
  }, [tree]);

  return (
    <TreeView
      style={{ maxHeight: "350px", overflowY: "scroll", width: "620px" }}
      defaultExpandIcon={<Add />}
      defaultCollapseIcon={<Remove />}
      selected={selectedNode}
      expanded={expanded}
      onNodeToggle={(event, paths) => {
        setExpanded(paths);
      }}
      onNodeSelect={(event: React.ChangeEvent<{}>, nodePath: string) => {
        setSelected(nodePath);
        setSelectedNode(nodePath);
      }}
    >
      {renderedTree}
    </TreeView>
  );
}
