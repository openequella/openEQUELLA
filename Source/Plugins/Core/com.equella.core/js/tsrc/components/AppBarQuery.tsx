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
import { useState, FunctionComponent, ChangeEvent } from "react";
import { Theme } from "@material-ui/core";
import { Search } from "@material-ui/icons";
import { fade } from "@material-ui/core/styles/colorManipulator";
import { makeStyles } from "@material-ui/core/styles";
import { commonString } from "../util/commonstrings";
import { debounce } from "lodash";

interface AppBarQueryProps {
  onChange: (query: string) => void;
  query: string;
}

const useStyles = makeStyles((theme: Theme) => ({
  queryWrapper: {
    position: "relative",
    fontFamily: theme.typography.fontFamily,
    marginRight: theme.spacing(2),
    marginLeft: theme.spacing(2),
    borderRadius: 2,
    background: fade(theme.palette.common.white, 0.15),
    width: "400px",
  },
  queryIcon: {
    width: theme.spacing(9),
    height: "100%",
    position: "absolute",
    pointerEvents: "none",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
  },
  queryField: {
    font: "inherit",
    padding: theme.spacing(1),
    paddingLeft: theme.spacing(9),
    border: 0,
    display: "block",
    verticalAlign: "middle",
    whiteSpace: "normal",
    background: "none",
    margin: 0,
    color: "inherit",
    width: "100%",
  },
}));

const AppBarQuery: FunctionComponent<AppBarQueryProps> = ({
  onChange,
  query,
}: AppBarQueryProps) => {
  const classes = useStyles();
  const [searchText, setSearchText] = useState(query);
  const [debounceHandler] = useState(() =>
    debounce((value: string) => onChange(value), 500)
  );

  return (
    <div className={classes.queryWrapper}>
      <div className={classes.queryIcon}>
        <Search />
      </div>
      <input
        type="text"
        aria-label={commonString.action.search}
        className={classes.queryField}
        value={searchText}
        onChange={({ target: { value } }: ChangeEvent<HTMLInputElement>) => {
          setSearchText(value);
          debounceHandler(value);
        }}
      />
    </div>
  );
};

export default AppBarQuery;
