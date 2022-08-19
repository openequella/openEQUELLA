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
import { DELETE } from './AxiosInstance';

const SCRAPBOOK_API_PATH = '/scrapbook';

/**
 * Send a DELETE request to the endpoint for Scrapbook to delete one Scrapbook Item.
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 * @param uuid UUID of the Scrapbook Item.
 */
export const deleteScrapbook = (
  apiBasePath: string,
  uuid: string
): Promise<void> => DELETE(`${apiBasePath}${SCRAPBOOK_API_PATH}/${uuid}`);
