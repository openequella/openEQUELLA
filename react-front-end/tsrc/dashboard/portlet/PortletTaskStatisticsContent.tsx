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
import { styled } from "@mui/material/styles";

const PREFIX = "PortletTaskStatistics";

/**
 * Class names used in the {@link PortletTaskStatistics}.
 */
export const classes = {
  options: `${PREFIX}-options`,
  divider: `${PREFIX}-divider`,
  table: `${PREFIX}-table`,
  tableRow: `${PREFIX}-table-row`,
  tableLink: `${PREFIX}-table-link`,
};

/**
 * Styled div to render the statistics content.
 */
export const PortletTaskStatisticsContent = styled("div")(({ theme }) => ({
  [`& .${classes.options}`]: {
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
  },
  [`& .${classes.divider}`]: {
    marginTop: theme.spacing(2),
    marginBottom: theme.spacing(2),
  },
  [`& .${classes.table}`]: {
    borderRadius: theme.shape.borderRadius,
    border: `1px solid ${theme.palette.divider}`,
    overflow: "auto",
    // Can display 5 records without scrolling.
    maxHeight: "200px",
  },
  [`& .${classes.tableRow}:last-child td`]: {
    border: 0,
  },
  [`& .${classes.tableLink}`]: {
    cursor: "pointer",
  },
}));
