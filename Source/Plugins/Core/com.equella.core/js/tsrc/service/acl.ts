import axios from 'axios';
import { Dispatch } from 'redux';
import { AsyncActionCreators } from 'typescript-fsa';
import { TargetList } from '../api/acleditor';
import { Config } from '../config';
import { crudActions, ObjectApiInvoker, ObjectService, PartialObjectState, reducerBuilder, workers } from '../service/generic';
import { actionCreator, wrapAsyncWorker } from '../util/actionutil';
import { IDictionary } from '../util/dictionary';

const acl = aclService();
export default acl;

const nodeToEndpointMap = {
	INSTITUTION: 'acl',
	SCHEMA: 'schema/acl',
	COURSE_INFO: 'course/acl',
	COLLECTION: 'collection/acl',
	WORKFLOW: 'workflow/acl',
	CONNECTOR: 'connector/acl',
	TAXONOMY: 'taxonomy/acl',
	REPORT: 'report/acl',
	OAUTH_CLIENT: 'oauth/acl'
};

export interface AclState extends PartialAclState {
}

function aclService(): AclService {
	const invoker: ObjectApiInvoker<TargetList> = {
		doCreateUpdate: async function(object: TargetList){
			const apiData = Object.assign({}, object);
			delete apiData.node;
			const res = await axios.put<TargetList>(`${Config.baseUrl}api/${nodeToEndpointMap[object.node]}`, apiData);
			return ({ result: res.data });
        },
        doDelete: async function(id: string){
            await axios.delete(`${Config.baseUrl}api/${nodeToEndpointMap[id]}`);
			return ({ id });
        },
        doRead: async function(id: string){
            const res = await axios.get<TargetList>(`${Config.baseUrl}api/${nodeToEndpointMap[id]}`);
			const targetList = res.data;
			targetList.node = id;
			return { result: targetList }; 
        },
        doValidate: function(object: TargetList){
            const validationErrors = {};
            return validationErrors;
        }
	};
	const actions = Object.assign({}, crudActions<TargetList>('TARGETLIST'), {
		listPrivileges: actionCreator.async<{node: string}, {node: string, result: string[]}, void>('LIST_PRIVILEGES_FOR_NODE')
	});
	const initialState: PartialAclState = {
        nodes: {}
    };
    return {
        actions,
        workers: Object.assign({}, workers(actions, invoker), {
			listPrivileges: wrapAsyncWorker(actions.listPrivileges, 
				async (param): Promise<{node: string, result: string[]}> => { 
					const { node } = param;
					const res = await axios.get<string[]>(`${Config.baseUrl}api/acl/privileges?node=${node}`);
					return ({ node, result: res.data }); 
				}
			  )
		}),
		reducer: reducerBuilder<TargetList, PartialAclState>(actions, initialState)
			.case(actions.listPrivileges.started, (state, data) => {
           		return state;
			})
			.case(actions.listPrivileges.done, (state, success) => {
				const nodes = state.nodes;
				return { ...state, nodes: { ...nodes, [success.result.node]: success.result.result }};
			})
    };
}

interface AclService extends ObjectService<TargetList, 
	{
		listPrivileges: AsyncActionCreators<{node: string}, {node: string, result: string[]}, void>;
	},
	{
		listPrivileges: (dispatch: Dispatch<any>, params: {node: string}) => Promise<{node: string, result: string[]}>;
	}> {
}

interface PartialAclState extends PartialObjectState<TargetList> {
	nodes: IDictionary<string[]>
}
/*
interface AclActions extends ObjectCrudActions<TargetList> {
    listPrivileges: AsyncActionCreators<{node: string}, {node: string, result: string[]}, void>;
} 

interface AclWorkers extends ObjectWorkers<TargetList> {
    listPrivileges: (dispatch: Dispatch<any>, params: {node: string}) => Promise<{node: string, result: string[]}>;
}*/