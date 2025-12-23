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
import { ViewList } from "@mui/icons-material";
import { List, ListItemButton } from "@mui/material";
import * as A from "fp-ts/Array";
import { flow, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as React from "react";
import { Link } from "react-router-dom";
import { MyResourcesTypeData } from "../../modules/SearchMyResourceModule";
import { ListItemContent } from "./PortletHelper";

export interface MyResourcesTypeProps {
  /** The data for a specific 'My Resources' type. */
  myResourcesType: MyResourcesTypeData;
}

/**
 * Component that renders a list item for a 'My Resources' type with count. It handles the display of the
 * parent category and any associated sub-searches as nested items.
 */
export const MyResourcesType: React.FC<MyResourcesTypeProps> = ({
  myResourcesType,
}) => {
  const parentListItem = (
    <ListItemButton component={Link} to={myResourcesType.to}>
      <ListItemContent
        text={myResourcesType.name}
        count={myResourcesType.count}
      />
    </ListItemButton>
  );

  const childItems = pipe(
    O.fromNullable(myResourcesType.subSearches),
    O.filter(A.isNonEmpty),
    O.map(
      flow(
        A.map((subSearch) => (
          <ListItemButton
            key={subSearch.id}
            sx={{ pl: 4 }}
            component={Link}
            to={subSearch.to}
          >
            <ListItemContent
              text={subSearch.name}
              count={subSearch.count}
              icon={<ViewList />}
            />
          </ListItemButton>
        )),
        (items) => <List>{items}</List>,
      ),
    ),
    O.toNullable,
  );

  return (
    <>
      {parentListItem}
      {childItems}
    </>
  );
};
