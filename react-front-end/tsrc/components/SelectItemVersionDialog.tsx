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
  Autocomplete,
  Chip,
  FormControl,
  FormControlLabel,
  FormLabel,
  Grid,
  Radio,
  RadioGroup,
  TextField,
} from "@mui/material";
import * as React from "react";
import { useContext, useState } from "react";
import ConfirmDialog from "./ConfirmDialog";
import { AppContext } from "../mainui/App";
import { languageStrings } from "../util/langstrings";

const { selectVersion, toThisVersion, versionOptions } =
  languageStrings.selectItemVersionDialog;

export type ItemVersionOption = "latest" | "this";

export interface SelectItemVersionDialogProps {
  /**
   * `true` to open the dialog
   */
  open: boolean;
  /**
   * Fired when the dialog is closed
   */
  closeDialog: () => void;
  /**
   * Title for the dialog.
   */
  title: string;
  /**
   * Description of the tag input field.
   * If not provided, the tag input field will not be shown.
   */
  tagDescription?: string;
  /**
   * `true` if the Item is on its latest version.
   */
  isLatestVersion: boolean;
  /**
   * The handler for clicking the Confirm button.
   */
  onConfirm: (isAlwaysLatest: boolean, tags?: string[]) => Promise<void>;
}

/**
 * Provide a Dialog where user can select whether to add the latest version or
 * a fixed version of the selected Item to an entity (e.g. Favourite Item and Hierarchy key resource).
 */
const SelectItemVersionDialog = ({
  open,
  closeDialog,
  isLatestVersion,
  onConfirm,
  title,
  tagDescription,
}: SelectItemVersionDialogProps) => {
  const { appErrorHandler } = useContext(AppContext);

  const [tags, setTags] = useState<string[]>([]);
  const [versionOption, setVersionOption] =
    useState<ItemVersionOption>("latest");

  const confirmHandler = () =>
    onConfirm(versionOption === "latest", tags)
      .then(() => {
        // Need to reset versionOption to match the RadioGroup's default selected value .
        setVersionOption("latest");
        closeDialog();
      })
      .catch(appErrorHandler);

  return (
    <ConfirmDialog
      open={open}
      title={title}
      onConfirm={confirmHandler}
      onCancel={closeDialog}
      confirmButtonText={languageStrings.common.action.ok}
    >
      <Grid container direction="column" spacing={2}>
        {tagDescription && (
          <Grid item>
            <Autocomplete
              multiple
              freeSolo
              renderTags={(value: string[], getTagProps) =>
                value.map((option: string, index: number) => (
                  <Chip
                    label={option}
                    {...getTagProps({ index })}
                    key={index}
                  />
                ))
              }
              renderInput={(params) => (
                <TextField
                  variant="standard"
                  {...params}
                  label={tagDescription}
                />
              )}
              options={[]}
              onChange={(_, value: string[]) => setTags(value)}
            />
          </Grid>
        )}
        <Grid item>
          {isLatestVersion ? (
            <FormControl>
              <FormLabel>{selectVersion}</FormLabel>
              <RadioGroup
                row
                onChange={(event) =>
                  setVersionOption(event.target.value as ItemVersionOption)
                }
                defaultValue="latest"
              >
                <FormControlLabel
                  value="latest"
                  control={<Radio />}
                  label={versionOptions.useLatestVersion}
                />
                <FormControlLabel
                  value="this"
                  control={<Radio />}
                  label={versionOptions.useThisVersion}
                />
              </RadioGroup>
            </FormControl>
          ) : (
            toThisVersion
          )}
        </Grid>
      </Grid>
    </ConfirmDialog>
  );
};

export default SelectItemVersionDialog;
