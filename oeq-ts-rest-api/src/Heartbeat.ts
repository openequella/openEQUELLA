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
import { is } from 'typescript-is';
import { GET } from './AxiosInstance';

type heartbeatResponse = 'OK';

const HEART_BEAT_API_PATH = '/status/heartbeat';

/**
 * Retrieve oEQ server status by sending a heartbeat request.
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const checkHeartbeat = (apiBasePath: string): Promise<string> =>
  GET<string>(
    apiBasePath + HEART_BEAT_API_PATH,
    (data): data is heartbeatResponse => is<heartbeatResponse>(data)
  );
