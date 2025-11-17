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
import { pipe } from "fp-ts/function";
import * as A from "fp-ts/Array";
import * as T from "fp-ts/Task";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { useEffect, useState } from "react";
import HierarchyTree from "../../hierarchy/components/HierarchyTree";
import { getRootHierarchies } from "../../modules/HierarchyModule";
import { DraggablePortlet } from "../components/DraggablePortlet";
import * as OEQ from "@openequella/rest-api-client";
import { PortletSearchResultNoneFound } from "../components/PortletSearchResultNoneFound";
import type { PortletBasicProps } from "./PortletHelper";
import { languageStrings } from "../../util/langstrings";

const { noneFound } = languageStrings.dashboard.portlets.browse;

export interface PortletBrowseProps extends PortletBasicProps {
  /** Optional provider for fetching root hierarchies - primarily for testing. */
  getRootHierarchiesProvider?: () => Promise<
    OEQ.BrowseHierarchy.HierarchyTopicSummary[]
  >;
}

export const PortletBrowse = ({
  cfg,
  position,
  getRootHierarchiesProvider = getRootHierarchies,
}: PortletBrowseProps) => {
  const [hierarchies, setHierarchies] = useState<
    OEQ.BrowseHierarchy.HierarchyTopicSummary[]
  >([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    pipe(
      TE.tryCatch(
        () => getRootHierarchiesProvider(),
        (e) => `Failed to get hierarchies: ${e}`,
      ),
      TE.match((e) => {
        console.error(e);
      }, setHierarchies),
      T.tapIO(() => () => setIsLoading(false)),
    )();
  }, [getRootHierarchiesProvider]);

  return (
    <DraggablePortlet
      portlet={cfg}
      position={position}
      isLoading={isLoading}
      sx={{
        // Ensure the content area is scrollable when hierarchy tree overflows.
        "& .MuiCardContent-root": {
          overflow: "auto",
        },
      }}
    >
      {A.isEmpty(hierarchies) ? (
        <PortletSearchResultNoneFound noneFoundMessage={noneFound} />
      ) : (
        <HierarchyTree hierarchies={hierarchies} />
      )}
    </DraggablePortlet>
  );
};
