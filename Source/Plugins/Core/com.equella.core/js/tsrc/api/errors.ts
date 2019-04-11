import v4 = require("uuid/v4");

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

export const generateFromError = (error: Error): ErrorResponse => {
  return {
    id: v4(),
    error: error.name,
    description: error.message
  };
};
