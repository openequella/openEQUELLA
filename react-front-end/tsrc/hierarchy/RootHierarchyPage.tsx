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
import * as React from "react";
import { useLocation } from "react-router";
import { useParams } from "react-router-dom";
import { generateNewErrorID } from "../api/errors";
import ErrorPage from "../mainui/ErrorPage";
import { TemplateUpdateProps } from "../mainui/Template";
import { languageStrings } from "../util/langstrings";
import HierarchyPage from "./HierarchyPage";

// Used with React-Router useParams to get a compound UUID from the route path.
interface CompoundUUID {
  compoundUuid?: string;
}

const {
  hierarchy: { error },
} = languageStrings;

/**
 * Root component for Hierarchy search to retrieve the compound UUID from the URL
 * for which the component is rendered. If the compound UUID is not found, {@link ErrorPage}
 * will be displayed instead.
 */
const RootHierarchyPage = (props: TemplateUpdateProps) => {
  const uuidFromRoute: string | undefined =
    useParams<CompoundUUID>().compoundUuid;
  const location = useLocation();
  const getUuidFromQueryParam = (): string | null =>
    new URLSearchParams(location.search).get("topic");

  const compoundUuid = uuidFromRoute ?? getUuidFromQueryParam();

  return compoundUuid ? (
    <HierarchyPage {...props} compoundUuid={compoundUuid} />
  ) : (
    <ErrorPage error={generateNewErrorID(error.missingTopicUUID)} />
  );
};

export default RootHierarchyPage;