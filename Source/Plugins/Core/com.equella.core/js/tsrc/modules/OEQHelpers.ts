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

/**
 * Provides a simple summary of a list of BaseEntities, where K will be the UUID and V will
 * be the name. This kind of summary is useful for displaying in UI controls where a user
 * may need to select one.
 *
 * @param list A list most likely retrieved via a REST call in the REST Client library
 */
export const summarisePagedBaseEntities = (
  list: OEQ.Common.PagedResult<OEQ.Common.BaseEntity>
) =>
  list.results.reduce(
    (prev: Map<string, string>, curr: OEQ.Common.BaseEntity) =>
      prev.set(curr.uuid, curr.name),
    new Map<string, string>()
  );
