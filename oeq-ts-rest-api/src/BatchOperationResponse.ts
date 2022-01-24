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
/**
 * Responses for batch operations
 * such as batch create, update and delete
 */
export interface BatchOperationResponse {
  /** The ID of the item the response belongs to. */
  id: string;
  /** An HTTP status code indicating what the result was. */
  status: number;
  /** A human-readable message about the result. */
  message: string;
}

/**
 * Group responses that have a 4xx or 5xx status code, and return their messages
 */
export const groupErrorMessages = (
  responses: BatchOperationResponse[]
): string[] => {
  return responses
    .filter(({ status }) => status >= 400)
    .map(({ message }) => message);
};
