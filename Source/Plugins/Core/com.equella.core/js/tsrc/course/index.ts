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
    privilege: ["EDIT_COURSE_INFO", "DELETE_COURSE_INFO"]
  });
  return Axios.get<PagingResults<Course>>(
    `${Config.baseUrl}api/course${qs}`
  ).then(res => res.data);
}
