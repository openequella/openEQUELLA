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
import { Alert, Card, CardContent } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as T from "fp-ts/Task";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { useContext, useEffect, useState } from "react";
import LoadingCircle from "../components/LoadingCircle";
import { AppContext } from "../mainui/App";
import { templateDefaults, TemplateUpdateProps } from "../mainui/Template";
import { getRootHierarchies } from "../modules/HierarchyModule";
import { languageStrings } from "../util/langstrings";
import HierarchyTree from "./components/HierarchyTree";

const noHierarchyText = languageStrings.hierarchy.noHierarchy;

/**
 * Display an expandable tree view to support browsing all the Hierarchy topics.
 * Child topics are collapsed by default.
 */
const BrowseHierarchyPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const { appErrorHandler } = useContext(AppContext);

  const [isLoading, setIsLoading] = useState(true);
  const [hierarchies, setHierarchies] = useState<
    OEQ.BrowseHierarchy.HierarchyTopicSummary[]
  >([]);

  useEffect(() => {
    pipe(
      TE.tryCatch(
        () => getRootHierarchies(),
        (e) => `Failed to get hierarchies: ${e}`,
      ),
      TE.match(appErrorHandler, setHierarchies),
      T.map(() => setIsLoading(false)),
    )();
  }, [appErrorHandler]);

  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(languageStrings.hierarchy.browseHierarchyPageTitle)(
        tp,
      ),
    }));
  }, [updateTemplate]);

  return isLoading ? (
    <LoadingCircle />
  ) : (
    <Card>
      <CardContent
        sx={{
          // Handle overflow for hierarchy tree.
          overflow: "auto",
        }}
      >
        {A.isEmpty(hierarchies) ? (
          <Alert severity="info">{noHierarchyText}</Alert>
        ) : (
          <HierarchyTree hierarchies={hierarchies} />
        )}
      </CardContent>
    </Card>
  );
};

export default BrowseHierarchyPage;
