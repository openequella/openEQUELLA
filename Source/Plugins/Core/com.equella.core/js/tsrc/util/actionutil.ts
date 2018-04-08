import { Dispatch } from 'redux';
import { AsyncActionCreators } from 'typescript-fsa'
import actionCreatorFactory from 'typescript-fsa';
import { Entity } from '../api/Entity';
import { SearchResults } from '../api/General';

export const actionCreator = actionCreatorFactory();

export interface EntityCrudActions<E extends Entity> {
  create: AsyncActionCreators<{entity: E}, {entity: E, result: E}, {entity: E}>;
  update: AsyncActionCreators<{entity: E}, {entity: E, result: E}, {entity: E}>;
  read: AsyncActionCreators<{uuid: string}, {uuid: string, result: E}, {uuid: string}>;
  delete: AsyncActionCreators<{uuid: string}, {uuid: string}, {uuid: string}>;
  search: AsyncActionCreators<{query?: string}, {query?: string, results: SearchResults<E>}, {query?: string}>;
}

export function entityCrudActions<E extends Entity>(entityType: string): EntityCrudActions<E> {
  const createUpdate = actionCreator.async<{entity: E}, {entity: E, result: E}, {entity: E}>('SAVE_' + entityType);
  return {
    create: createUpdate,
    update: createUpdate,
    read: actionCreator.async<{uuid: string}, {uuid: string, result: E}, {uuid: string}>('LOAD_' + entityType),
    delete: actionCreator.async<{uuid: string}, {uuid: string}, {uuid: string}>('DELETE_' + entityType),
    search: actionCreator.async<{query?: string}, {query?: string, results: SearchResults<E>}, {query?: string}>('SEARCH_' + entityType)
  };
}

// https://github.com/aikoven/typescript-fsa/issues/5#issuecomment-255347353
export function wrapAsyncWorker<TParameters, TSuccess, TError>(
    asyncAction: AsyncActionCreators<TParameters, TSuccess, TError>,
    worker: (params: TParameters) => Promise<TSuccess>,
  ) {
    return function wrappedWorker(dispatch: Dispatch<any>, params: TParameters): Promise<TSuccess> {
      dispatch(asyncAction.started(params));
      return worker(params).then(result => {
        dispatch(asyncAction.done({ params, result }));
        return result;
      }, (error: TError) => {
        dispatch(asyncAction.failed({ params, error }));
        throw error;
      });
    };
  }