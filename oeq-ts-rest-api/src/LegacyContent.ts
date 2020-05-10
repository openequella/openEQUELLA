import { AxiosError, AxiosResponse } from 'axios';
import { repackageError } from './Errors';

import axios from './AxiosInstance';

export interface ItemCounts {
  tasks: number;
  notifications: number;
}

export interface MenuItem {
  title: string;
  href?: string;
  systemIcon?: string;
  route?: string;
  iconUrl?: string;
  newWindow: boolean;
}

export interface CurrentUserDetails {
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  emailAddress: string;
  accessibilityMode: boolean;
  autoLoggedIn: boolean;
  guest: boolean;
  prefsEditable: boolean;
  menuGroups: Array<Array<MenuItem>>;
  counts?: ItemCounts;
}

/**
 * Retrieve details of the current user (based on JSESSIONID) including details for the UI such
 * as menu structure and task and notification counts.
 * 
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const getCurrentUserDetails = (
  apiBasePath: string
): Promise<CurrentUserDetails> => {
  return axios
    .get<CurrentUserDetails>(apiBasePath + '/content/currentuser')
    .then((response: AxiosResponse<CurrentUserDetails>) => response.data)
    .catch((error: AxiosError | Error) => {
      throw repackageError(error);
    });
};
