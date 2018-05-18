import { Course } from '../api';
import { EntityState, extendedEntityService } from '../entity/index';
import { IDictionary } from '../util/dictionary';

const courseService = extendedEntityService<Course, {}, {}>('COURSE', {}, {}, validate);
export default courseService;

export interface CourseState extends EntityState<Course> {}

function validate(entity: Course, errors: IDictionary<string>): void {
    if (!entity.code){
        errors['code'] = 'Code is required';
    }
}