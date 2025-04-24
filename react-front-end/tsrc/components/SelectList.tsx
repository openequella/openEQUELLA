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
import AddCircleIcon from "@mui/icons-material/AddCircle";
import {
  List,
  ListItemButton,
  ListItemSecondaryAction,
  ListItemText,
} from "@mui/material";
import { pipe } from "fp-ts/function";
import * as S from "fp-ts/string";
import * as React from "react";
import { languageStrings } from "../util/langstrings";
import { collectUnOrd } from "../util/Map";
import { TooltipIconButton } from "./TooltipIconButton";

const { select: selectLabel } = languageStrings.common.action;

export interface SelectListProps {
  /**
   * The available options. Where the **keys** are ultimately the values used at the
   * program level, and the **values** are used for display purposes.
   * If **value** is react Element it will overwrite the main content which is displayed before `add icon`.
   */
  options: Map<string, string | JSX.Element>;
  /**
   * On select handler which will return the currently `selected` `option`.
   */
  onSelect: (selection: string) => void;
}

/**
 * Displays a list of `options` as a list with each one having an `add icon` next to them for
 * selection.
 */
export const SelectList = ({
  options,
  onSelect,
}: SelectListProps): JSX.Element => {
  return (
    <List>
      {pipe(
        options,
        collectUnOrd(
          (key: string, content: string | JSX.Element): JSX.Element => (
            <ListItemButton key={key} dense>
              {S.isString(content) ? (
                <ListItemText primary={content} />
              ) : (
                content
              )}

              <ListItemSecondaryAction>
                <TooltipIconButton
                  title={selectLabel}
                  onClick={(_) => {
                    onSelect(key);
                  }}
                >
                  <AddCircleIcon />
                </TooltipIconButton>
              </ListItemSecondaryAction>
            </ListItemButton>
          ),
        ),
      )}
    </List>
  );
};
