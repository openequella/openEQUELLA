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
import Axios from "axios";
import { Course, PagingResults } from "../api";
import { Config } from "../config";
import { EntityState, extendedEntityService } from "../entity/index";
import { IDictionary } from "../util/dictionary";
import { encodeQuery } from "../util/encodequery";
import * as validation from "../util/validation";

const courseService = extendedEntityService<Course, {}, {}>(
  "COURSE",
  {},
  {},
  validate
);
export default courseService;

export interface CourseState extends EntityState<Course> {}

function validate(entity: Course, errors: IDictionary<string>): void {
  const { code, students } = entity;
  if (!code) {
    errors["code"] = "Code is required";
  }

  if (!validation.isInteger(students, false, true)) {
    errors["students"] =
      "Unique Individuals must be a positive whole number, or leave blank.";
  }
}

export function searchCourses(
  query: string,
  includeArchived: boolean,
  length: number,
  resumption?: string | undefined
): Promise<PagingResults<Course>> {
  const qs = encodeQuery({
    q: query,
    length,
    archived: includeArchived,
    resumption,
    privilege: ["EDIT_COURSE_INFO", "DELETE_COURSE_INFO"],
  });
  return Axios.get<PagingResults<Course>>(
    `${Config.baseUrl}api/course${qs}`
  ).then((res) => res.data);
}
