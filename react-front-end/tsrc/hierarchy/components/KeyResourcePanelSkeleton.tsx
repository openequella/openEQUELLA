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
import { Box, CardContent, Grid, Skeleton } from "@mui/material";
import * as React from "react";
import { classes, StyledCard } from "./KeyResource";

/**
 * A skeleton view for the key resource panel component.
 */
const KeyResourcePanelSkeleton = () => (
  <Grid container spacing={2}>
    {[1, 2, 3].map((number) => (
      <Grid
        key={number}
        size={{
          xs: 12,
          lg: 6,
          xl: 4,
        }}
      >
        <StyledCard>
          <Box className={classes.container}>
            <Skeleton width={100} height={100} />

            <CardContent className={classes.info}>
              <Skeleton variant="text" />
              <Skeleton variant="text" height={50} />
            </CardContent>

            <Box className={classes.action}>
              <Skeleton variant="circular" width={30} height={30} />
              <Skeleton variant="circular" width={30} height={30} />
            </Box>
          </Box>
        </StyledCard>
      </Grid>
    ))}
  </Grid>
);

export default KeyResourcePanelSkeleton;
