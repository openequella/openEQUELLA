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
import { v4 } from "uuid";
import type {
  AjaxFileEntry,
  UpdateEntry,
  UploadFailed,
} from "../tsrc/modules/FileUploaderModule";

export const uploadedFileEntry: AjaxFileEntry = {
  id: v4(),
  name: "test2.png",
  link: "https://localhost:8080/test/upload/test2",
  preview: true,
  editable: true,
  children: [],
};

export const successfulUploadResponse: UpdateEntry = {
  entry: uploadedFileEntry,
  attachmentDuplicateInfo: {
    displayWarningMessage: true,
    warningMessageWebId: "P0C0",
  },
  response: "updateentry",
};

export const failedUploadResponse: UploadFailed = {
  reason: "File is too large",
  response: "uploadfailed",
};

export const files = [
  new File(["test1"], "test1.png"),
  new File(["test2"], "test2.png"),
];
