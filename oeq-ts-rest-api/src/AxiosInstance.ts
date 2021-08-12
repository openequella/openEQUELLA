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
import Axios, { AxiosResponse, AxiosError } from 'axios';
import axiosCookieJarSupport from 'axios-cookiejar-support';
import * as tough from 'tough-cookie';
import { is } from 'typescript-is';
import { repackageError } from './Errors';
import { stringify } from 'query-string';

// So that cookies work when used in non-browser (i.e. Node/Jest) type environments. And seeing
// the oEQ security is based on JSESSIONID cookies currently this is key.
const axios = axiosCookieJarSupport(Axios.create());
axios.defaults.jar = new tough.CookieJar();
axios.defaults.withCredentials = true;

const catchHandler = (error: AxiosError | Error): never => {
  throw repackageError(error);
};

/**
 * Executes a HTTP GET for a given path.
 *
 * @param path The URL path for the target GET
 * @param validator A function to perform runtime type checking against the result - typically with typescript-is
 * @param queryParams The query parameters to send with the GET request
 */
export const GET = <T>(
  path: string,
  validator: (data: unknown) => data is T,
  queryParams?: Parameters<typeof stringify>[0] // eslint-disable-line @typescript-eslint/ban-types
): Promise<T> =>
  axios
    .get(path, {
      params: queryParams,
      paramsSerializer: (params) => stringify(params),
    })
    .then(({ data }: AxiosResponse<unknown>) => {
      if (!validator(data)) {
        throw new TypeError(
          `Data format mismatch with data received from server, on request to: "${path}"`
        );
      }

      return data;
    })
    .catch(catchHandler);

/**
 * Executes a HTTP HEAD request for a given path. Return a promise of `true`
 * to indicate the requested resource is available.
 *
 * @param path The URL path for the target HEAD
 * @param queryParams The query parameters to send with the HEAD request
 */
export const HEAD = (
  path: string,
  queryParams?: Parameters<typeof stringify>[0] // eslint-disable-line @typescript-eslint/ban-types
): Promise<boolean> =>
  axios
    .head(path, {
      params: queryParams,
      paramsSerializer: (params) => stringify(params),
    })
    .then(() => true)
    .catch(catchHandler);

/**
 * Executes a HTTP PUT request for a given path.
 *
 * @param path The URL path for the target HEAD
 * @param data The data to be sent in POST request
 */
export const PUT = <T, R>(path: string, data?: T): Promise<R> =>
  axios
    .put(path, data)
    .then((response: AxiosResponse<R>) => response.data)
    .catch(catchHandler);

/**
 * Executes a HTTP POST for a given path.
 *
 * @param path The URL path for the target POST
 * @param validator A function to perform runtime type checking against the result - typically with typescript-is
 * @param data The data to be sent in POST request
 */
export const POST = <T, R>(
  path: string,
  validator: (data: unknown) => data is R,
  data?: T
): Promise<R> =>
  axios
    .post(path, data)
    .then(({ data }: AxiosResponse<unknown>) => {
      if (!validator(data)) {
        throw new TypeError(
          `Data format mismatch with data received from server, on request to: "${path}"`
        );
      }
      return data;
    })
    .catch(catchHandler);

type RESPONSE_EMPTY_BODY = undefined | null | '';
/**
 * Executes a HTTP POST for a given path where explicitly no response payload is expected. This is
 * useful for endpoints which simply return a 200 with an empty body to signal request was successful.
 *
 * If expecting a response payload, then use `POST` instead.
 *
 * @param path The URL path for the target POST
 * @param data The data to be sent in POST request
 */
export const POST_void = <T>(path: string, data?: T): Promise<void> =>
  axios
    .post(path, data)
    .then(({ data }: AxiosResponse<unknown>) => {
      if (!is<RESPONSE_EMPTY_BODY>(data)) {
        throw new TypeError(
          `Data format mismatch with data received from server (expected NO data), on request to: "${path}"`
        );
      }
      return;
    })
    .catch(catchHandler);

/**
 * Executes a HTTP DELETE for a given path.
 *
 * @param path The URL path for the target DELETE
 * @param queryParams  The query parameters to send with the DELETE request
 */
export const DELETE = <R>(
  path: string,
  queryParams?: Parameters<typeof stringify>[0]
): Promise<R> =>
  axios
    .delete(path, {
      params: queryParams,
      paramsSerializer: (params) => stringify(params),
    })
    .then(({ data }: AxiosResponse<R>) => data)
    .catch(catchHandler);

export default axios;
