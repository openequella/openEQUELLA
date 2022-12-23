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
import MUILink from "@mui/material/Link";
import { ReactNode } from "react";
import * as React from "react";
import { Link } from "react-router-dom";
import { isSelectionSessionOpen } from "../modules/LegacySelectionSessionModule";

interface OEQLinkProps {
  /**
   * The content of a link
   */
  children: ReactNode;
  /**
   * A function providing a URL for React Router Link
   */
  routeLinkUrlProvider: () => string;
  /**
   * A function providing a URL for MUI Link
   */
  muiLinkUrlProvider: () => string;
  /**
   * Function typically used to intercept calls before the browser navigates away.
   *
   */
  onClick?: (e: React.MouseEvent<HTMLAnchorElement>) => void;
}

/**
 * Provide a Link which is either a React Route Link or a MUI Link, depending
 * on whether Selection Session is open or not.
 */
export const OEQLink = ({
  children,
  routeLinkUrlProvider,
  muiLinkUrlProvider,
  onClick,
}: OEQLinkProps) =>
  isSelectionSessionOpen() ? (
    <MUILink href={muiLinkUrlProvider()} underline="none" onClick={onClick}>
      {children}
    </MUILink>
  ) : (
    <Link to={routeLinkUrlProvider()} onClick={onClick}>
      {children}
    </Link>
  );
