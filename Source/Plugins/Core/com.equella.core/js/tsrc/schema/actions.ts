import axios from 'axios';
import { Schema, SchemaList } from '../api';
import { Config } from '../config';
import { wrapAsyncWorker, entityCrudActions } from '../util/actionutil'

export const schemaActions = entityCrudActions<Schema>('SCHEMA');

export const searchSchemasWorker =  
    wrapAsyncWorker(schemaActions.search, 
        (param): Promise<{query?: string, results: SchemaList}> => { 
            const { query } = param;
            const qs = ''; // (!query ? '' : `?code=${encodeURIComponent(query)}`);
            return axios.get<SchemaList>(`${Config.baseUrl}api/schema${qs}`)
                .then(res => ({ query, results: res.data})); 
        }
    );


export const loadSchemaWorker =  
    wrapAsyncWorker(schemaActions.read, 
        (param): Promise<{uuid: string, result: Schema}> => { 
            const { uuid } = param;
            return axios.get<Schema>(`${Config.baseUrl}api/schema/${uuid}`)
                .then(res => ({ uuid, result: res.data})); 
        }
    );

export const saveSchemaWorker =  
    wrapAsyncWorker(schemaActions.update, 
        (param): Promise<{entity: Schema, result: Schema}> => { 
            const { entity } = param;
            if (entity.uuid){
                return axios.put<Schema>(`${Config.baseUrl}api/schema/${entity.uuid}`, entity)
                    .then(res => ({ entity, result: res.data})); 
            }
            else {
                return axios.post<Schema>(`${Config.baseUrl}api/schema/`, entity)
                    .then(res => ({ entity, result: res.data})); 
            }
            
        }
    );