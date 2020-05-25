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
import { templateDefaults, TemplateUpdateProps } from "../mainui/Template";
import { useEffect } from "react";
import { languageStrings } from "../util/langstrings";
import {
  Card,
  IconButton,
  List,
  ListItem,
  ListItemText,
  ListSubheader,
  TextField,
} from "@material-ui/core";
import SearchIcon from "@material-ui/icons/Search";

const SearchPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const searchStrings = languageStrings.searchpage;
  const placeholderData: string[] = [
    "item1",
    "item2",
    "item3",
    "item4",
    "item5",
  ];

  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(searchStrings.title)(tp),
    }));
  }, []);

  const searchResults = placeholderData.map((data: string) => {
    return (
      <ListItem>
        <ListItemText primary={data} />
      </ListItem>
    );
  });
  const searchResultList = (
    <List
      subheader={
        <ListSubheader disableGutters>{searchStrings.subtitle}</ListSubheader>
      }
    >
      {searchResults}
    </List>
  );

  return (
    <>
      <Card>
        <IconButton>
          <SearchIcon fontSize={"large"} />
        </IconButton>
        <TextField />
      </Card>
      <Card>{searchResultList}</Card>
    </>
  );
};

export default SearchPage;
