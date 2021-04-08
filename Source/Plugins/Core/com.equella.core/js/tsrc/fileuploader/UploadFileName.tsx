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

interface UploadFileNameProps {
  /**
   * The file's name
   */
  fileName: string;
  /**
   * The link to preview the file
   */
  link?: string;
  /**
   * `true` if the entry should be indented
   */
  indented: boolean;
}

/**
 * Display a file name in plain text or as a link, depending on whether the link URL is provided or not.
 * Indentation is achieved by reusing oEQ existing CSS styles.
 */
export const UploadFileName = ({
  fileName,
  link,
  indented,
}: UploadFileNameProps) => (
  <div className={indented ? "indent" : ""}>
    {link ? (
      <Link href={link} target="_blank" rel="noopener noreferrer">
        {fileName}
      </Link>
    ) : (
      <div>{fileName}</div>
    )}
  </div>
);
