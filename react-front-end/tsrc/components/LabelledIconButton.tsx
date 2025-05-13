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
import { Button, Grid } from "@mui/material";
import { ReactElement } from "react";
import * as React from "react";
import { ButtonProps } from "@mui/material";

export interface LabelledIconButtonProps {
  /**
   * The MUI Icon used in this button
   */
  icon: ReactElement;
  /**
   * Text of this button
   */
  buttonText: string;
  /**
   * Color to be used
   */
  color?: ButtonProps["color"];
  /**
   * The button's ID
   */
  id?: string;
  /**
   * Function fired when the button is clicked
   */
  onClick?: () => void;
}

/**
 * This component provides an Icon Button which has a text to describe the button.
 * Both the icon and text are clickable.
 */
export const LabelledIconButton = ({
  icon,
  buttonText,
  color,
  id,
  onClick,
}: LabelledIconButtonProps) => (
  <Button id={id} onClick={onClick} title={buttonText} color={color}>
    <Grid container spacing={1}>
      <Grid>{icon}</Grid>
      <Grid>{buttonText}</Grid>
    </Grid>
  </Button>
);
