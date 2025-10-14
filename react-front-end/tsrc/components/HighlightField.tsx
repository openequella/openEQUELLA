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
import { styled } from "@mui/material/styles";
import HTMLReactParser from "html-react-parser";
import { highlight } from "../util/TextUtils";
import * as React from "react";

export interface HighlightFieldProps {
  /**
   * The original text displayed in the component.
   */
  content: string;
  /**
   * Keywords that need to be highlighted in the content.
   */
  highlights: string[];
}

const highlightClass = "highlight-field";

const StyledSpan = styled("span")(({ theme }) => ({
  [`& .${highlightClass}`]: {
    color: theme.palette.secondary.main,
  },
}));

/**
 * Displays a text with highlighted keywords.
 */
const HighlightField = ({ content, highlights }: HighlightFieldProps) => (
  <StyledSpan>
    {HTMLReactParser(highlight(content, highlights, highlightClass))}
  </StyledSpan>
);

export default HighlightField;
