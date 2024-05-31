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

import { Grid } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import KeyResource from "./KeyResource";

export interface KeyResourcePanelProps {
  /**
   * A list of key resource items to display.
   */
  items: OEQ.Search.SearchResultItem[];
  /**
   * The handler for each key resource item's pin icon click event.
   */
  onPinIconClick?: (item: OEQ.Search.SearchResultItem) => void;
}

/**
 * Displays a list of key resource items.
 */
const KeyResourcePanel = ({ items, onPinIconClick }: KeyResourcePanelProps) => (
  <Grid container spacing={2} data-testid="key-resource-panel">
    {items.map((item) => (
      <Grid key={`${item.uuid}/${item.version}`} item xs={12} lg={6} xl={4}>
        <KeyResource item={item} onPinIconClick={onPinIconClick}></KeyResource>
      </Grid>
    ))}
  </Grid>
);

export default KeyResourcePanel;
