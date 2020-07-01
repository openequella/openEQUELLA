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
  popover: {
    position: "absolute",
    zIndex: 2,
  },
  cover: {
    position: "fixed",
    top: "0px",
    right: "0px",
    bottom: "0px",
    left: "0px",
  },
});

interface ColorProps {
  currentColor?: string;
  onColorChange(color: string): void;
}

const ColorPickerComponent = ({ currentColor, onColorChange }: ColorProps) => {
  const [displayColorPicker, setDisplayColorPicker] = useState<boolean>(false);
  const classes = useStyles();

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
        <div className={classes.popover}>
          <div
            className={classes.cover}
            onClick={() => setDisplayColorPicker(false)}
          />
          <SketchPicker
            disableAlpha={true}
            color={currentColor}
            onChange={changeHandler}
            onChangeComplete={changeHandler}
          />
        </div>
      )}
    </>
  );
};

export default ColorPickerComponent;
