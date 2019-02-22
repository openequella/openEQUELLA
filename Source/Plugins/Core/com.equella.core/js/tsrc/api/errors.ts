import v4 = require("uuid/v4");
import { AxiosError } from "axios";

export interface ErrorResponse {
  id: string;
  error: string;
  description?: string;
}

export const generateNewErrorID = (
  error: string,
  code?: number,
  description?: string
): ErrorResponse => {
  return {
    id: v4(),
    description,
    error
  };
};

export const generateFromAxiosError = (error: AxiosError): ErrorResponse => {
  return {
    id: v4(),
    error: error.name,
    description: error.message
  };
};
