import Axios from "axios";

export interface MenuItem {
  route?: string;
  href?: string;
  title: string;
  systemIcon?: string;
  iconUrl?: string;
  newWindow: boolean;
}

export interface UserData {
  id: string;
  guest: boolean;
  autoLoggedIn: boolean;
  prefsEditable: boolean;
  counts?: {
    tasks: number;
    notifications: number;
  };
  menuGroups: MenuItem[][];
}

export const guestUser: UserData = {
  id: "guest",
  guest: true,
  autoLoggedIn: false,
  prefsEditable: false,
  counts: {
    tasks: 0,
    notifications: 0,
  },
  menuGroups: [],
};

export function getCurrentUser(): Promise<UserData> {
  return Axios.get<UserData>("api/content/currentuser").then((r) => r.data);
}
