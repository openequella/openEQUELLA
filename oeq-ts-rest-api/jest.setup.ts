import Axios, {AxiosInstance} from "axios";
import {CookieJar} from "tough-cookie";
import { wrapper as axiosCookieJarSupport } from 'axios-cookiejar-support';
import * as AI from "./src/AxiosInstance"
import * as Auth from './src/Auth';

// So that cookies work when used in non-browser (i.e. Node/Jest) type environments. And seeing
// the oEQ security is based on JSESSIONID cookies currently this is key.
const mockedAxios = axiosCookieJarSupport(Axios.create({ jar: new CookieJar() }));
jest.spyOn(AI, 'axiosInstance').mockImplementation(() => mockedAxios);

// Mock the logout function to:
// 1. Call the original implementation to perform the logout
// 2. Clear all cookies from the mocked axios instance
jest.mock('./src/Auth', () => {
  const actualImpl: typeof Auth = jest.requireActual('./src/Auth');

  return {
    ...actualImpl,
    logout: async (apiBasePath: string): Promise<void> =>
       actualImpl.logout(apiBasePath).then(() => {
          console.log('Clearing all cookies.');
          const defaultConfig: AxiosInstance['defaults'] & { jar?: CookieJar } = mockedAxios.defaults;
          defaultConfig.jar?.removeAllCookiesSync();
      })
  };
});
