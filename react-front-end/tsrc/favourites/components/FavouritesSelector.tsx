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
import SearchIcon from "@mui/icons-material/Search";
import BookmarkIcon from "@mui/icons-material/Bookmark";
import { Button, ButtonGroup, Tooltip } from "@mui/material";
import * as t from "io-ts";
import * as React from "react";
import { ReactElement } from "react";
import { languageStrings } from "../../util/langstrings";

const { resources: resourcesLabel, searches: searchesLabel } =
  languageStrings.favourites.favouritesSelector;

// TODO: move to FavouritesModule.ts
export const FavouritesTypeCodec = t.union([
  t.literal("resources"),
  t.literal("searches"),
]);
// TODO: move to FavouritesModule.ts
export type FavouritesType = t.TypeOf<typeof FavouritesTypeCodec>;

export interface FavouritesSelectorProps {
  /**
   * The current selection.
   */
  value: FavouritesType;
  /**
   * Handler to call when the selection is changed.
   *
   * @param value the new favourites type
   */
  onChange: (value: FavouritesType) => void;
}

/**
 * A group of toggle buttons that can change the favourites type.
 */
const FavouritesSelector = ({ value, onChange }: FavouritesSelectorProps) => {
  const options: {
    favouritesType: FavouritesType;
    icon: ReactElement;
    label: string;
  }[] = [
    {
      favouritesType: "resources",
      icon: <BookmarkIcon />,
      label: resourcesLabel,
    },
    {
      favouritesType: "searches",
      icon: <SearchIcon />,
      label: searchesLabel,
    },
  ];

  const buttons = options.map(({ favouritesType, icon, label }) => {
    const currentSelected = favouritesType === value;

    return (
      <Tooltip title={label} key={favouritesType}>
        <Button
          variant={currentSelected ? "contained" : "outlined"}
          onClick={() => onChange(favouritesType)}
          aria-checked={currentSelected}
          aria-label={label}
        >
          {icon}
        </Button>
      </Tooltip>
    );
  });

  return <ButtonGroup color="secondary">{buttons}</ButtonGroup>;
};

export default FavouritesSelector;
