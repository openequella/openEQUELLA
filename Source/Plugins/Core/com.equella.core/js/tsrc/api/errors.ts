import { AxiosError, AxiosResponse } from "axios";
import { languageStrings } from "../util/langstrings";
import v4 = require("uuid/v4");

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

// For handling standard errors - permissions, 404s, etc.
export function fromAxiosError(error: AxiosError): ErrorResponse {
  const langStrings = languageStrings;
  if (error.response) {
    switch (error.response.status) {
      case 403:
        return generateNewErrorID(
          langStrings.newuisettings.errors.permissiontitle,
          error.response.status,
          langStrings.newuisettings.errors.permissiondescription
        );
      case 404:
        return generateNewErrorID(
          langStrings.settings.searching.searchPageSettings.notFoundError,
          error.response.status,
          langStrings.settings.searching.searchPageSettings.notFoundErrorDesc
        );
      default:
        return generateFromError(error);
    }
  } else {
    //non axios errors
    return generateFromError(error);
  }
}

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
