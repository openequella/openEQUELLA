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
import { Card, CardContent, Typography } from "@material-ui/core";
import * as React from "react";
import { languageStrings } from "../../util/langstrings";
import { CategorySelector, CategorySelectorProps } from "./CategorySelector";

/**
 * Lays out `CategorySelector` in a MUI `Card` with appropriate title. As a result, props
 * are simply passed through to `CategorySelector`.
 */
export const ClassificationsPanel = (props: CategorySelectorProps) => (
  <Card>
    <CardContent>
      <Typography variant="h5">
        {languageStrings.searchpage.categorySelector.title}
      </Typography>
      <CategorySelector {...props} />
    </CardContent>
  </Card>
);
