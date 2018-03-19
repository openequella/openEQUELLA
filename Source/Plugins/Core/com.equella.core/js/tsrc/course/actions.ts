import { Course } from './CourseModel';
import { Dispatch, Action } from 'redux';
import axios from 'axios';
import * as API from '../api';
import { INST_URL } from '../config';

// FIXME: use redux-actions

export const SEARCH_COURSES = 'SEARCH_COURSES';
export type SEARCH_COURSES = typeof SEARCH_COURSES;

export const REQUEST_COURSES = 'REQUEST_COURSES';
export type REQUEST_COURSES = typeof REQUEST_COURSES;

export const RECEIVE_COURSES = 'RECEIVE_COURSES';
export type RECEIVE_COURSES = typeof RECEIVE_COURSES;

export const SAVE_COURSE = 'SAVE_COURSE';
export type SAVE_COURSE = typeof SAVE_COURSE;

export const SAVED_COURSE = 'SAVED_COURSE';
export type SAVED_COURSE = typeof SAVED_COURSE;

export const LOAD_COURSE = 'LOAD_COURSE';
export type LOAD_COURSE = typeof LOAD_COURSE;

export const LOADED_COURSE = 'LOADED_COURSE';
export type LOADED_COURSE = typeof LOADED_COURSE;

export interface CoursesAction extends Action {
    query?: string;
}

export interface SearchCoursesAction extends CoursesAction {
    type: SEARCH_COURSES;
}

export interface RequestCoursesAction extends CoursesAction {
    type: REQUEST_COURSES;
}

export interface ReceiveCoursesAction extends CoursesAction {
    type: RECEIVE_COURSES;
    courses: Course[];
}

export interface SaveCourseAction extends Action {
    type: SAVE_COURSE;
    course: Course;
}

export interface SavedCourseAction extends Action {
    type: SAVED_COURSE;
    course: Course;
}

export interface LoadCourseAction extends Action {
    type: LOAD_COURSE;
    uuid?: string;
}

export interface LoadedCourseAction extends Action {
    type: LOADED_COURSE;
    course: Course;
}

function transformApiCourse(course: API.Course): Course {
    let { uuid, name, code, description } = course;
    return {
        uuid,
        name,
        code,
        description
    };
}

export function searchCoursesAction(query?: string): SearchCoursesAction {
    return {
        type: SEARCH_COURSES,
        query: query
    };
}

function requestCoursesAction(query?: string): RequestCoursesAction {
    return {
        type: REQUEST_COURSES,
        query: query
    };
}

function receiveCoursesAction(results: API.CourseList, query?: string): ReceiveCoursesAction {
    return {
        type: RECEIVE_COURSES,
        query: query,
        courses: results.results.map(transformApiCourse)
    };
}

export function saveCourseAction(course: Course): SaveCourseAction {
    return {
        type: SAVE_COURSE,
        course: course
    };
}

function savedCourseAction(course: API.Course): SavedCourseAction {
    return {
        type: SAVED_COURSE,
        course: course
    };
}

export function loadCourseAction(uuid: string): LoadCourseAction {
    return {
        type: LOAD_COURSE,
        uuid: uuid
    };
}

function loadedCourseAction(course: API.Course): LoadedCourseAction {
    return {
        type: LOADED_COURSE,
        course: transformApiCourse(course)
    };
}

function _fetchCourses(query?: string): (dispatch: Dispatch<RequestCoursesAction>) => Promise<ReceiveCoursesAction> {
    return (dispatch: Dispatch<RequestCoursesAction>): Promise<ReceiveCoursesAction> => {
        dispatch(requestCoursesAction(query));
        const qs = (!query ? '' : `?code=${encodeURIComponent(query)}`);
        return axios.get<API.CourseList>(`${INST_URL}api/course${qs}`)
            .then(res => res.data)
            .then(results => dispatch(receiveCoursesAction(results, query)));
    };
}

function _saveCourse(course: Course)/*: (dispatch: Dispatch<SaveCourseAction>) => Promise<SavedCourseAction>*/ {
    return (dispatch: Dispatch<SaveCourseAction>)/*: Promise<SavedCourseAction>*/ => {
        dispatch(saveCourseAction(course));
        const apiCourse: API.Course = {
            name: course.name,
            code: course.code
        }
        const { uuid } = course;
        let path = '';
        if (uuid){
            path = '/' + uuid;
        }
        return axios.post<any>(`${INST_URL}api/course${path}`, apiCourse)
            .then(res => res.headers['Location'])
            .then(location => axios.get<API.Course>(location))
            .then(res => res.data)
            .then(result => dispatch(savedCourseAction(result)));
    };
}

function _loadCourse(uuid?: string) {
    return (dispatch: Dispatch<LoadCourseAction>) => {
        if (!uuid){
            return dispatch(loadedCourseAction({
                name: '',
                code: ''
            }));
        }

        dispatch(loadCourseAction(uuid));
        return axios.get<API.Course>(`${INST_URL}api/course/${uuid}`)
            .then(res => res.data)
            .then(result => dispatch(loadedCourseAction(result)));
    };
}

export function fetchCourses(query?: string){
    return (dispatch: any /*Dispatch<Promise<ReceiveCourses>>*/) => dispatch(_fetchCourses(query));
}

export function searchCourses(query?: string){
    return (dispatch: any /*Dispatch<Promise<ReceiveCourses>>*/) => dispatch(fetchCourses(query));
}

export function saveCourse(course: Course){
    return (dispatch: any) => dispatch(_saveCourse(course));
}

export function loadCourse(uuid?: string){
    return (dispatch: any) => dispatch(_loadCourse(uuid));
}
/*
  function shouldFetchPosts(state, subreddit) {
    const posts = state.postsBySubreddit[subreddit]
    if (!posts) {
      return true
    } else if (posts.isFetching) {
      return false
    } else {
      return posts.didInvalidate
    }
  }*/