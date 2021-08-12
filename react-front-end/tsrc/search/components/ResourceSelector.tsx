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
import DoubleArrowIcon from "@material-ui/icons/DoubleArrow";
import * as React from "react";
import { TooltipIconButton } from "../../components/TooltipIconButton";

export interface ResourceSelectorProps {
  /**
   * Text for the label of ResourceSelector
   */
  labelText: string;
  /**
   * Handler for clicking the ResourceSelector
   */
  onClick: () => void;
  /**
   * Whether to stop the propagation of a click event
   */
  isStopPropagation?: boolean;
}

/**
 * This component is basically a double arrow icon wrapped by a Tooltip.
 * A typical user case is selecting resources from the new search UI.
 */
export const ResourceSelector = ({
  labelText,
  onClick,
  isStopPropagation = false,
}: ResourceSelectorProps) => (
  <TooltipIconButton
    title={labelText}
    color="secondary"
    aria-label={labelText}
    onClick={(event) => {
      if (isStopPropagation) {
        event.stopPropagation();
      }
      onClick();
    }}
  >
    <DoubleArrowIcon />
  </TooltipIconButton>
);
