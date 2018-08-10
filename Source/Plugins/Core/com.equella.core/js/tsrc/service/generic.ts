import { Dispatch } from 'redux';
import { Action, ActionCreator, AsyncActionCreators } from 'typescript-fsa';
import { ReducerBuilder, reducerWithInitialState } from "typescript-fsa-reducers/dist";
import { Bridge } from '../api/bridge';
import { actionCreator, wrapAsyncWorker } from '../util/actionutil';
import { IDictionary } from '../util/dictionary';



export function extendedObjectService<E, XC extends {}, XW extends {}>(
                            objectType: string, 
                            invoker: ObjectApiInvoker<E>,
                            initialState: PartialObjectState<E>,
                            extCrud?: XC, 
                            extWorkers?: XW): ObjectService<E, XC, XW> {
    const baseActions = crudActions<E>(objectType);
    const baseWorkers = workers(baseActions, invoker);
    
    return {
        actions: Object.assign({}, baseActions, extCrud),
        workers: Object.assign({}, baseWorkers, extWorkers),
        reducer: reducerBuilder(baseActions, initialState)
    };
}

export function objectService<E>(
                            objectType: string,
                            invoker: ObjectApiInvoker<E>,
                            initialState: PartialObjectState<E>,): ObjectService<E, {}, {}> {
    return extendedObjectService<E, {}, {}>(objectType, invoker, initialState);
}



export interface EditObjectProps {
	bridge: Bridge;
	id: string;
}

export interface ListObjectProps {
	bridge: Bridge;
}

export interface EditObjectStateProps<E> {
    loading?: boolean;
    object: E | undefined;
}

export interface ListObjectStateProps<E> {
    loading?: boolean;
    results: E[] | undefined;
}

export interface EditObjectDispatchProps<E> {
    loadObject: (id: string) => Promise<{result: E}>;
    saveObject: (object: E) => Promise<{result: E}>;
    modifyObject: (object: E) => Action<{object: E}>;
    validateObject: (object: E) => Promise<IDictionary<string>>;
}

export interface ObjectCrudActions<E> {
    objectType: string;
    create: AsyncActionCreators<{object: E}, {result: E}, void>;
    update: AsyncActionCreators<{object: E}, {result: E}, void>;
    read: AsyncActionCreators<{id: string}, {result: E}, void>;
    delete: AsyncActionCreators<{id: string}, {id: string}, void>;
    validate: AsyncActionCreators<{object: E}, IDictionary<string>, void>;
    // This is for temp modifications.  E.g. uncommitted changes before save
    modify: ActionCreator<{object: E}>;
}

export interface PartialObjectState<E> {
    editingObject?: E;
    loading?: boolean;
}

export interface ObjectState<E> extends PartialObjectState<E> {
    loading: boolean;
}

export interface ObjectWorkers<E> {
    objectType: string;
    create: (dispatch: Dispatch<any>, params: { object: E; }) => Promise<{ result: E; }>;
    update: (dispatch: Dispatch<any>, params: { object: E; }) => Promise<{ result: E; }>;
    read: (dispatch: Dispatch<any>, params: { id: string; }) => Promise<{ result: E; }>;
    delete: (dispatch: Dispatch<any>, params: { id: string; }) => Promise<{ id: string }>;
    validate: (dispatch: Dispatch<any>, params: { object: E }) => Promise<IDictionary<string>>;
}

export interface ObjectApiInvoker<E> {
    doDelete: (id: string) => Promise<{ id: string }>;
    doCreateUpdate: (object: E) => Promise<{ result: E }>;
    doValidate: (object: E) => IDictionary<string>;
    doRead: (id: string) => Promise<{ result: E }>;

}

export interface ObjectService<E, XC extends {}, XW extends {}> {
    actions: ObjectCrudActions<E> & XC;
    workers: ObjectWorkers<E> & XW;
    reducer: ReducerBuilder<PartialObjectState<E>, PartialObjectState<E>>;
}

export function crudActions<E>(objectType: string): ObjectCrudActions<E> {
    const createUpdate = actionCreator.async<{object: E}, {result: E}, void>('SAVE_' + objectType);
    return {
        objectType: objectType,
        create: createUpdate,
        update: createUpdate,
        read: actionCreator.async<{id: string}, {result: E}, void>('LOAD_' + objectType),
        delete: actionCreator.async<{id: string}, {id: string}, void>('DELETE_' + objectType),
        modify: actionCreator<{object: E}>('MODIFY_' + objectType),
        validate: actionCreator.async<{object: E}, IDictionary<string>, void>('VALIDATE_' + objectType)
    };
}

export function workers<E>(crudActions: ObjectCrudActions<E>, 
                            invoker: ObjectApiInvoker<E>
                        ): ObjectWorkers<E> {
    const createUpdate = wrapAsyncWorker(crudActions.update, 
        (param): Promise<{result: E}> => { 
            const { object } = param;
            return invoker.doCreateUpdate(object); 
        }
    );
    return {
      objectType: crudActions.objectType,
      create: createUpdate,
      update: createUpdate,
      read: wrapAsyncWorker(crudActions.read, 
        (param): Promise<{result: E}> => { 
            const { id } = param;
            return invoker.doRead(id);
        }),
      delete: wrapAsyncWorker(crudActions.delete, 
        (param): Promise<{id:string}> => {
            const { id } = param;
            return invoker.doDelete(id);
        }),
      validate: wrapAsyncWorker(crudActions.validate, 
        (param): Promise<IDictionary<string>> => {
            return Promise.resolve(invoker.doValidate(param.object));
        })
    };
}

export function reducerBuilder<E, S extends PartialObjectState<E>>(crudActions: ObjectCrudActions<E>, initialState: S): ReducerBuilder<S, S> {
    return reducerWithInitialState(initialState)
        .case(crudActions.read.started, (state, data) => {
            return Object.assign({}, state, { editingObject: undefined, loading: true });
        })
        .case(crudActions.read.done, (state, success) => {
            return Object.assign({}, state, { editingObject: success.result.result, loading: false });
        })
        .case(crudActions.read.failed, (state, failure) => {
            return Object.assign({}, state, { loading: false });
        })
        .case(crudActions.update.started, (state, data) => {
            return state;
        })
        .case(crudActions.update.done, (state, success) => {
            return state;
        })
        .case(crudActions.update.failed, (state, failure) => {
            return state;
        })
        .case(crudActions.modify, (state, payload) => {
            return Object.assign({}, state, { editingObject: payload.object });
        })
        .case(crudActions.validate.done, (state, success) => {
            const editingObject = Object.assign({}, state.editingObject, { validationErrors: success.result });
            return Object.assign({}, state, { editingObject });
        });
};
