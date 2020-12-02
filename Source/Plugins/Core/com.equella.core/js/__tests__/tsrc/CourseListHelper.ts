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
import * as LegacySelectionSessionModule from "../../tsrc/modules/LegacySelectionSessionModule";

const mockGlobalCourseList = jest.spyOn(
  LegacySelectionSessionModule,
  "getGlobalCourseList"
);

const basicCourseList = {
  updateCourseList: jest.fn(),
  prepareDraggableAndBind: jest.fn(),
  newSearchPageItemClass: "SearchPage-Item",
  newSearchPageAttachmentClass: "SearchPage-Attachment",
};

export const updateMockGlobalCourseList = (courseList = basicCourseList) =>
  mockGlobalCourseList.mockReturnValue(courseList);
