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
import { SvgIcon, SvgIconProps } from "@mui/material";
import * as React from "react";

/**
 * An icon representing a two-column dashboard layout with equal widths.
 */
export const TwoColumnsEqualIcon = (props: SvgIconProps) => {
  return (
    <SvgIcon {...props}>
      <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
        <rect
          x="5"
          y="5"
          width="40"
          height="90"
          stroke="currentColor"
          strokeWidth="6"
          fill="none"
          rx="5"
          ry="5"
        />
        <rect
          x="52.5"
          y="5"
          width="40"
          height="90"
          stroke="currentColor"
          strokeWidth="6"
          fill="none"
          rx="5"
          ry="5"
        />
      </svg>
    </SvgIcon>
  );
};
