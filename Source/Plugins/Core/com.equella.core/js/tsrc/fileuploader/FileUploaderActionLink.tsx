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
import { Link } from "@material-ui/core";
import * as React from "react";

interface FileActionLinkProps {
  /**
   * Fired when clicking the link
   */
  onClick: () => void;
  /**
   * Text of the link
   */
  text: string;
  /**
   * ID of the link
   */
  id?: string;
  /**
   * Custom CSS class applied to the link
   */
  className?: string;
}

export const FileUploaderActionLink = ({
  onClick,
  text,
  id,
  className,
}: FileActionLinkProps) => (
  // This Link is used as a button but we don't use `component="button"` because
  // this will break UI consistency (color & font size) in Old UI.
  <Link
    id={id}
    className={className}
    onClick={onClick}
    title={text}
    role="button"
    style={{ cursor: "pointer" }} // Add this style since this Link does not have 'href'.
  >
    {text}
  </Link>
);
