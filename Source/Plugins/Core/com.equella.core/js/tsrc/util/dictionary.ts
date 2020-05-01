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
export interface IDictionary<T> {
  [key: string]: T | undefined;
}

/**
 * Get a list of key/property names from an object
 *
 * @param obj object to find keys for
 *
 * TODO: replace this with Object.keys or Object.getOwnPropertyNames
 */
export function properties<T>(obj: IDictionary<T>): string[] {
  const props: string[] = [];
  for (const key in obj) {
    if (obj.hasOwnProperty(key)) {
      props.push(key);
    }
  }
  return props;
}
