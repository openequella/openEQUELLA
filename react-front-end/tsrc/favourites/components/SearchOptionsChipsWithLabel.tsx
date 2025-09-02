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
import { ListItem, ListItemText } from "@mui/material";
import * as React from "react";
import SearchOptionsChips from "./SearchOptionsChips";

export interface SearchOptionsChipsWithLabelProps {
  /**
   * The label to be displayed in front of the options.
   */
  label: string;
  /**
   * The options to be displayed as chips.
   */
  options?: string | string[];
}

/**
 * A component that displays a label followed by search options as chips in the {@link SearchOptions}.
 */
const SearchOptionsChipsWithLabel = ({
  label,
  options,
}: SearchOptionsChipsWithLabelProps) =>
  options == undefined ? undefined : (
    // Set padding to 0 to align with the favourites search title and make each option label closer.
    <ListItem key={label} sx={{ padding: 0 }}>
      <ListItemText
        primary={label + ":"}
        secondary={<SearchOptionsChips options={options} />}
      />
    </ListItem>
  );

export default SearchOptionsChipsWithLabel;
