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
import { AxiosResponse, AxiosError } from 'axios';
import { repackageError } from './Errors';

import { axiosInstance, PUT } from './AxiosInstance';

/**
 * A simple login method which results in the establishment of a session with the oEQ server and
 * managed via a JSESSIONID cookie.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param username the username of a supported oEQ authentication scheme
 * @param password the password of the specified user
 * @returns the resulting JSESSIONID from the cookie on successful authentication
 */
export const login = (
  apiBasePath: string,
  username: string,
  password: string
): Promise<string | undefined> =>
  axiosInstance()
    .post(apiBasePath + '/auth/login', null, {
      params: {
        username: username,
        password: password,
      },
    })
    .then((response: AxiosResponse) => {
      const cookies = response.headers['set-cookie'] as Array<string>;
      const sessionIdCookie = cookies?.find((c) => c.startsWith('JSESSIONID'));
      const sessionId = sessionIdCookie?.substring(
        sessionIdCookie.indexOf('=') + 1,
        sessionIdCookie.indexOf(';')
      );

      return sessionId;
    })
    .catch((error: AxiosError | Error) => {
      throw repackageError(error);
    });

/**
 * Executes a simple logout on the specified oEQ server, which results in the session (identified by
 * JSESSIONID cookie) be reset to a guest login.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param clearCookies Typically only for testing or running on NodeJS, but will clear the cookie JAR after logout
 */
export const logout = (apiBasePath: string): Promise<void> =>
  PUT(apiBasePath + '/auth/logout');
