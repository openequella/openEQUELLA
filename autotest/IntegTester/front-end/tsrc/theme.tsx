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
import "babel-polyfill";
import { Paper, TextField } from "@mui/material";
import { styled } from "@mui/material/styles";
import { createTheme } from "@mui/material/styles";
import { deepOrange, deepPurple } from "@mui/material/colors";

export const theme = createTheme({
  palette: {
    primary: deepOrange,
    secondary: deepPurple,
  },
});

export const StyledRoot = styled("div")({
  height: "100vh",
  display: "flex",
  flexDirection: "column",
});

export const StyledPaper = styled(Paper)({
  display: "flex",
  flexDirection: "column",
  flexGrow: 1,
});

export const StyledBody = styled("div")({
  margin: theme.spacing(2),
});

export const StyledTextField = styled(TextField)({
  marginLeft: theme.spacing(1),
  marginRight: theme.spacing(1),
  width: 300,
});
