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
import NavigateNextIcon from "@mui/icons-material/NavigateNext";
import {
  Card,
  CardContent,
  CardHeader,
  Stack,
  Typography,
} from "@mui/material";
import Breadcrumbs from "@mui/material/Breadcrumbs";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import HTMLReactParser from "html-react-parser";
import * as React from "react";
import { Link } from "react-router-dom";
import { routes } from "../../mainui/routes";
import { languageStrings } from "../../util/langstrings";
import HierarchyTree from "./HierarchyTree";

const { breadcrumb: breadcrumbText } = languageStrings.common;
const { browse: browseText } = languageStrings.hierarchy;

const buildBreadcrumbs = (
  hierarchy: OEQ.BrowseHierarchy.HierarchyTopic<OEQ.Search.SearchResultItem>,
): React.JSX.Element => {
  const crumbs = pipe(
    hierarchy.parents,
    A.map((parent) => {
      const { compoundUuid, name } = parent;
      return (
        <Link
          key={compoundUuid}
          color="inherit"
          to={routes.Hierarchy.to(compoundUuid)}
        >
          {name ?? compoundUuid}
        </Link>
      );
    }),
    A.prepend(
      <Link key="browse" color="inherit" to={routes.BrowseHierarchy.path}>
        {browseText}
      </Link>,
    ),
    A.append(
      <Typography key="last" color="text.primary">
        {hierarchy.summary.name}
      </Typography>,
    ),
  );

  return (
    <Breadcrumbs
      separator={<NavigateNextIcon fontSize="small" />}
      aria-label={breadcrumbText}
    >
      {crumbs}
    </Breadcrumbs>
  );
};

export interface HierarchyPanelProps {
  /**
   * The hierarchy to be displayed.
   */
  hierarchy: OEQ.BrowseHierarchy.HierarchyTopic<OEQ.Search.SearchResultItem>;
}

/**
 * Hierarchy panel to display details of a hierarchy. It contains a hierarchy tree view with
 * breadcrumbs, title and description.
 *
 * @param hierarchy The hierarchy to be displayed.
 */
const HierarchyPanel = ({
  hierarchy,
}: HierarchyPanelProps): React.JSX.Element => {
  const {
    name,
    longDescription,
    subHierarchyTopics,
    compoundUuid,
    subTopicSectionName,
  } = hierarchy.summary;
  return (
    <Card>
      <CardHeader title={buildBreadcrumbs(hierarchy)} />
      <CardContent>
        <Stack spacing={2}>
          <Typography variant="h4">{name ?? compoundUuid}</Typography>

          {longDescription && (
            <Typography variant="body1">
              {HTMLReactParser(longDescription)}
            </Typography>
          )}

          {subTopicSectionName && (
            <Typography variant="h5">{subTopicSectionName}</Typography>
          )}
        </Stack>
        <HierarchyTree hierarchies={subHierarchyTopics} />
      </CardContent>
    </Card>
  );
};

export default HierarchyPanel;
