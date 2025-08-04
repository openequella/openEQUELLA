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
  ToggleButton,
  ToggleButtonGroup,
} from "@mui/material";
import { styled } from "@mui/material/styles";
import * as React from "react";
import { useState } from "react";
import { ColorResult, SketchPicker, SwatchesPicker } from "react-color";
import { languageStrings } from "../util/langstrings";

const PREFIX = "ColorPickerComponent";

const classes = {
  color: `${PREFIX}-color`,
  swatch: `${PREFIX}-swatch`,
};

const StyledDiv = styled("div")({
  padding: "4px",
  background: "#fff",
  borderRadius: "4px",
  boxShadow: "0 0 0 1px rgba(0,0,0,.1)",
  display: "inline-block",
  cursor: "pointer",
  div: {
    width: "36px",
    height: "16px",
    borderRadius: "4px",
  },
});

interface ColorProps {
  currentColor?: string;
  onColorChange(color: string): void;
}

type ColorPickerType = "simple" | "swatch";

const DEFAULT_PICKER_TYPE: ColorPickerType = "simple";

const ColorPickerComponent = ({ currentColor, onColorChange }: ColorProps) => {
  const [displayColorPicker, setDisplayColorPicker] = useState<boolean>(false);
  const [tempColor, setTempColor] = useState<string | undefined>(currentColor);
  const [pickerType, setPickerType] =
    useState<ColorPickerType>(DEFAULT_PICKER_TYPE);

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

  const handleCancel = () => {
    setTempColor(currentColor); // Reset temp color to original
    setPickerType(DEFAULT_PICKER_TYPE); // Reset picker type to default
    setDisplayColorPicker(false);
  };

  const handlePickerTypeChange = (
    _: React.MouseEvent<HTMLElement>,
    newPickerType: ColorPickerType,
  ) => {
    if (newPickerType !== null) {
      setPickerType(newPickerType);
    }
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
            <ToggleButtonGroup
              value={pickerType}
              exclusive
              onChange={handlePickerTypeChange}
              aria-label="color picker type"
              fullWidth
              sx={{ mb: 2 }}
            >
              <ToggleButton value="simple" aria-label="simple picker">
                Simple
              </ToggleButton>
              <ToggleButton value="swatches" aria-label="swatches picker">
                Swatches
              </ToggleButton>
            </ToggleButtonGroup>

            {pickerType === "simple" ? (
              <SketchPicker
                disableAlpha
                color={tempColor || currentColor}
                onChange={handleColorChange}
                onChangeComplete={handleColorChange}
                // This width attempts to match the SwatchPicker width default
                width="320px"
              />
            ) : (
              <SwatchesPicker
                color={tempColor || currentColor}
                onChange={handleColorChange}
                onChangeComplete={handleColorChange}
                width={320}
                // This height attempts to match the SketchPicker height
                height={373}
              />
            )}
          </DialogContent>
          <DialogActions>
            <Button color="secondary" onClick={handleCancel}>
              {languageStrings.common.action.cancel}
            </Button>
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
