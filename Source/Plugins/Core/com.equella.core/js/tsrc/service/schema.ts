import axios from 'axios';
import { Dispatch } from 'redux';
import { AsyncActionCreators } from 'typescript-fsa';
import { Schema } from '../api';
import { Config } from '../config';
import { actionCreator, wrapAsyncWorker } from '../util/actionutil';
import { EntityState, extendedEntityService } from './entity';


interface SchemaExtActions {
    citations: AsyncActionCreators<{}, string[], void>;
}

interface SchemaExtWorkers {
    citations: (dispatch: Dispatch<any>, params: {}) => Promise<string[]>;
}

const actions: SchemaExtActions = {
    citations: actionCreator.async<{}, string[], void>('LOAD_CITATIONS')
};

const workers: SchemaExtWorkers = {
    citations: wrapAsyncWorker(actions.citations, 
        (params): Promise<string[]> => { 
            return axios.get<string[]>(`${Config.baseUrl}api/schema/citation`)
                .then(res => (res.data)); 
        }
      )
};

const schemaService = extendedEntityService<Schema, SchemaExtActions, SchemaExtWorkers>('SCHEMA', actions, workers);
schemaService.reducer
    .case(actions.citations.started, (state) => { return state; })
    .case(actions.citations.done, (state, success) => { return { ...state, citations: success.result }; });

export default schemaService;


export interface SchemaState extends EntityState<Schema> {
    citations?: string[];
}
