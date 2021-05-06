/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { AxiosError, AxiosResponse } from "axios";
import { languageStrings } from "../util/langstrings";
import { v4 } from "uuid";

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
    error,
  };
};

export const generateFromError = (error: Error): ErrorResponse => {
  return {
    id: v4(),
    error: error.name,
    error_description: error.message,
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
    const [error, error_description] = (function () {
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
      code: response.status,
    };
  }
}
