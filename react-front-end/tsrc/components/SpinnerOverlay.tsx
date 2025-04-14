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
import { Box, CircularProgress } from "@mui/material";

/**
 * A spinner overlay that covers the parent element.
 *
 * Make sure the parent element has a position attribute of relative or absolute.
 */
const SpinnerOverlay = () => (
  <Box
    sx={{
      position: "absolute",
      // The backdrop should cover the whole card. But still under the page header.
      zIndex: 10,
      // The color and background color are set to match the material UI disabled button.
      color: "rgba(0, 0, 0, 0.26)",
      backgroundColor: "rgba(0, 0, 0, 0.12)",
      width: "100%",
      height: "100%",
      display: "flex",
      justifyContent: "center",
    }}
  >
    <CircularProgress
      sx={(theme) => ({
        marginTop: theme.spacing(4),
      })}
    />
  </Box>
);

export default SpinnerOverlay;
