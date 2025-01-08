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
import { ListItemText } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as ORD from "fp-ts/Ord";
import * as S from "fp-ts/string";
import * as React from "react";
import { searchGroups } from "../../modules/GroupModule";
import { languageStrings } from "../../util/langstrings";
import BaseSearch, {
  CommonEntitySearchProps,
  wildcardQuery,
} from "./BaseSearch";

export interface GroupSearchProps
  extends CommonEntitySearchProps<OEQ.UserQuery.GroupDetails> {
  search?: (
    query?: string,
    filter?: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.GroupDetails[]>;
}

/**
 * Provides a control to list groups via an input field text query filter.
 * Groups can then be selected (support single/multiple select).
 */
const GroupSearch = ({
  search = (query?: string, groupFilter?: ReadonlySet<string>) =>
    searchGroups(wildcardQuery(query), groupFilter),
  ...restProps
}: GroupSearchProps) => {
  /**
   * A template used to display a group entry in BaseSearch (in CheckboxList).
   */
  const groupEntry = (group: OEQ.UserQuery.GroupDetails) => (
    <ListItemText primary={group.name} />
  );

  return (
    <BaseSearch
      strings={languageStrings.groupSearchComponent}
      itemOrd={ORD.contramap((g: OEQ.UserQuery.GroupDetails) => g.name)(S.Ord)}
      itemDetailsToEntry={groupEntry}
      search={search}
      {...restProps}
    />
  );
};

export default GroupSearch;
