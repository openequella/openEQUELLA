import { schemaActions } from './actions';
import { Schema } from '../api/Schema';
import { reducerWithInitialState } from "typescript-fsa-reducers";

let initialState = {
    query: '',
    entities: [] as Schema[],
    loading: false
};

export const schemaReducer = reducerWithInitialState(initialState)
    .case(schemaActions.search.started, (state, data) => {
        return state;
    })
    .case(schemaActions.search.done, (state, success) => {
        return { ...state, entities: success.result.results.results };
    })
    .case(schemaActions.read.started, (state, data) => {
        return state;
    })
    .case(schemaActions.read.done, (state, success) => {
        return { ...state, editingEntity: success.result.result };
    })
    .case(schemaActions.update.started, (state, data) => {
        return state;
    })
    .case(schemaActions.update.done, (state, success) => {
        return state;
    })
    .build();