import { compose, createStore, applyMiddleware, combineReducers } from 'redux';
import { courseReducer, schemaReducer } from './reducers';
import thunkMiddleware from 'redux-thunk';
import { createLogger } from 'redux-logger';
import { Course, Schema } from './api';
//import history from './history';
//import { routerReducer, routerMiddleware } from 'react-router-redux';
import { Entity } from './api/Entity';

export interface EntityState<E extends Entity> {
    query?: string;
    entities: E[];
    editingEntity?: E;
    loading: false;
}

export interface CourseState extends EntityState<Course> {}
export interface SchemaState extends EntityState<Schema> {}

export class StoreState {
    course: CourseState;
    schema: SchemaState;
}

/*
function reduceReducers(...reducers: any[]) {
    return (previous: any, current: any) =>
        reducers.reduce(
            (p, r) => r(p, current),
            previous
        );
}*/

const loggerMiddleware = createLogger();
//const historyMiddleware = routerMiddleware(history);

const composeEnhancers = window['__REDUX_DEVTOOLS_EXTENSION_COMPOSE__'] || compose;
const store = createStore<StoreState>(
                        //reduceReducers(CourseReducer, routerReducer), 
                        combineReducers({ course: courseReducer, schema: schemaReducer/*, router: routerReducer*/}),
                        composeEnhancers(applyMiddleware(thunkMiddleware, loggerMiddleware /*, historyMiddleware*/)
                    ));


export default store;
