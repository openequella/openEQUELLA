import Axios, { AxiosResponse, AxiosError } from 'axios';
import axiosCookieJarSupport from 'axios-cookiejar-support';
import * as tough from 'tough-cookie';
import { repackageError } from './Errors';

// So that cookies work when used in non-browser (i.e. Node/Jest) type environments. And seeing
// the oEQ security is based on JSESSIONID cookies currently this is key.
const axios = axiosCookieJarSupport(Axios.create());
axios.defaults.jar = new tough.CookieJar();
axios.defaults.withCredentials = true;

const catchHandler = (error: AxiosError | Error): never => {
  throw repackageError(error);
};

export const GET = <T>(
  path: string,
  validator?: (data: unknown) => boolean
): Promise<T> =>
  axios
    .get<T>(path)
    .then((response: AxiosResponse<T>) => {
      if (validator && !validator(response.data)) {
        // If a validator is provided, but it fails to validate the provided data...
        throw new Error('Data format mismatch with data received from server.');
      }

      return response.data;
    })
    .catch(catchHandler);

export const PUT = <T, R>(path: string, data?: T): Promise<R> =>
  axios
    .put(path, data)
    .then((response: AxiosResponse<R>) => response.data)
    .catch(catchHandler);

export default axios;
