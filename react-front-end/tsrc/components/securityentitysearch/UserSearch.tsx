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
import { ListItemText } from "@material-ui/core";
import * as OEQ from "@openequella/rest-api-client";
import * as ORD from "fp-ts/Ord";
import * as S from "fp-ts/string";
import * as React from "react";
import { listUsers } from "../../modules/UserModule";
import { languageStrings } from "../../util/langstrings";
import BaseSearch, {
  CommonEntitySearchProps,
  wildcardQuery,
} from "./BaseSearch";

export interface UserSearchProps
  extends CommonEntitySearchProps<OEQ.UserQuery.UserDetails> {
  search?: (
    query?: string,
    filter?: ReadonlySet<string>
  ) => Promise<OEQ.UserQuery.UserDetails[]>;
}

/**
 * Provides a control to list users via an input field text query filter.
 * Users can then be selected (support single/multiple select).
 */
const UserSearch = ({
  search = (query?: string, groupFilter?: ReadonlySet<string>) =>
    listUsers(wildcardQuery(query), groupFilter),
  ...restProps
}: UserSearchProps) => {
  /**
   * A template used to display a user entry in BaseSearch (in CheckboxList).
   */
  const userEntry = ({
    username,
    firstName,
    lastName,
  }: OEQ.UserQuery.UserDetails) => (
    <ListItemText primary={username} secondary={`${firstName} ${lastName}`} />
  );

  return (
    <BaseSearch<OEQ.UserQuery.UserDetails>
      strings={languageStrings.userSearchComponent}
      itemOrd={ORD.contramap((u: OEQ.UserQuery.UserDetails) => u.username)(
        S.Ord
      )}
      itemDetailsToEntry={userEntry}
      search={search}
      {...restProps}
    />
  );
};

export default UserSearch;
