import { compose, createStore, applyMiddleware, combineReducers } from 'redux';
import { courseService, schemaService } from './services';
import thunkMiddleware from 'redux-thunk';
import { createLogger } from 'redux-logger';
import { Course, Schema } from './api';
import { Entity } from './api/Entity';

export interface PartialEntityState<E extends Entity> {
    query?: string;
    entities?: E[];
    editingEntity?: E;
    loading?: boolean;
}

export interface EntityState<E extends Entity> extends PartialEntityState<E> {
    entities: E[];
    loading: boolean;
}

export interface CourseState extends EntityState<Course> {}
export interface SchemaState extends EntityState<Schema> {}

export class StoreState {
    course: CourseState;
    schema: SchemaState;
}


const loggerMiddleware = createLogger();

const composeEnhancers = window['__REDUX_DEVTOOLS_EXTENSION_COMPOSE__'] || compose;
const store = createStore<StoreState>(
                        combineReducers({ course: courseService.reducer, schema: schemaService.reducer }),
                        composeEnhancers(applyMiddleware(thunkMiddleware, loggerMiddleware))
                    );


export default store;
