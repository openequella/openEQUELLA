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
import { getAllPaths, pathForNode, SchemaNode } from "../schema/SchemaModule";
import { Button, Grid } from "@material-ui/core";
import { makeStyles, Theme } from "@material-ui/core/styles";
import { languageStrings } from "../util/langstrings";

const useStyles = makeStyles((theme: Theme) => {
  return {
    treeView: {
      flexGrow: 1,
      height: "30vh",
      overflowY: "auto",
      width: "100%",
    },
    button: {
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1),
    },
  };
});

interface SchemaNodeSelectorProps {
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
  const classes = useStyles();
  const strings =
    languageStrings.settings.searching.facetedsearchsetting.schemaSelector
      .nodeSelector;
  React.useEffect(() => {
    if (tree) {
      setRenderedTree(renderTree(tree));
      setExpanded([]);
      setSelectedNode("");
    }
  }, [tree]);

  return (
    <>
      {expandControls && (
        <Grid container direction={"row"} wrap={"nowrap"} justify={"flex-end"}>
          <Grid item>
            <Button
              className={classes.button}
              size={"small"}
              onClick={() => setExpanded(getAllPaths(tree!, false))}
            >
              {strings.expandAll}
            </Button>
          </Grid>
          <Grid item>
            <Button
              className={classes.button}
              size={"small"}
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
    </>
  );
}
