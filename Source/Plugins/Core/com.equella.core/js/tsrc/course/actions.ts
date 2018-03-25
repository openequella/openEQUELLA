import { Course } from './CourseModel';
import { Dispatch } from 'redux';
import axios from 'axios';
import * as API from '../api';
import { INST_URL } from '../config';

import actionCreatorFactory from 'typescript-fsa';


import { AsyncActionCreators } from 'typescript-fsa'



// https://github.com/aikoven/typescript-fsa/issues/5#issuecomment-255347353
function wrapAsyncWorker<TParameters, TSuccess, TError>(
  asyncAction: AsyncActionCreators<TParameters, TSuccess, TError>,
  worker: (params: TParameters) => Promise<TSuccess>,
) {
  return function wrappedWorker(dispatch: Dispatch<any>, params: TParameters): Promise<TSuccess> {
    dispatch(asyncAction.started(params));
    return worker(params).then(result => {
      dispatch(asyncAction.done({ params, result }));
      return result;
    }, (error: TError) => {
      dispatch(asyncAction.failed({ params, error }));
      throw error;
    });
  };
}




const actionCreator = actionCreatorFactory();

export const searchCourses = actionCreator.async<
    {query?: string}, 
    {query?: string, results: API.CourseList}, 
    {query?: string}>('SEARCH_COURSES');

export const loadCourse = actionCreator.async<
    {uuid: string}, 
    {uuid: string, result: API.Course}, 
    {uuid: string}>('LOAD_COURSE');

export const saveCourse = actionCreator.async<
    {course: Course}, 
    {course: Course, result: API.Course}, 
    {course: Course}>('SAVE_COURSE');


export const searchCoursesWorker =  
    wrapAsyncWorker(searchCourses, 
        (param): Promise<{query?: string, results: API.CourseList}> => { 
            const { query } = param;
            const qs = (!query ? '' : `?code=${encodeURIComponent(query)}`);
            return axios.get<API.CourseList>(`${INST_URL}api/course${qs}`)
                .then(res => ({ query, results: res.data})); 
        }
    );


export const loadCourseWorker =  
    wrapAsyncWorker(loadCourse, 
        (param): Promise<{uuid: string, result: API.Course}> => { 
            const { uuid } = param;
            return axios.get<API.Course>(`${INST_URL}api/course/${uuid}`)
                .then(res => ({ uuid, result: res.data})); 
        }
    );

export const saveCourseWorker =  
    wrapAsyncWorker(saveCourse, 
        (param): Promise<{course: Course, result: API.Course}> => { 
            const { course } = param;
            if (course.uuid){
                return axios.put<API.Course>(`${INST_URL}api/course/${course.uuid}`, transformToApiCourse(course))
                    .then(res => ({ course, result: res.data})); 
            }
            else {
                return axios.post<API.Course>(`${INST_URL}api/course/`, transformToApiCourse(course))
                    .then(res => ({ course, result: res.data})); 
            }
            
        }
    );

function transformToApiCourse(course: Course): API.Course {
    let { uuid, name, code, description, departmentName, 
        citation, students, from, until, versionSelection, archived } = course;
    return {
        uuid,
        name,
        code,
        description,
        departmentName,
        citation,
        students,
        from,
        until,
        versionSelection,
        archived
    };
}