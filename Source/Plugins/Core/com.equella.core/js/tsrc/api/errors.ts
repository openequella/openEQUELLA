import * as v4 from "uuid/v4";
import { AxiosResponse } from "axios";

export interface ErrorResponse {
  id: string;
  error: string;
  error_description?: string;
  code?: number;
}

export const generateNewErrorID = (
  error: string,
  code?: number,
  description?: string
): ErrorResponse => {
  return {
    id: v4(),
    error_description: description,
    code,
    error
  };
};

export const generateFromError = (error: Error): ErrorResponse => {
  return {
    id: v4(),
    error: error.name,
    error_description: error.message
  };
};

export function fromAxiosResponse(
  response: AxiosResponse<ErrorResponse>
): ErrorResponse {
  if (typeof response.data == "object") {
    return { ...response.data, id: v4() };
  } else {
    const [error, error_description] = (function() {
      switch (response.status) {
        case 404:
          return ["Not Found", ""];
        default:
          return [response.statusText, ""];
      }
    })();
    return {
      id: v4(),
      error,
      error_description,
      code: response.status
    };
  }
}
