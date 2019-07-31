export interface User {
  id?: string;
  username?: string;
  firstName?: string;
  lastName?: string;
  emailAddress?: string;
  exportDetails?: UserExport;
}

export interface UserExport {
  exportVersion?: string;
  passwordHash?: string;
}
