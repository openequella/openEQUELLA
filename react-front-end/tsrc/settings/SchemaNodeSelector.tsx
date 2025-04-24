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
import { styled } from "@mui/material/styles";
import { TreeView } from "@mui/x-tree-view/TreeView";
import { TreeItem } from "@mui/x-tree-view/TreeItem";
import Add from "@mui/icons-material/Add";
import Remove from "@mui/icons-material/Remove";
import { getAllPaths, pathForNode, SchemaNode } from "../modules/SchemaModule";
import { Button, Grid } from "@mui/material";
import { languageStrings } from "../util/langstrings";

const PREFIX = "SchemaNodeSelector";

const classes = {
  treeView: `${PREFIX}-treeView`,
  button: `${PREFIX}-button`,
};

const Root = styled("div")(({ theme }) => {
  return {
    [`& .${classes.treeView}`]: {
      flexGrow: 1,
      height: "30vh",
      overflowY: "auto",
      width: "100%",
    },
    [`& .${classes.button}`]: {
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1),
    },
  };
});

export interface SchemaNodeSelectorProps {
  /**
   * The schema tree this component will display.
   */
  tree: SchemaNode | undefined;
  /**
   * Function that gets called upon selection of a node.
   * @param node The path of the selected node.
   */
  setSelectedNode: (node: string) => void;
  /**
   * If present, expand all/collapse all buttons will be available.
   */
  expandControls?: boolean;
}

/**
 * Recursive helper function that converts a SchemaNode into a corresponding Material UI
 * TreeItem tree.
 * @param nodes The node to generate a TreeItem for.
 */
export const renderTree = (nodes: SchemaNode) => {
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
  tree,
  setSelectedNode,
  expandControls,
}: SchemaNodeSelectorProps) {
  const [selectedNode, setSelected] = React.useState("");
  const [expanded, setExpanded] = React.useState<string[]>([]);
  const [renderedTree, setRenderedTree] = React.useState<JSX.Element>(<div />);

  const strings =
    languageStrings.settings.searching.facetedsearchsetting.schemaSelector
      .nodeSelector;

  React.useEffect(() => {
    if (tree) {
      setRenderedTree(renderTree(tree));
      setExpanded([]);
      setSelectedNode("");
    }
  }, [tree, setSelectedNode]);

  return (
    <Root>
      {expandControls && (
        <Grid container direction="row" wrap="nowrap" justifyContent="flex-end">
          <Grid item>
            <Button
              className={classes.button}
              size="small"
              onClick={() => {
                if (!tree) throw new TypeError("tree is not defined");
                setExpanded(getAllPaths(tree, false));
              }}
            >
              {strings.expandAll}
            </Button>
          </Grid>
          <Grid item>
            <Button
              className={classes.button}
              size="small"
              onClick={() => setExpanded([])}
            >
              {strings.collapseAll}
            </Button>
          </Grid>
        </Grid>
      )}
      <TreeView
        className={classes.treeView}
        defaultExpandIcon={<Add />}
        defaultCollapseIcon={<Remove />}
        selected={selectedNode}
        expanded={expanded}
        onNodeToggle={(
          event: React.SyntheticEvent<Element, Event>,
          paths: string[],
        ) => {
          setExpanded(paths);
        }}
        onNodeSelect={(event: React.ChangeEvent<object>, nodePath: string) => {
          setSelected(nodePath);
          setSelectedNode(nodePath);
        }}
      >
        {renderedTree}
      </TreeView>
    </Root>
  );
}
