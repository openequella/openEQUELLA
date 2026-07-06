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
import FolderIcon from "@mui/icons-material/Folder";
import { Box, Chip, ListItemIcon, ListItemText } from "@mui/material";
import * as React from "react";

interface ListItemContentProps {
  /** Text to display. */
  text: string;
  /** Optional count to display in a chip. */
  count: number;
  /** Icon to display on the left. Defaults to `<FolderIcon />`. */
  icon?: React.ReactElement;
}

/**
 * A reusable component to display the content of a list item, including an icon,
 * primary text, and an optional count chip.
 */
export const ListItemContent: React.FC<ListItemContentProps> = ({
  text,
  count,
  icon,
}): React.ReactElement => (
  <>
    <ListItemIcon>{icon ? icon : <FolderIcon />}</ListItemIcon>
    <ListItemText
      primary={
        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
          <span>{text}</span>
          {count > 0 && <Chip label={count} color="primary" size="small" />}
        </Box>
      }
    />
  </>
);
