import { courseActions } from './actions';
import { Course } from '../api/Course';
import { reducerWithInitialState } from "typescript-fsa-reducers";

let initialState = {
    query: '',
    entities: [] as Course[],
    loading: false
};

export const courseReducer = reducerWithInitialState(initialState)
    .case(courseActions.search.started, (state, data) => {
        return state;
    })
    .case(courseActions.search.done, (state, success) => {
        return { ...state, entities: success.result.results.results };
    })
    .case(courseActions.read.started, (state, data) => {
        return state;
    })
    .case(courseActions.read.done, (state, success) => {
        return { ...state, editingEntity: success.result.result };
    })
    .case(courseActions.update.started, (state, data) => {
        return state;
    })
    .case(courseActions.update.done, (state, success) => {
        return state;
    })
    .build();