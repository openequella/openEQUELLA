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
import { Tooltip } from "@mui/material";
import { styled } from "@mui/material/styles";
import * as React from "react";
import { ReactNode } from "react";
import { TooltipProps } from "@mui/material/Tooltip/Tooltip";

export interface TooltipCustomComponentProps
  extends Omit<TooltipProps, "children" | "title"> {
  /** The text to be displayed on the tooltip. */
  title: string;
  /** The children to be rendered inside the tooltip. */
  children: ReactNode;
}

const StyledDiv = styled("div")(() => ({
  display: "inline-block",
}));

/**
 *  A customized tooltip by wrapping its children with the Tooltip element from Material UI.
 *  The purpose of this component is to ensure that the DOM event listeners from the Tooltip
 *  are correctly applied to its child element, even if the child is a custom component.
 */
export const TooltipCustomComponent = ({
  title,
  children,
  ...rest
}: TooltipCustomComponentProps) => (
  <Tooltip title={title} {...rest}>
    <StyledDiv>{children}</StyledDiv>
  </Tooltip>
);
