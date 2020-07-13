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
import {
  List,
  ListItem,
  ListItemText,
  Checkbox,
  Typography,
} from "@material-ui/core";
import { getMimeTypeDetail, MimeTypeEntry } from "./SearchFilterSettingsModule";
import { makeStyles } from "@material-ui/core/styles";
import { languageStrings } from "../../../util/langstrings";

const useStyles = makeStyles({
  item: {
    padding: 0,
  },
  list: {
    maxHeight: 400,
    overflow: "auto",
  },
  listTitle: {
    marginTop: 20,
  },
});

interface MimeTypeFilterListProps {
  /**
   * A list of MIME types to be displayed.
   */
  entries: MimeTypeEntry[];
  /**
   * Fired when the value of each Checkbox is changed.
   */
  onChange(check: boolean, mimeType: string): void;
  /**
   * Values of Checkboxes that are ticked.
   */
  selected: string[];
}

const MimeTypeList = ({
  entries,
  onChange,
  selected,
}: MimeTypeFilterListProps) => {
  const classes = useStyles();
  const searchFilterStrings =
    languageStrings.settings.searching.searchfiltersettings;

  return (
    <List
      className={classes.list}
      subheader={
        <Typography
          variant="subtitle2"
          color="textSecondary"
          className={classes.listTitle}
        >
          {searchFilterStrings.mimetypelistlabel}
        </Typography>
      }
    >
      {entries.map((entry, index) => {
        return (
          <ListItem key={index} className={classes.item}>
            <Checkbox
              checked={selected.indexOf(entry.mimeType) >= 0}
              onChange={(_, checked) => onChange(checked, entry.mimeType)}
            />
            <ListItemText primary={getMimeTypeDetail(entry)} />
          </ListItem>
        );
      })}
    </List>
  );
};

export default React.memo(MimeTypeList);
