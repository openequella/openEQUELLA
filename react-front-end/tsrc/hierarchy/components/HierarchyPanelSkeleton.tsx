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
import {
  Card,
  CardContent,
  CardHeader,
  Skeleton,
  Typography,
} from "@mui/material";
import * as React from "react";

/**
 * A skeleton view for the hierarchy panel component.
 */
const HierarchyPanelSkeleton = () => (
  <Card>
    <CardHeader title={<Skeleton />} />
    <CardContent>
      <Typography variant="h4" gutterBottom>
        <Skeleton />
      </Typography>
      <Skeleton variant="rounded" height={100} />
    </CardContent>
  </Card>
);

export default HierarchyPanelSkeleton;