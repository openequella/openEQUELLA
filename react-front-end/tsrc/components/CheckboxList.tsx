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
import {
  Checkbox,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
} from "@mui/material";
import { pipe } from "fp-ts/function";
import * as M from "fp-ts/Map";
import * as SET from "fp-ts/Set";
import * as S from "fp-ts/string";
import * as React from "react";
import { OrdAsIs } from "../util/Ord";

/**
 * Map collect where order is unimportant.
 */
const collectUnOrd = M.collect<string>(OrdAsIs);

export interface CheckboxListProps {
  /**
   * DOM id.
   */
  id: string;
  /**
   * The available options / checkboxes. Where the **keys** are ultimately the values used at the
   * program level, and the **values** are used for display purposes.
   */
  options: Map<string, string>;
  /**
   * The **keys** of the `options` which should be 'checked'/ticked/selected.
   */
  checked: Set<string>;
  /**
   * On change handler which will return the list of currently `checked` `options`.
   */
  onChange: (checked: Set<string>) => void;
}

/**
 * Displays a list of `options` as a list with each one having a checkbox next to them for
 * selection.
 */
export const CheckboxList = ({
  id,
  options,
  checked,
  onChange,
}: CheckboxListProps): JSX.Element => {
  const labelId = (forValue: string): string => `${id}-label-${forValue}`;

  const isChecked = (forValue: string): boolean =>
    pipe(checked, SET.elem(S.Eq)(forValue));

  const handleOnClick = (value: string): void =>
    pipe(checked, SET.toggle(S.Eq)(value), onChange);

  return (
    <List id={id}>
      {pipe(
        options,
        collectUnOrd(
          (value: string, text: string): JSX.Element => (
            <ListItem
              key={value}
              dense
              button
              onClick={() => pipe(value, handleOnClick)}
            >
              <ListItemIcon>
                <Checkbox
                  edge="start"
                  tabIndex={-1}
                  disableRipple
                  inputProps={{ "aria-labelledby": labelId(value) }}
                  checked={isChecked(value)}
                />
              </ListItemIcon>
              <ListItemText id={labelId(value)} primary={text} />
            </ListItem>
          )
        )
      )}
    </List>
  );
};
