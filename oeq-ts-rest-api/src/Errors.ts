import { AxiosError } from 'axios';

export class ApiError extends Error {
  constructor(message: string, public status?: number) {
    super(message);
    this.status = status;
  }
}

export const repackageError = (error: AxiosError | Error): Error => {
  if ('isAxiosError' in error) {
    return new ApiError(error.message, error.response?.status);
  } else {
    return error;
  }
};
