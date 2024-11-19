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
import { AxiosError, isAxiosError } from 'axios';
import { ApiErrorResponseCodec, LegacyErrorResponseCodec } from './gen/Errors';
import * as A from 'fp-ts/Array';
import * as O from 'fp-ts/Option';
import * as S from 'fp-ts/string';
import { pipe, flow } from 'fp-ts/function';

/**
 * Type definition for the API Response structure mostly used in oEQ Legacy code
 */
interface LegacyErrorResponse {
  code: number;
  error: string;
  error_description: string;
}

/**
 * Represent a single error message returned from server
 */
interface ApiResponseMessage {
  message: string;
}

/**
 * Type definition for the API Response structure used in REST endpoints implemented in Scala
 */
interface ApiErrorResponse {
  /**
   * List of error messages returned from server
   */
  errors: ApiResponseMessage[];
}

export class ApiError extends Error {
  constructor(
    message: string,
    public status?: number
  ) {
    super(message);
    this.status = status;
  }
}

export const isApiError = (error: unknown): error is ApiError =>
  error instanceof ApiError;

const handleLegacyErrorResponse: (responseData: unknown) => O.Option<ApiError> =
  flow(
    O.fromPredicate(LegacyErrorResponseCodec.is),
    O.map(
      ({ error_description, code }: LegacyErrorResponse) =>
        new ApiError(error_description, code)
    )
  );

const handleAxiosError = (error: AxiosError): ApiError => {
  const { message, response } = error;
  const { data, status } = response ?? { data: undefined, status: undefined };

  const handleApiErrorResponse = ({ errors }: ApiErrorResponse): ApiError =>
    pipe(
      errors,
      A.map(({ message }) => message),
      A.intercalate(S.Monoid)('\n'),
      (apiErrorMessages) => new ApiError(apiErrorMessages, status)
    );

  return pipe(
    data,
    O.fromPredicate(ApiErrorResponseCodec.is),
    O.map(handleApiErrorResponse),
    O.alt(() => handleLegacyErrorResponse(data)),
    O.getOrElse(() => new ApiError(message, status))
  );
};

export const repackageError = (error: AxiosError | Error): Error =>
  pipe(
    error,
    O.fromPredicate(isAxiosError),
    O.map(handleAxiosError),
    O.getOrElse(() => error)
  );
