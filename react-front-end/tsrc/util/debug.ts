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
 * Utility function for debugging fp-ts pipelines. Allow the value to be shown in the console log,
 * but then continue its way down the pipeline.
 *
 * Example:
 *
 * ```
 * pipe(
 *   x,
 *   doTransform,
 *   showValue("after doTransform"),
 *   f);
 * ```
 *
 * @param label The string to use to label/prefix the console log entry.
 */
export const showValue =
  <T>(label: string) =>
  (value: T) => {
    console.debug(label, value);
    return value;
  };
