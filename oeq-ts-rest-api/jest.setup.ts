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
import Axios from 'axios';
import { CookieJar } from 'tough-cookie';
import { wrapper as axiosCookieJarSupport } from 'axios-cookiejar-support';
import * as AI from './src/AxiosInstance';

// So that cookies work when used in non-browser (i.e. Node/Jest) type environments. And seeing
// the oEQ security is based on JSESSIONID cookies currently this is key.
const mockedAxios = axiosCookieJarSupport(
  Axios.create({ jar: new CookieJar() })
);
jest.spyOn(AI, 'axiosInstance').mockImplementation(() => mockedAxios);
