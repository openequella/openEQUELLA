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
import type {AxiosInstance} from "axios";
import type {CookieJar} from "tough-cookie";
import * as OEQ from '../src';
import { axiosInstance } from "../src/AxiosInstance";
import * as TC from './TestConfig';

/**
 * Executes a logout request and then clears all cookies if the request succeeds.
 */
export const logout = () => {
  OEQ.Auth.logout(TC.API_PATH).then(() => {
    console.log('Clearing all cookies.');

    const mockedAxios = axiosInstance()
    const defaultConfig: AxiosInstance['defaults'] & { jar?: CookieJar } = mockedAxios.defaults;
    defaultConfig.jar?.removeAllCookiesSync();
  });
}
