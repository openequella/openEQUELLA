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
import { useState } from "react";
import { ColorResult, SketchPicker } from "react-color";
import { makeStyles } from "@material-ui/core/styles";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
} from "@material-ui/core";
import { languageStrings } from "../util/langstrings";

const useStyles = makeStyles({
  color: {
    width: "36px",
    height: "14px",
    borderRadius: "2px",
  },
  swatch: {
    padding: "5px",
    background: "#fff",
    borderRadius: "1px",
    boxShadow: "0 0 0 1px rgba(0,0,0,.1)",
    display: "inline-block",
    cursor: "pointer",
  },
});

interface ColorProps {
  currentColor?: string;
  onColorChange(color: string): void;
}

const ColorPickerComponent = ({ currentColor, onColorChange }: ColorProps) => {
  const [displayColorPicker, setDisplayColorPicker] = useState<boolean>(false);
  const classes = useStyles();
  const strings = languageStrings.newuisettings.colorPicker;

  const changeHandler = (color: ColorResult) => onColorChange(color.hex);

  return (
    <>
      <div
        className={classes.swatch}
        onClick={() => setDisplayColorPicker(true)}
      >
        <div style={{ background: currentColor }} className={classes.color} />
      </div>
      {displayColorPicker && (
        <Dialog
          open={displayColorPicker}
          aria-labelledby="select-color-dialog-title"
        >
          <DialogTitle id="select-color-dialog-title">
            {strings.dialogTitle}
          </DialogTitle>
          <DialogContent>
            <SketchPicker
              disableAlpha
              color={currentColor}
              onChange={changeHandler}
              onChangeComplete={changeHandler}
            />
          </DialogContent>
          <DialogActions>
            <Button
              color="primary"
              onClick={() => setDisplayColorPicker(false)}
            >
              {languageStrings.common.action.done}
            </Button>
          </DialogActions>
        </Dialog>
      )}
    </>
  );
};

export default ColorPickerComponent;
