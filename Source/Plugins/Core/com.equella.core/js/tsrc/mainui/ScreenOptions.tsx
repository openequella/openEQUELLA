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
import { IconButton, makeStyles, Popover } from "@material-ui/core";
import MoreVertIcon from "@material-ui/icons/MoreVert";
import JQueryDiv from "../legacycontent/JQueryDiv";
import { languageStrings } from "../util/langstrings";

const useStyles = makeStyles((t) => ({
  screenOptions: {
    margin: 20,
  },
}));

interface ScreenOptionsProps {
  optionsHtml: string;
  contentId: string;
}

export default React.memo(function ScreenOptions({
  optionsHtml,
}: ScreenOptionsProps) {
  const [optionsAnchor, setOptionsAnchor] = React.useState<HTMLElement>();
  const classes = useStyles();
  return (
    <>
      <IconButton
        id="screenOptionsOpen"
        onClick={(e) => setOptionsAnchor(e.currentTarget)}
        aria-label={languageStrings.screenoptions.description}
      >
        <MoreVertIcon />
      </IconButton>
      <Popover
        open={Boolean(optionsAnchor)}
        marginThreshold={64}
        keepMounted
        container={document.getElementById("eqpageForm")}
        anchorOrigin={{ vertical: "bottom", horizontal: "left" }}
        anchorEl={optionsAnchor}
        onClose={(_) => setOptionsAnchor(undefined)}
      >
        <JQueryDiv
          id="screenOptions"
          className={classes.screenOptions}
          html={optionsHtml}
        />
      </Popover>
    </>
  );
});
