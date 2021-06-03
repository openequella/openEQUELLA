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
import { Button, ButtonGroup } from "@material-ui/core";
import LibraryBooksIcon from "@material-ui/icons/LibraryBooks";
import PhotoLibraryIcon from "@material-ui/icons/PhotoLibrary";
import VideoLibraryIcon from "@material-ui/icons/VideoLibrary";
import * as React from "react";
import { ReactElement } from "react";
import { languageStrings } from "../../util/langstrings";
import type { DisplayMode } from "../../modules/SearchModule";

const {
  modeGalleryImage: labelImageGallery,
  modeGalleryVideo: labelVideoGallery,
  modeItemList: labelItemList,
} = languageStrings.searchpage.displayModeSelector;

export interface DisplayModeSelectorProps {
  /**
   * The current selection.
   */
  value: DisplayMode;
  /**
   * Handler to call when the selection is modified. The resulting value being what would be passed
   * back as `value` for future renderings.
   *
   * @param value the new Display Mode
   */
  onChange: (value: DisplayMode) => void;
  /**
   * `true` to disable Image mode.
   */
  disableImageMode: boolean;
  /**
   * `true` to disable Video mode.
   */
  disableVideoMode: boolean;
}

/**
 * A group of toggle buttons to provide a simple means of change the mode the search results are
 * displayed in.
 */
const DisplayModeSelector = ({
  value,
  onChange,
  disableImageMode,
  disableVideoMode,
}: DisplayModeSelectorProps) => {
  const options: {
    displayMode: DisplayMode;
    icon: ReactElement;
    label: string;
    disabled: boolean;
  }[] = [
    {
      displayMode: "list",
      icon: <LibraryBooksIcon />,
      label: labelItemList,
      disabled: false,
    },
    {
      displayMode: "gallery-image",
      icon: <PhotoLibraryIcon />,
      label: labelImageGallery,
      disabled: disableImageMode,
    },
    {
      displayMode: "gallery-video",
      icon: <VideoLibraryIcon />,
      label: labelVideoGallery,
      disabled: disableVideoMode,
    },
  ];

  const buttons = options
    .filter((option) => !option.disabled)
    .map(({ displayMode, icon, label }) => {
      const currentlySelected = displayMode === value;

      return (
        <Button
          key={displayMode}
          variant={currentlySelected ? "contained" : "outlined"}
          onClick={() => onChange(displayMode)}
          aria-checked={currentlySelected}
          aria-label={label}
        >
          {icon}
        </Button>
      );
    });

  return <ButtonGroup color="secondary">{buttons}</ButtonGroup>;
};

export default DisplayModeSelector;
