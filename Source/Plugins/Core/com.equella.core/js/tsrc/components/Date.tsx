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
import { Typography, Tooltip } from "@material-ui/core";
import { makeStyles } from "@material-ui/styles";
import { DateTime } from "luxon";

const useStyles = makeStyles({
  dateModified: {
    display: "inline-block",
  },
});
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
export default function Date({ displayRelative, date }: DateProps) {
  const classes = useStyles();
  const luxDate = DateTime.fromJSDate(date);
  let primaryDate = luxDate.toLocaleString(DateTime.DATETIME_MED);
  let hoverDate = luxDate.toRelative() || "undefined";
  if (displayRelative) {
    //swap 'em around
    [primaryDate, hoverDate] = [hoverDate, primaryDate];
  }

  return (
    <Tooltip title={hoverDate}>
      <Typography
        className={classes.dateModified}
        component="span"
        variant="body1"
      >
        {primaryDate}
      </Typography>
    </Tooltip>
  );
}
export { Date as DateDisplay };
