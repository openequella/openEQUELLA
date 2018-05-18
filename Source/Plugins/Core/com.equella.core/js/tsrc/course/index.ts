import { Course } from '../api';
import { EntityState, entityService } from '../entity/index';

const courseService = entityService<Course>('COURSE');
export default courseService;

export interface CourseState extends EntityState<Course> {}