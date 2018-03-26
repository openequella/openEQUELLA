import { searchCourses, loadCourse, saveCourse } from './actions';
import { CourseStoreState } from './CourseStore';
import { reducerWithInitialState } from "typescript-fsa-reducers";

let initialState: CourseStoreState = {
    query: '',
    courses: [],
    loading: false
};

export const CourseReducer = reducerWithInitialState(initialState)
    .case(searchCourses.started, (state, data) => {
        return state;
    })
    .case(searchCourses.done, (state, success) => {
        return { ...state, courses: success.result.results.results };
    })
    .case(loadCourse.started, (state, data) => {
        return state;
    })
    .case(loadCourse.done, (state, success) => {
        return { ...state, editingCourse: success.result.result };
    })
    .case(saveCourse.started, (state, data) => {
        return state;
    })
    .case(saveCourse.done, (state, success) => {
        return state;
    })
    .build();