import { AxiosResponse, AxiosError } from 'axios';
import { repackageError } from './Errors';

import axios, { PUT } from './AxiosInstance';
import { CookieJar } from 'tough-cookie';

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
): Promise<string | undefined> => {
  return axios
    .post(apiBasePath + '/auth/login', null, {
      params: {
        username: username,
        password: password,
      },
    })
    .then((response: AxiosResponse) => {
      const cookies = response.headers['set-cookie'] as Array<string>;
      const sessionIdCookie = cookies?.find(c => c.startsWith('JSESSIONID'));
      const sessionId = sessionIdCookie?.substring(
        sessionIdCookie.indexOf('=') + 1,
        sessionIdCookie.indexOf(';')
      );

      return sessionId;
    })
    .catch((error: AxiosError | Error) => {
      throw repackageError(error);
    });
};

/**
 * Executes a simple logout on the specified oEQ server, which results in the session (identified by
 * JSESSIONID cookie) be reset to a guest login.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param clearCookies Typically only for testing or running on NodeJS, but will clear the cookie JAR after logout
 */
export const logout = (
  apiBasePath: string,
  clearCookies?: boolean
): Promise<void> =>
  PUT(apiBasePath + '/auth/logout').then(() => {
    if (clearCookies) {
      console.log('Clearing all cookies.');
      (axios.defaults.jar as CookieJar).removeAllCookiesSync();
    }
  });
