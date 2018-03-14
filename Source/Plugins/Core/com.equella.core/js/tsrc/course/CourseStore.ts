import { compose, createStore, applyMiddleware } from 'redux';
import { CourseReducer } from './reducers';
import thunkMiddleware from 'redux-thunk';
import { createLogger } from 'redux-logger';
import { fetchCourses } from './actions';
import { Course } from './CourseModel';
import history from '../history';
import { routerReducer, routerMiddleware } from 'react-router-redux'

export class CourseStoreState {
    query?: string;
    courses: Course[];
    editingCourse?: Course;
    loading: false;
}

function reduceReducers(...reducers: any[]) {
    return (previous: any, current: any) =>
        reducers.reduce(
            (p, r) => r(p, current),
            previous
        );    
}

const loggerMiddleware = createLogger();
const historyMiddleware = routerMiddleware(history);

const composeEnhancers = window['__REDUX_DEVTOOLS_EXTENSION_COMPOSE__'] || compose;
const CourseStore = createStore<CourseStoreState>(
                        reduceReducers(CourseReducer, routerReducer), 
                        composeEnhancers(applyMiddleware(thunkMiddleware, loggerMiddleware, historyMiddleware)
                    ));

CourseStore.dispatch(fetchCourses as any);

export default CourseStore;
