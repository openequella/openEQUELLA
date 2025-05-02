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
import * as E from "fp-ts/Either";
import * as O from "fp-ts/Option";

expect.extend({
  /**
   * An expect matcher for Jest to confirm the response is a E.Left - typically to confirm an
   * error result.
   *
   * @param received The E.Either to validate
   */
  toBeLeft: (received: E.Either<unknown, unknown>): jest.CustomMatcherResult =>
    E.isLeft(received)
      ? {
          pass: true,
          message: () => `expected ${received} not to be E.Left`,
        }
      : {
          pass: false,
          message: () => `expected ${received} to be E.Left`,
        },

  /**
   * An expect matcher for Jest to confirm the response is an E.Right - typically to confirm an
   * success result.
   *
   * @param received The E.Either to validate
   */
  toBeRight: (
    received: E.Either<unknown, unknown>,
  ): jest.CustomMatcherResult =>
    E.isRight(received)
      ? {
          pass: true,
          message: () => `expected ${received} not to be E.Right`,
        }
      : {
          pass: false,
          message: () => `expected E.Right but got E.Left(${received.left})`,
        },

  /**
   * An expect matcher for Jest to confirm the response is O.None.
   *
   * @param received The O.Option to validate
   */
  toBeNone: (received: O.Option<unknown>): jest.CustomMatcherResult =>
    O.isNone(received)
      ? {
          pass: true,
          message: () => `expected ${received} not to be O.None`,
        }
      : {
          pass: false,
          message: () => `expected O.None but got O.Some: ${received}`,
        },

  /**
   * An expect matcher for Jest to confirm the response is O.Some.
   *
   * @param received The O.Option to validate
   */
  toBeSome: (received: O.Option<unknown>): jest.CustomMatcherResult =>
    O.isSome(received)
      ? {
          pass: true,
          message: () => `expected ${received} not to be O.Some`,
        }
      : {
          pass: false,
          message: () => `expected O.Some but got O.None`,
        },
});

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace jest {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    interface Matchers<R> {
      toBeLeft(): jest.CustomMatcherResult;

      toBeRight(): jest.CustomMatcherResult;

      toBeNone(): jest.CustomMatcherResult;

      toBeSome(): jest.CustomMatcherResult;
    }
  }
}

/**
 * An expect matcher for Jest to confirm the response is an E.Right - typically to confirm a
 * success result. The value is also returned, albeit (unfortunately) unioned with an `undefined`.
 * It is considered safe to access the value via a bang - .e.g `const value = expectRight(either)!;`
 *
 * @param either The E.Either to validate
 * @return The value contained in the E.Right
 */
export const expectRight = <V>(either: E.Either<unknown, V>): V | undefined => {
  expect(either).toBeRight();
  return E.isRight(either) ? either.right : undefined;
};
