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
 * Encode an object as a query string
 *
 * @param params object to encode
 *
 * TODO: replace with https://github.com/ljharb/qs
 */
export function encodeQuery(params: {
  [key: string]: string | string[] | boolean | number | undefined;
}): string {
  let s = "";
  function addOne(key: string, element: string | number | boolean) {
    if (s.length > 0) s += "&";
    s += encodeURIComponent(key) + "=" + encodeURIComponent(element.toString());
  }
  for (const key in params) {
    if (params.hasOwnProperty(key)) {
      const paramValue = params[key];
      if (typeof paramValue != "undefined") {
        if (typeof paramValue == "object") {
          paramValue.forEach((element) => addOne(key, element));
        } else {
          addOne(key, paramValue);
        }
      }
    }
  }
  if (s.length > 0) {
    s = "?" + s;
  }
  return s;
}
