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
 * Restrictions applied to Taxonomy term selection.
 */
export enum SelectionRestriction {
  TOP_LEVEL_ONLY = 'TOP_LEVEL_ONLY',
  LEAF_ONLY = 'LEAF_ONLY',
  UNRESTRICTED = 'UNRESTRICTED',
}

/**
 * Formats which ae used to search for a term.
 */
export enum TermStorageFormat {
  FULL_PATH = 'FULL_PATH',
  LEAF_ONLY = 'LEAF_ONLY',
}
