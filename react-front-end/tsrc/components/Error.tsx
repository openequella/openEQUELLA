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
import { Typography } from "@mui/material";
import { styled } from "@mui/material/styles";
import Paper from "@mui/material/Paper";
import * as React from "react";

const PREFIX = "Error";

const classes = {
  error: `${PREFIX}-error`,
};

const StyledPaper = styled(Paper)(({ theme }) => ({
  [`& .${classes.error}`]: {
    padding: theme.spacing(3),
    backgroundColor: "rgb(255, 220, 220)",
  },
}));

export interface ErrorProps {
  children: React.ReactNode;
}

const Error = ({ children }: ErrorProps) => {
  return (
    <StyledPaper className={classes.error}>
      <Typography color="error" align="center">
        {children}
      </Typography>
    </StyledPaper>
  );
};

export default Error;
