import { Dispatch } from 'redux';
import { AsyncActionCreators } from 'typescript-fsa';
import axios from 'axios';
import { reducerWithInitialState, ReducerBuilder } from "typescript-fsa-reducers";

import { Entity } from '../api/Entity';
import { SearchResults } from '../api/General';
import { Config } from '../config';
import { actionCreator, wrapAsyncWorker } from '../util/actionutil';
import { PartialEntityState } from '../store';


export interface EntityCrudActions<E extends Entity> {
    entityType: string;
    create: AsyncActionCreators<{entity: E}, {entity: E, result: E}, {entity: E}>;
    update: AsyncActionCreators<{entity: E}, {entity: E, result: E}, {entity: E}>;
    read: AsyncActionCreators<{uuid: string}, {uuid: string, result: E}, {uuid: string}>;
    delete: AsyncActionCreators<{uuid: string}, {uuid: string}, {uuid: string}>;
    search: AsyncActionCreators<{query?: string}, {query?: string, results: SearchResults<E>}, {query?: string}>;
}
  
export interface EntityWorkers<E extends Entity> {
    entityType: string;
    create: (dispatch: Dispatch<any>, params: { entity: E; }) => Promise<{ entity: E; result: E; }>;
    update: (dispatch: Dispatch<any>, params: { entity: E; }) => Promise<{ entity: E; result: E; }>;
    read: (dispatch: Dispatch<any>, params: { uuid: string; }) => Promise<{ uuid: string; result: E; }>;
    search: (dispatch: Dispatch<any>, params: { query?: string | undefined; }) => Promise<{ query?: string | undefined; results: SearchResults<E>; }>;
}
  
export interface EntityService<E extends Entity> {
    actions: EntityCrudActions<E>;
    workers: EntityWorkers<E>;
    reducer: ReducerBuilder<PartialEntityState<E>, PartialEntityState<E>>;
}

export function entityService<E extends Entity>(entityType: string): EntityService<E> {
    const actions = entityCrudActions<E>(entityType);
    return {
        actions: actions,
        workers: entityWorkers(actions),
        reducer: entityReducerBuilder(actions)
    };
}




  
function entityCrudActions<E extends Entity>(entityType: string): EntityCrudActions<E> {
    const createUpdate = actionCreator.async<{entity: E}, {entity: E, result: E}, {entity: E}>('SAVE_' + entityType);
    return {
      entityType,
      create: createUpdate,
      update: createUpdate,
      read: actionCreator.async<{uuid: string}, {uuid: string, result: E}, {uuid: string}>('LOAD_' + entityType),
      delete: actionCreator.async<{uuid: string}, {uuid: string}, {uuid: string}>('DELETE_' + entityType),
      search: actionCreator.async<{query?: string}, {query?: string, results: SearchResults<E>}, {query?: string}>('SEARCH_' + entityType)
    };
  }
  
function entityWorkers<E extends Entity>(entityCrudActions: EntityCrudActions<E>): any {
    const entityLower = entityCrudActions.entityType.toLowerCase();
    const createUpdate = wrapAsyncWorker(entityCrudActions.update, 
      (param): Promise<{entity: E, result: E}> => { 
          const { entity } = param;
          if (entity.uuid){
              return axios.put<E>(`${Config.baseUrl}api/${entityLower}/${entity.uuid}`, entity)
                  .then(res => ({ entity, result: res.data})); 
          }
          else {
              return axios.post<E>(`${Config.baseUrl}api/${entityLower}/`, entity)
                  .then(res => ({ entity, result: res.data})); 
          }   
      }
    );
  
    return {
      create: createUpdate,
      update: createUpdate,
      read: wrapAsyncWorker(entityCrudActions.read, 
        (param): Promise<{uuid: string, result: E}> => { 
            const { uuid } = param;
            return axios.get<E>(`${Config.baseUrl}api/${entityLower}/${uuid}`)
                .then(res => ({ uuid, result: res.data})); 
        }
      ),
      search: wrapAsyncWorker(entityCrudActions.search, 
        (param): Promise<{query?: string, results: SearchResults<E>}> => { 
            const { query } = param;
            const qs = (!query ? '' : `?q=${encodeURIComponent(query)}`);
            return axios.get<SearchResults<E>>(`${Config.baseUrl}api/${entityLower}${qs}`)
                .then(res => ({ query, results: res.data})); 
        }
      )
    };
  }

const entityReducerBuilder = function<E extends Entity>(entityCrudActions: EntityCrudActions<E>): ReducerBuilder<PartialEntityState<E>, PartialEntityState<E>> {
    let initialEntityState: PartialEntityState<E> = {
        query: '',
        entities: [] as E[],
        loading: false
    };

    return reducerWithInitialState(initialEntityState)
        .case(entityCrudActions.search.started, (state, data) => {
            return state;
        })
        .case(entityCrudActions.search.done, (state, success) => {
            return { ...state, entities: success.result.results.results };
        })
        .case(entityCrudActions.read.started, (state, data) => {
            return state;
        })
        .case(entityCrudActions.read.done, (state, success) => {
            return { ...state, editingEntity: success.result.result };
        })
        .case(entityCrudActions.update.started, (state, data) => {
            return state;
        })
        .case(entityCrudActions.update.done, (state, success) => {
            return state;
        });
} 