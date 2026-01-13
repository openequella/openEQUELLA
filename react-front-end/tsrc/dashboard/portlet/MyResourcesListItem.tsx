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
import { MyResourcesCategory } from "../../modules/MyResourceModule";
import { ListItemContent } from "../components/ListItemContent";

export interface MyResourcesListItemProps {
  /** The data for a specific 'My Resources' type. */
  myResourcesType: MyResourcesCategory;
}

/**
 * Component that renders a list item for a 'My Resources' type with count. It handles the display of the
 * parent category and any associated sub-categories as nested items.
 */
export const MyResourcesListItem: React.FC<MyResourcesListItemProps> = ({
  myResourcesType,
}) => {
  const parentListItem = (
    <ListItemButton
      component={Link}
      to={myResourcesType.to}
      data-testid={`my-resources-category-${myResourcesType.name}`}
    >
      <ListItemContent
        text={myResourcesType.name}
        count={myResourcesType.count}
      />
    </ListItemButton>
  );

  const childItems = pipe(
    O.fromNullable(myResourcesType.subCategories),
    O.filter(A.isNonEmpty),
    O.map(
      flow(
        A.map((subCategory) => (
          <ListItemButton
            key={subCategory.id}
            sx={{ pl: 4 }}
            component={Link}
            to={subCategory.to}
            data-testid={`my-resources-category-${subCategory.name}`}
          >
            <ListItemContent
              text={subCategory.name}
              count={subCategory.count}
              icon={<ViewList />}
            />
          </ListItemButton>
        )),
        (items) => <List disablePadding>{items}</List>,
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
