import axios from 'axios';
import { Course, CourseList } from '../api';
import { Config } from '../config';
import { wrapAsyncWorker, entityCrudActions } from '../util/actionutil'


export const courseActions = entityCrudActions<Course>('COURSE');

export const searchCoursesWorker =  
    wrapAsyncWorker(courseActions.search, 
        (param): Promise<{query?: string, results: CourseList}> => { 
            const { query } = param;
            const qs = (!query ? '' : `?code=${encodeURIComponent(query)}`);
            return axios.get<CourseList>(`${Config.baseUrl}api/course${qs}`)
                .then(res => ({ query, results: res.data})); 
        }
    );


export const loadCourseWorker =  
    wrapAsyncWorker(courseActions.read, 
        (param): Promise<{uuid: string, result: Course}> => { 
            const { uuid } = param;
            return axios.get<Course>(`${Config.baseUrl}api/course/${uuid}`)
                .then(res => ({ uuid, result: res.data})); 
        }
    );

export const saveCourseWorker =  
    wrapAsyncWorker(courseActions.update, 
        (param): Promise<{entity: Course, result: Course}> => { 
            const { entity } = param;
            if (entity.uuid){
                return axios.put<Course>(`${Config.baseUrl}api/course/${entity.uuid}`, entity)
                    .then(res => ({ entity, result: res.data})); 
            }
            else {
                return axios.post<Course>(`${Config.baseUrl}api/course/`, entity)
                    .then(res => ({ entity, result: res.data})); 
            }
            
        }
    );