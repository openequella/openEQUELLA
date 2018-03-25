import { searchCourses, loadCourse, saveCourse } from './actions';
import { CourseStoreState } from './CourseStore';
import { Course } from './CourseModel';
import { reducerWithInitialState } from "typescript-fsa-reducers";
import * as API from '../api';

let initialState: CourseStoreState = {
    query: '',
    courses: [],
    loading: false
};

function transformApiCourse(course: API.Course): Course {
    let { uuid, name, code, description, departmentName, citation, students,
        from, until, versionSelection, archived } = course;
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

export const CourseReducer = reducerWithInitialState(initialState)
    .case(searchCourses.started, (state, data) => {
        return state;
    })
    .case(searchCourses.done, (state, success) => {
        return { ...state, courses: success.result.results.results.map(transformApiCourse) };
    })
    .case(loadCourse.started, (state, data) => {
        return state;
    })
    .case(loadCourse.done, (state, success) => {
        return { ...state, editingCourse: transformApiCourse(success.result.result) };
    })
    .case(saveCourse.started, (state, data) => {
        return state;
    })
    .case(saveCourse.done, (state, success) => {
        return state;
    })
    .build();