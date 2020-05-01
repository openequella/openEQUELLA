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
import { Dispatch } from "redux";
import { AsyncActionCreators } from "typescript-fsa";
import actionCreatorFactory from "typescript-fsa";

export const actionCreator = actionCreatorFactory();

/**
 * Handle asynchronous actions in the redux store
 *
 * @param asyncAction redux action to map to
 * @param worker asynchronous callback of work to be done
 *
 * NOTE: originally from https://github.com/aikoven/typescript-fsa/issues/5#issuecomment-255347353
 */
export function wrapAsyncWorker<TParameters, TSuccess, TError>(
  asyncAction: AsyncActionCreators<TParameters, TSuccess, TError>,
  worker: (params: TParameters) => Promise<TSuccess>
) {
  return function wrappedWorker(
    dispatch: Dispatch<any>,
    params: TParameters
  ): Promise<TSuccess> {
    dispatch(asyncAction.started(params));
    return worker(params).then(
      (result) => {
        dispatch(asyncAction.done({ params, result }));
        return result;
      },
      (error: TError) => {
        dispatch(asyncAction.failed({ params, error }));
        throw error;
      }
    );
  };
}
