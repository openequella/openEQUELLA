/* import axios from 'axios';
import { Dispatch } from 'redux';
import { AsyncActionCreators } from 'typescript-fsa';
import { ReducerBuilder, reducerWithInitialState } from "typescript-fsa-reducers/dist";
import { Config } from '../config';
import { actionCreator, wrapAsyncWorker } from '../util/actionutil';
import { IDictionary } from '../util/dictionary';

const acl = aclService();
export default acl;

export interface AclState extends PartialAclState {
}


function aclService() {
    const actions = aclActions();
    return {
        actions,
        workers: aclWorkers(actions),
        reducer: aclReducerBuilder(actions)
    };
}

interface AclActions {
    listPrivileges: AsyncActionCreators<{node: string}, {node: string, result: string[]}, void>;
} 

interface AclWorkers {
    listPrivileges: (dispatch: Dispatch<any>, params: {node: string}) => Promise<{node: string, result: string[]}>;
}

interface PartialAclState {
    nodes: IDictionary<string[]>
}

function aclActions(): AclActions {
    return {
        listPrivileges: actionCreator.async<{node: string}, {node: string, result: string[]}, void>('LIST_PRIVILEGES_FOR_NODE')
    };
}

function aclWorkers(actions: AclActions): AclWorkers {
    return {
        listPrivileges: wrapAsyncWorker(actions.listPrivileges, 
            (param): Promise<{node: string, result: string[]}> => { 
                const { node } = param;
                return axios.get<string[]>(`${Config.baseUrl}api/acl/privileges?node=${node}`)
                    .then(res => ({ node, result: res.data})); 
            }
          )
    };
}

function aclReducerBuilder(actions: AclActions): ReducerBuilder<PartialAclState, PartialAclState> {
    let initialState: PartialAclState = {
        nodes: {}
    };

    return reducerWithInitialState(initialState)
        .case(actions.listPrivileges.started, (state, data) => {
            return state;
        })
        .case(actions.listPrivileges.done, (state, success) => {
            const nodes = state.nodes;
            return { ...state, nodes: { ...nodes, [success.result.node]: success.result.result }};
        });
} */