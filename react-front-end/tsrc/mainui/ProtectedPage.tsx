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
import * as React from "react";
import { useEffect, useState } from "react";
import { Redirect } from "react-router-dom";
import { flow, pipe } from "fp-ts/function";
import * as A from "fp-ts/Array";
import * as O from "fp-ts/Option";
import * as S from "fp-ts/string";
import * as T from "fp-ts/Task";
import * as TE from "fp-ts/TaskEither";
import { sprintf } from "sprintf-js";
import { generateNewErrorID } from "../api/errors";
import LoadingCircle from "../components/LoadingCircle";
import type { RequiredPermissionCheck } from "../modules/SecurityModule";
import { languageStrings } from "../util/langstrings";
import ErrorPage from "./ErrorPage";
import { BaseOEQRouteComponentProps } from "./routes";

export interface ProtectedPageProps {
  /**
   * The path which the page is rendered for.
   */
  path: string;
  /**
   * Props which provides New UI specific functions.
   */
  newUIProps: BaseOEQRouteComponentProps;
  /**
   * A list of required permission checks to determine if the user has permissions to access the page.
   */
  permissionChecks?: RequiredPermissionCheck[];
  /**
   * The page component to be rendered.
   */
  Page: React.ComponentType<BaseOEQRouteComponentProps>;
  /**
   * Whether the current user is authenticated.
   */
  isAuthenticated: boolean;
}
const { accessdenied } = languageStrings.error;

const loginPage = `/logon.do?.page=${window.location.href}`;

/**
 * Provide protection for the access to a New UI page by either authentication or permission checks.
 *
 * * When only authentication is required:
 *   Displaying the page or redirecting to the Login page, depending on the authentication (`isAuthenticated`) status.
 *
 * * When permission check is required (i.e. `permissionCheck` is not undefined):
 *   If permitted, display the page, otherwise redirect to the Login page or display Error page depending
 *   on the authentication status. While the permission check is in progress, show a loading circle.
 *
 *  Note: To support access to a 'protected' page for unauthenticated/guest users, simply provide
 *  a 'permissionCheck' function which always returns `true`.
 */
const ProtectedPage = ({
  path,
  Page,
  permissionChecks,
  newUIProps,
  isAuthenticated,
}: ProtectedPageProps) => {
  const [permitted, setPermitted] = useState<boolean>();
  const [error, setError] = useState<string>();

  useEffect(() => {
    const applicative = TE.getApplicativeTaskValidation(
      T.ApplySeq,
      A.getSemigroup<string>(),
    ); // Collection all the errors in an array.

    const runChecks = (checks: TE.TaskEither<string, true>[]) =>
      pipe(
        checks,
        A.traverse(applicative)(TE.mapLeft(A.of)),
        TE.map(A.every((r) => r === true)), // Every check must succeed.
      );

    pipe(
      permissionChecks,
      O.fromNullable,
      O.map(runChecks),
      O.getOrElse(() => TE.right(true)), // No ACL check required so set the flag to true.
      TE.match(
        flow(A.intercalate(S.Monoid)(", "), setError, () =>
          setPermitted(false),
        ),
        setPermitted,
      ),
    )();
  }, [permissionChecks, path]);

  const protectedByAuthentication = () =>
    isAuthenticated ? (
      <Page key={path} {...newUIProps} />
    ) : (
      <Redirect to={loginPage} />
    );

  const protectedByPermission = () => {
    const redirectOrError = () =>
      isAuthenticated ? (
        <ErrorPage
          error={generateNewErrorID(
            accessdenied.title,
            403,
            sprintf(accessdenied.message, path, error),
          )}
          updateTemplate={newUIProps.updateTemplate}
        />
      ) : (
        <Redirect to={loginPage} />
      );

    return pipe(
      permitted,
      O.fromNullable,
      O.map((granted) =>
        granted ? <Page key={path} {...newUIProps} /> : redirectOrError(),
      ),
      O.getOrElse(() => <LoadingCircle />),
    );
  };

  return permissionChecks
    ? protectedByPermission()
    : protectedByAuthentication();
};

export default ProtectedPage;
