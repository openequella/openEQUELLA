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
import { searchRoles } from "../../modules/RoleModule";
import { languageStrings } from "../../util/langstrings";
import BaseSearch, {
  CommonEntitySearchProps,
  wildcardQuery,
} from "./BaseSearch";

export interface RoleSearchProps
  extends CommonEntitySearchProps<OEQ.UserQuery.RoleDetails> {
  search?: (query?: string) => Promise<OEQ.UserQuery.RoleDetails[]>;
}

/**
 * Provides a control to list roles via an input field text query filter.
 * Roles can then be selected (support single/multiple select).
 */
const RoleSearch = ({
  search = (query?: string) => searchRoles(wildcardQuery(query)),
  ...restProps
}: RoleSearchProps) => {
  /**
   * A template used to display a role entry in BaseSearch (in CheckboxList).
   */
  const roleEntry = (role: OEQ.UserQuery.RoleDetails) => (
    <ListItemText primary={role.name} />
  );

  return (
    <BaseSearch<OEQ.UserQuery.RoleDetails>
      strings={languageStrings.roleSearchComponent}
      itemOrd={ORD.contramap((r: OEQ.UserQuery.RoleDetails) => r.name)(S.Ord)}
      itemDetailsToEntry={roleEntry}
      search={search}
      {...restProps}
    />
  );
};

export default RoleSearch;
