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

import { Box, Card, CardContent, CardHeader, Skeleton } from "@mui/material";
import { pipe } from "fp-ts/function";
import * as React from "react";
import * as NEA from "fp-ts/NonEmptyArray";

/**
 * A skeleton for a portlet item, to be shown when the actual portlet content is loading.
 */
const PortletItemSkeleton = () => {
  const actions = () => (
    <Box sx={{ display: "flex", alignItems: "center" }}>
      {pipe(
        NEA.range(1, 3),
        NEA.map((num) => (
          <Skeleton
            key={num}
            variant="circular"
            sx={{ margin: "4px" }}
            component="span"
            width={32}
            height={32}
          />
        )),
      )}
    </Box>
  );

  return (
    <Card data-testid="portlet-item-skeleton">
      <CardHeader title={<Skeleton variant="text" />} action={actions()} />

      <CardContent>
        <Skeleton variant="rounded" height={180} />
      </CardContent>
    </Card>
  );
};

export default PortletItemSkeleton;
