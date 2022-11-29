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
import * as React from "react";
import { Typography, Tooltip } from "@mui/material";
import { DateTime } from "luxon";
import { styled } from "@mui/material/styles";

const PREFIX = "Date";

const classes = {
  dateModified: `${PREFIX}-dateModified`,
};

const StyledTooltip = styled(Tooltip)(() => ({
  [`& .${classes.dateModified}`]: {
    display: "inline-block",
  },
}));

export interface DateProps {
  /**
   * If true, relative date will be used as primary display. Timestamp will be displayed as tooltip on hover
   */
  displayRelative: boolean;
  /**
   * UTC Date/Time. It will be formatted and displayed on-screen based on user's locale
   */
  date: Date;
}

/**
 * Displays the provided `date` in a standard format, including a tooltip to display the alternate
 * representation of the date (relative vs absolute).
 */
export default function Date({ displayRelative, date }: DateProps) {
  const luxDate = DateTime.fromJSDate(date);
  let primaryDate: string | null = luxDate.toLocaleString(
    DateTime.DATETIME_MED
  );
  let hoverDate = luxDate.toRelative();
  if (displayRelative) {
    //swap 'em around
    [primaryDate, hoverDate] = [hoverDate, primaryDate];
  }

  return (
    <StyledTooltip title={hoverDate ?? "undefined"}>
      <Typography className={classes.dateModified} component="span">
        {primaryDate}
      </Typography>
    </StyledTooltip>
  );
}
export { Date };
