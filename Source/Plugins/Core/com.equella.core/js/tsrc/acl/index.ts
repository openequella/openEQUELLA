/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import axios from "axios";
import { Dispatch } from "redux";
import { AsyncActionCreators } from "typescript-fsa";
import {
  ReducerBuilder,
  reducerWithInitialState,
} from "typescript-fsa-reducers";
import { Config } from "../config";
import { actionCreator, wrapAsyncWorker } from "../util/actionutil";
import { IDictionary } from "../util/dictionary";

const acl = aclService();
export default acl;

export interface AclState extends PartialAclState {}

function aclService() {
  const actions = aclActions();
  return {
    actions,
    workers: aclWorkers(actions),
    reducer: aclReducerBuilder(actions),
  };
}

interface AclActions {
  listPrivileges: AsyncActionCreators<
    { node: string },
    { node: string; result: string[] },
    void
  >;
}

interface AclWorkers {
  listPrivileges: (
    dispatch: Dispatch<any>,
    params: { node: string }
  ) => Promise<{ node: string; result: string[] }>;
}

interface PartialAclState {
  nodes: IDictionary<string[]>;
}

function aclActions(): AclActions {
  return {
    listPrivileges: actionCreator.async<
      { node: string },
      { node: string; result: string[] },
      void
    >("LIST_PRIVILEGES_FOR_NODE"),
  };
}

function aclWorkers(actions: AclActions): AclWorkers {
  return {
    listPrivileges: wrapAsyncWorker(
      actions.listPrivileges,
      (param): Promise<{ node: string; result: string[] }> => {
        const { node } = param;
        return axios
          .get<string[]>(`${Config.baseUrl}api/acl/privileges?node=${node}`)
          .then((res) => ({ node, result: res.data }));
      }
    ),
  };
}

function aclReducerBuilder(
  actions: AclActions
): ReducerBuilder<PartialAclState, PartialAclState> {
  const initialState: PartialAclState = {
    nodes: {},
  };

  return reducerWithInitialState(initialState)
    .case(actions.listPrivileges.started, (state, data) => {
      return state;
    })
    .case(actions.listPrivileges.done, (state, success) => {
      const nodes = state.nodes;
      return {
        ...state,
        nodes: { ...nodes, [success.result.node]: success.result.result },
      };
    });
}
