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
import { IconButton, IconButtonProps } from "@material-ui/core";
import Tooltip from "@material-ui/core/Tooltip";
import { TooltipProps } from "@material-ui/core/Tooltip/Tooltip";
import * as React from "react";

/**
 * A combined type including TooltipProps and IconButtonProps.
 * 'onClick' of TooltipProps is omitted as what's really clicked is the icon button.
 */
export type TooltipIconButtonProps = Omit<TooltipProps, "onClick"> &
  IconButtonProps;

/**
 * Provide an IconButton wrapped by a Tooltip.
 */
export const TooltipIconButton = ({
  title,
  onClick,
  children,
  size,
  color,
  "aria-label": ariaLabel,
}: TooltipIconButtonProps) => (
  <Tooltip title={title}>
    <IconButton
      onClick={onClick}
      aria-label={ariaLabel ?? title}
      size={size}
      color={color}
    >
      {children}
    </IconButton>
  </Tooltip>
);
