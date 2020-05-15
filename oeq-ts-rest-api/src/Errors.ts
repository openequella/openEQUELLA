import { AxiosError } from 'axios';
import { is } from 'typescript-is';

export interface ErrorResponse {
  code: number;
  error: string;
  error_description: string;
}

export class ApiError extends Error {
  constructor(message: string, public status?: number) {
    super(message);
    this.status = status;
  }

  errorResponse?: ErrorResponse;
}

export const repackageError = (error: AxiosError | Error): Error => {
  if ('isAxiosError' in error) {
    const apiError = new ApiError(error.message, error.response?.status);
    if (is<ErrorResponse>(error.response?.data)) {
      apiError.errorResponse = error.response?.data;
    }

    return apiError;
  } else {
    return error;
  }
};
