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
import { TreeItem } from "@mui/x-tree-view/TreeItem";
import { styled } from "@mui/material/styles";

const PREFIX = "ACLTreeItem";
export const classes = {
  root: `${PREFIX}-root`,
  content: "MuiTreeItem-content",
  labelRoot: `${PREFIX}-labelRoot`,
  labelText: `${PREFIX}-labelText`,
  labelSelect: `${PREFIX}-labelSelect`,
};

export const ACLTreeItem = styled(TreeItem)(({ theme }) => ({
  [`&.${classes.root}`]: {
    color: theme.palette.text.secondary,
  },
  // TreeItem selected effect (changing background color) will ignore the padding.
  [`& .${classes.content}`]: {
    boxSizing: "border-box",
  },
  [`& .${classes.labelRoot}`]: {
    display: "flex",
    alignItems: "center",
    padding: theme.spacing(1, 0),
  },
  [`& .${classes.labelText}`]: {
    flexGrow: 1,
  },
  [`& .${classes.labelSelect}`]: {
    flexGrow: 1,
  },
}));
