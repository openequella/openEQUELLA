import { Course } from '../api';
import { entityService } from '../entity/index';

const courseService = entityService<Course>('COURSE');
export default courseService;