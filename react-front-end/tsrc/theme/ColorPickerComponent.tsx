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
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
} from "@mui/material";
import { styled } from "@mui/material/styles";
import * as React from "react";
import { useState } from "react";
import { ColorResult, SketchPicker } from "react-color";
import { languageStrings } from "../util/langstrings";

const PREFIX = "ColorPickerComponent";

const classes = {
  color: `${PREFIX}-color`,
  swatch: `${PREFIX}-swatch`,
};

const StyledDiv = styled("div")({
  padding: "5px",
  background: "#fff",
  borderRadius: "1px",
  boxShadow: "0 0 0 1px rgba(0,0,0,.1)",
  display: "inline-block",
  cursor: "pointer",
  div: {
    width: "36px",
    height: "14px",
    borderRadius: "2px",
  },
});

interface ColorProps {
  currentColor?: string;
  onColorChange(color: string): void;
}

const ColorPickerComponent = ({ currentColor, onColorChange }: ColorProps) => {
  const [displayColorPicker, setDisplayColorPicker] = useState<boolean>(false);
  const [tempColor, setTempColor] = useState<string | undefined>(currentColor);

  const strings = languageStrings.newuisettings.colorPicker;

  const handleOpenDialog = () => {
    setTempColor(currentColor); // Reset temp colour to current colour when opening
    setDisplayColorPicker(true);
  };

  const handleColorChange = (color: ColorResult) => {
    setTempColor(color.hex);
  };

  const handleDone = () => {
    // Only call onColorChange if the colour has actually changed
    if (tempColor && tempColor !== currentColor) {
      onColorChange(tempColor);
    }
    setDisplayColorPicker(false);
  };

  return (
    <>
      <StyledDiv className={classes.swatch} onClick={handleOpenDialog}>
        <div style={{ background: currentColor }} className={classes.color} />
      </StyledDiv>
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
              color={tempColor || currentColor}
              onChange={handleColorChange}
              onChangeComplete={handleColorChange}
            />
          </DialogContent>
          <DialogActions>
            <Button color="primary" onClick={handleDone}>
              {languageStrings.common.action.done}
            </Button>
          </DialogActions>
        </Dialog>
      )}
    </>
  );
};

export default ColorPickerComponent;
