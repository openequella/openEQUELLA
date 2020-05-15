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
import * as OEQ from "@openequella/rest-api-client";
import { Config } from "../config";

const API_BASE_URL = `${Config.baseUrl}api`;

/**
 * Provides a simple Map<string,string> summary of available schemas, where K is the UUID
 * and V is the schema's name.
 *
 * On failure, an OEQ.Errors.ApiError will be thrown.
 */
export const schemaListSummary = (): Promise<Map<string, string>> =>
  OEQ.Schema.listSchemas(API_BASE_URL, {
    // We believe very few people have more than 5 schemas, so this will do for now.
    // Plus there are some oddities currently in the oEQ paging as seen in
    // https://github.com/openequella/openEQUELLA/issues/1735
    length: 100,
  }).then((schemas: OEQ.Common.PagedResult<OEQ.Common.BaseEntity>) =>
    schemas.results.reduce(
      (prev: Map<string, string>, curr: OEQ.Common.BaseEntity) =>
        prev.set(curr.uuid, curr.name),
      new Map<string, string>()
    )
  );
