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
export const TARGET_HOST = 'http://localhost:8080/';
export const API_PATH = `${TARGET_HOST}rest/api`;
export const API_PATH_VANILLA = `${TARGET_HOST}vanilla/api`;
export const API_PATH_FACET = `${TARGET_HOST}facet/api`;
export const API_PATH_FIVEO = `${TARGET_HOST}fiveo/api`;
export const USERNAME = process.env.USERNAME_AUTOTEST ?? 'AutoTest';
export const PASSWORD = process.env.PASSWORD_AUTOTEST ?? 'automated';
export const USERNAME_SUPER = process.env.USERNAME_SUPER ?? 'TLE_ADMINISTRATOR';
export const PASSWORD_SUPER = process.env.PASSWORD_SUPER ?? 'a123456';
