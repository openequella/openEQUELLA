import { combineReducers } from 'redux'
import { CoursesAction, ReceiveCoursesAction } from './actions';
import { CourseStoreState } from './CourseStore';
import { RECEIVE_COURSES } from './actions';
import { Course } from './CourseModel';

let initialState: CourseStoreState = {
    query: '',
    courses: [],
    loading: false
};

// Pro tip! Although I don't think it's mentioned ANYWHERE,
// the name of the reducer is the name of the state property it reduces to

function courses(state: Course[] = initialState.courses, action: CoursesAction): Course[] {
    switch (action.type){
        case RECEIVE_COURSES:
            let rcA = action as ReceiveCoursesAction;
            return rcA.courses;
        default:
            return state;
    }
}

function query(state: string = initialState.query!, action: CoursesAction): string {
    return action.query || '';
}

export const CourseReducer = combineReducers<CourseStoreState>(
    { 
        courses, 
        query
    }
);
