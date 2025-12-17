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
  Grid,
  List,
  ListItem,
  ListItemSecondaryAction,
  ListItemText,
  TextField,
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import DeleteIcon from "@mui/icons-material/Delete";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as RA from "fp-ts/ReadonlyArray";
import * as RSET from "fp-ts/ReadonlySet";
import * as S from "fp-ts/string";
import * as React from "react";
import { useState } from "react";
import { languageStrings } from "../../util/langstrings";
import { TooltipIconButton } from "../TooltipIconButton";
import { WizardControlBasicProps } from "./WizardHelper";
import { WizardLabel } from "./WizardLabel";

const {
  common: { action: commonActionStrings },
  shuffleList: shuffleListStrings,
} = languageStrings;

export interface WizardShuffleListProps extends WizardControlBasicProps {
  /**
   * Values to be displayed below the input field, with delete button alongside.
   */
  values: ReadonlySet<string>;
  /**
   * Callback called when users add another value, or remove a value. The resultant set can be
   * passed back as `values`.
   */
  onChange: (_: ReadonlySet<string>) => void;
}

export const WizardShuffleList = ({
  id,
  label,
  description,
  mandatory,
  values,
  onChange,
}: WizardShuffleListProps): React.JSX.Element => {
  const [newEntry, setNewEntry] = useState<string>("");

  const handleOnChange = () =>
    pipe(
      newEntry,
      O.fromPredicate((s) => !S.isEmpty(s)),
      O.map((s) =>
        pipe(values, RSET.insert(S.Eq)(s), (updatedValues) => {
          onChange(updatedValues);
          setNewEntry("");
        }),
      ),
    );

  return (
    <>
      <WizardLabel
        mandatory={mandatory}
        label={label}
        description={description}
        labelFor={id}
      />
      <Grid id={id} container spacing={2}>
        <Grid
          size={{
            xs: 12,
            sm: 6,
          }}
        >
          <List aria-label={shuffleListStrings.valueList}>
            <ListItem key="input controls">
              <ListItemText>
                <TextField
                  id={`${id}-new-entry`}
                  variant="outlined"
                  style={{ width: "95%" /* to accommodate icon at the end */ }}
                  label={shuffleListStrings.newEntry}
                  value={newEntry}
                  onChange={(event) => setNewEntry(event.target.value)}
                  onKeyPress={(event) =>
                    event.key === "Enter" ? handleOnChange() : undefined
                  }
                />
              </ListItemText>
              <ListItemSecondaryAction>
                <TooltipIconButton
                  title={commonActionStrings.add}
                  onClick={handleOnChange}
                >
                  <AddIcon />
                </TooltipIconButton>
              </ListItemSecondaryAction>
            </ListItem>
            {pipe(
              values,
              RSET.toReadonlyArray<string>(S.Ord),
              RA.map<string, React.JSX.Element>((s) => (
                <ListItem key={s}>
                  <ListItemText>{s}</ListItemText>
                  <ListItemSecondaryAction>
                    <TooltipIconButton
                      aria-label={`${commonActionStrings.delete} ${s}`}
                      title={commonActionStrings.delete}
                      onClick={() =>
                        pipe(values, RSET.remove(S.Eq)(s), onChange)
                      }
                    >
                      <DeleteIcon />
                    </TooltipIconButton>
                  </ListItemSecondaryAction>
                </ListItem>
              )),
            )}
          </List>
        </Grid>
      </Grid>
    </>
  );
};
