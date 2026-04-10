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
import * as React from "react";
import { PropsWithChildren } from "react";
import Stack, { StackProps } from "@mui/material/Stack";
import Divider from "@mui/material/Divider";

const classes = {
  metadataRow: `metadata-row`,
  divider: `metadata-row-divider`,
};

const StyledStack = styled(Stack)(({ theme }) => {
  return {
    [`.${classes.metadataRow}`]: {
      paddingTop: theme.spacing(1),
      alignItems: "center",
    },
    [`& .${classes.divider}`]: {
      margin: "0px 16px",
    },
  };
});

/**
 * Displays a list of metadata in a row, separated by vertical dividers.
 */
export const MetadataRow = ({
  children,
  ...rest
}: PropsWithChildren<StackProps>) => (
  <StyledStack
    direction="row"
    alignItems="center"
    className={classes.metadataRow}
    divider={
      <Divider
        flexItem
        component="span"
        variant="middle"
        orientation="vertical"
        className={classes.divider}
      />
    }
    {...rest}
  >
    {children}
  </StyledStack>
);

export default MetadataRow;
