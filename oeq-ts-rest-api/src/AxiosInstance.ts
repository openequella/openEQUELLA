import Axios, { AxiosResponse, AxiosError } from 'axios';
import axiosCookieJarSupport from 'axios-cookiejar-support';
import * as tough from 'tough-cookie';
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
 * @param transformer A function which returns a copy of the raw data from the GET with any required values transformed - this should NOT mutate the input data (transforms should start on a copy/clone of the input)
 */
export const GET = <T>(
  path: string,
  validator: (data: unknown) => data is T,
  queryParams?: Parameters<typeof stringify>[0]
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

export const PUT = <T, R>(path: string, data?: T): Promise<R> =>
  axios
    .put(path, data)
    .then((response: AxiosResponse<R>) => response.data)
    .catch(catchHandler);

export default axios;
