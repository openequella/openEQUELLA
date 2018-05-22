import { Course, PagingResults } from '../api';
import { EntityState, extendedEntityService } from '../entity/index';
import { IDictionary } from '../util/dictionary';
import Axios from 'axios';
import { Config } from '../config';
import { encodeQuery } from '../util/encodequery';

const courseService = extendedEntityService<Course, {}, {}>('COURSE', {}, {}, validate);
export default courseService;

export interface CourseState extends EntityState<Course> {}

function validate(entity: Course, errors: IDictionary<string>): void {
    if (!entity.code){
        errors['code'] = 'Code is required';
    }
}

export function searchCourses(query: string, includeArchived: boolean, resumption: string|undefined, length: number): Promise<PagingResults<Course>>
{
    const qs = encodeQuery({q: query, length, archived: includeArchived, resumption, privilege:["EDIT_COURSE_INFO", "DELETE_COURSE_INFO"]})
    return Axios.get<PagingResults<Course>>(`${Config.baseUrl}api/course${qs}`).then(res => (res.data)); 
}