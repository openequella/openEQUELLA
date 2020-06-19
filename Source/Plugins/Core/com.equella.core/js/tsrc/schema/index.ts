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
import { Schema } from "../api";
import { API_BASE_URL } from "../config";
import { EntityState, extendedEntityService } from "../entity/index";
import { actionCreator, wrapAsyncWorker } from "../util/actionutil";

interface SchemaExtActions {
  citations: AsyncActionCreators<{}, string[], void>;
}

interface SchemaExtWorkers {
  citations: (dispatch: Dispatch<any>, params: {}) => Promise<string[]>;
}

const actions: SchemaExtActions = {
  citations: actionCreator.async<{}, string[], void>("LOAD_CITATIONS"),
};

const workers: SchemaExtWorkers = {
  citations: wrapAsyncWorker(
    actions.citations,
    (params): Promise<string[]> => {
      return axios
        .get<string[]>(`${API_BASE_URL}/schema/citation`)
        .then((res) => res.data);
    }
  ),
};

const schemaService = extendedEntityService<
  Schema,
  SchemaExtActions,
  SchemaExtWorkers
>("SCHEMA", actions, workers);
schemaService.reducer
  .case(actions.citations.started, (state) => {
    return state;
  })
  .case(actions.citations.done, (state, success) => {
    return { ...state, citations: success.result };
  });

export default schemaService;

export interface SchemaState extends EntityState<Schema> {
  citations?: string[];
}
