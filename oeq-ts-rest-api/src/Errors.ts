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

interface LegacyErrorResponse {
  code: number;
  error: string;
  error_description: string;
}

interface ApiResponseMessage {
  message: string;
}

interface ApiErrorResponse {
  errors: ApiResponseMessage[];
}

export type ErrorResponse = LegacyErrorResponse | ApiErrorResponse;

export class ApiError extends Error {
  constructor(
    message: string,
    public status?: number
  ) {
    super(message);
    this.status = status;
  }

  errorResponse?: ErrorResponse;
}

const handleLegacyErrorResponse: (responseData: unknown) => O.Option<ApiError> =
  flow(
    O.fromPredicate(LegacyErrorResponseCodec.is),
    O.map(
      ({ error_description, code }) => new ApiError(error_description, code)
    )
  );

const handleAxiosError = (error: AxiosError): O.Option<ApiError> => {
  const responseData = error.response?.data;
  const apiErrorMessage = ({ errors }: ApiErrorResponse): string =>
    pipe(
      errors,
      A.map(({ message }) => message),
      A.intercalate(S.Monoid)('\n')
    );

  return pipe(
    responseData,
    O.fromPredicate(ApiErrorResponseCodec.is),
    O.map(apiErrorMessage),
    O.map((message) => new ApiError(message, error.status)),
    O.alt(() => handleLegacyErrorResponse(responseData))
  );
};

export const repackageError = (error: AxiosError | Error): Error =>
  pipe(
    error,
    O.fromPredicate(isAxiosError),
    O.chain(handleAxiosError),
    O.getOrElse(() => error)
  );
