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
import { Chip, Tooltip } from "@mui/material";
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

// The maximum length of a label before it gets truncated with ellipsis.
const MAX_LABEL_LENGTH = 50;
// Determine if the label is too long and needs to be truncated with ellipsis.
const isLabelTooLong = (label: string) => label.length > MAX_LABEL_LENGTH;

// Build a chip component for the given content and index. If the content is too long, it will be truncated
// with ellipsis and the full content will be shown in a tooltip on hover.
const buildChip = (content: string, index: number) => {
  if (isLabelTooLong(content)) {
    const shortContent = content.slice(0, MAX_LABEL_LENGTH) + "...";

    return (
      <Tooltip title={content} key={index}>
        <Chip
          component="span"
          sx={{ margin: 0.5 }}
          key={index}
          label={shortContent}
          color="secondary"
          size="small"
        />
      </Tooltip>
    );
  }

  return (
    <Chip
      component="span"
      sx={{ margin: 0.5 }}
      key={index}
      label={content}
      color="secondary"
      size="small"
    />
  );
};

/**
 * A component that displays search options as chips in the {@link SearchOptions}.
 * It will truncate long labels with ellipsis and show the full label in a tooltip on hover.
 */
const SearchOptionsChips = ({ options, prefix }: SearchOptionsChipsProps) => {
  const formatChipLabel = prefix
    ? (label: string) => `${prefix}: ${label}`
    : (label: string) => label;

  const chipLabels = S.isString(options) ? [options] : options;

  return chipLabels.map(formatChipLabel).map(buildChip);
};

export default SearchOptionsChips;
