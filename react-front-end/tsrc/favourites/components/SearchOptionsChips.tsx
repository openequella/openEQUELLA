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
import { Chip } from "@mui/material";
import * as S from "fp-ts/string";
import * as React from "react";

export interface SearchOptionsChipsProps {
  /**
   * The options to be displayed as chips.
   */
  options: string | string[];
  /**
   * An optional prefix to be displayed before each option in the chip.
   */
  prefix?: string;
}

/**
 * A component that displays search options as chips in the {@link SearchOptions}.
 */
const SearchOptionsChips = ({ options, prefix }: SearchOptionsChipsProps) => {
  const formatChipLabel = prefix
    ? (label: string) => `${prefix}: ${label}`
    : (label: string) => label;

  const chipLabels = S.isString(options) ? [options] : options;

  return chipLabels
    .map(formatChipLabel)
    .map((content, index) => (
      <Chip
        component="span"
        sx={{ margin: 0.5 }}
        key={index}
        label={content}
        color="secondary"
        size="small"
      />
    ));
};

export default SearchOptionsChips;
