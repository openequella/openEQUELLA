import axios from 'axios';
import { Dispatch } from 'redux';
import { AsyncActionCreators } from 'typescript-fsa';
import { ReducerBuilder } from "typescript-fsa-reducers/dist";
import { Entity } from '../api/Entity';
import { Config } from '../config';
import { actionCreator, wrapAsyncWorker } from '../util/actionutil';
import { IDictionary } from '../util/dictionary';
import { encodeQuery } from '../util/encodequery';
import { prepLangStrings } from '../util/langstrings';
import { crudActions, EditObjectDispatchProps, EditObjectProps, EditObjectStateProps, ObjectApiInvoker, ObjectCrudActions, ObjectService, ObjectState, ObjectWorkers, PartialObjectState, reducerBuilder, workers } from './generic';

export function extendedEntityService<E extends Entity, XC extends {}, XW extends {}>(entityType: string, extCrud?: XC, extWorkers?: XW, extValidate?: (entity: E, errors: IDictionary<string>) => void): EntityService<E, XC, XW> {
    const baseActions = entityCrudActions<E>(entityType);
    const baseWorkers = entityWorkers(baseActions, extValidate);
    const actions: EntityCrudActions<E> & XC = Object.assign({}, baseActions, extCrud);
    const workers: EntityWorkers<E> & XW = Object.assign({}, baseWorkers, extWorkers);
    
    return {
        actions: actions,
        workers: workers,
        reducer: entityReducerBuilder(baseActions)
    };
}

export function entityService<E extends Entity>(entityType: string): EntityService<E, {}, {}> {
    return extendedEntityService<E, {}, {}>(entityType);
}

export const entityStrings = prepLangStrings("entity", {
    edit: {
        tab: {
            permissions: "Permissions"
        }
    },
    validate: {
        required: {
            name: 'Name is required'
        }
    }
});

export interface EditEntityStateProps<E extends Entity> extends EditObjectStateProps<E> {
}

export interface EditEntityDispatchProps<E extends Entity> extends EditObjectDispatchProps<E> {
}

export interface EditEntityProps<E extends Entity> extends EditObjectProps, EditEntityStateProps<E>, EditEntityDispatchProps<E> {
}

export interface PartialEntityState<E extends Entity> extends PartialObjectState<E> {
    query?: string;
}

export interface EntityState<E extends Entity> extends PartialEntityState<E>, ObjectState<E> {
    loading: boolean;
}


interface EntityCrudActions<E extends Entity> extends ObjectCrudActions<E> {
    checkPrivs: AsyncActionCreators<{privilege: string[]}, string[], void>;
}
  
interface EntityWorkers<E extends Entity> extends ObjectWorkers<E> {
    checkPrivs: (dispatch: Dispatch<any>, params: { privilege: string[] }) => Promise<string[]>;
}
  
interface EntityService<E extends Entity, XC extends {}, XW extends {}> extends ObjectService<E, XC, XW> {
    actions: EntityCrudActions<E> & XC;
    workers: EntityWorkers<E> & XW;
    reducer: ReducerBuilder<PartialEntityState<E>, PartialEntityState<E>>;
}

function entityCrudActions<E extends Entity>(entityType: string): EntityCrudActions<E> {
    return Object.assign({}, crudActions(entityType), {
        checkPrivs: actionCreator.async<{privilege:string[]}, string[], void>('CHECKPRIVS_' + entityType)
    }) as EntityCrudActions<E>;
}

function entityWorkers<E extends Entity>(crudActions: EntityCrudActions<E>, extValidate?: (entity: E, errors: IDictionary<string>) => void): EntityWorkers<E> {
    const entityLower = crudActions.objectType.toLowerCase();
    const invoker: ObjectApiInvoker<E> = {
        doCreateUpdate: function(object: E){
            // FIXME: edit a specific locale:
            const postEntity = Object.assign({}, object, { nameStrings: { en: object.name }, descriptionStrings: {en: object.description } });
            if (object.uuid){
                return axios.put<E>(`${Config.baseUrl}api/${entityLower}/${object.uuid}`, postEntity)
                    .then(res => ({ result: res.data})); 
            }
            else {
                return axios.post<E>(`${Config.baseUrl}api/${entityLower}/`, postEntity)
                    .then(res => ({ result: res.data})); 
            }
        },
        doDelete: function(id: string){
            return axios.delete(`${Config.baseUrl}api/${entityLower}/${id}`)
                .then(res => ({id}));
        },
        doRead: function(id: string){
            return axios.get<E>(`${Config.baseUrl}api/${entityLower}/${id}`)
                .then(res => ({ result: res.data})); 
        },
        doValidate: function(object: E){
            const validationErrors = {};
            if (!object.name){
                validationErrors['name'] = entityStrings.validate.required.name;
            }
            if (extValidate){
                extValidate(object, validationErrors);
            }
            return validationErrors;
        }
    }; 
    const baseWorkers = workers(crudActions, invoker);
    const res = Object.assign({}, baseWorkers, {
        checkPrivs: wrapAsyncWorker(crudActions.checkPrivs,
            (param): Promise<string[]> => {
                const {privilege} = param;
                const qs = encodeQuery({privilege});
                return axios.get<string[]>(`${Config.baseUrl}api/acl/privilegecheck${qs}`).then(res => (res.data));
            })
        });
    return res;
}

function entityReducerBuilder<E extends Entity>(crudActions: EntityCrudActions<E>){
    const initialState: PartialEntityState<E> = {
        query: '',
        loading: false
    };
    return reducerBuilder(crudActions, initialState);
}