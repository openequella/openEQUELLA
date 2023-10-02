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
import { Card, Typography } from "@mui/material";
import { styled } from "@mui/material/styles";
import * as React from "react";

const PREFIX = "code-block";

const classes = {
  block: `${PREFIX}-block`,
  code: `${PREFIX}-code`,
};

const StyledCard = styled(Card)(({ theme }) => ({
  [`&`]: {
    backgroundColor: theme.palette.grey[200],
    padding: theme.spacing(2),
    fontSize: "inherit",
  },
  [`& .${classes.code}`]: {
    fontFamily: "monospace",
    color: theme.palette.text.secondary,
    fontSize: "inherit",
  },
}));

export interface CodeBlockProps {
  value: string;
}

/**
 * For displaying formatted code snippets on an outlined card with gray background.
 */
const CodeBlock = ({ value }: CodeBlockProps) => (
  <StyledCard variant="outlined" className={classes.block}>
    <Typography className={classes.code}>{value}</Typography>
  </StyledCard>
);

export default CodeBlock;
