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
import { useContext, useEffect, useState } from "react";
import { Redirect } from "react-router-dom";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as TE from "fp-ts/TaskEither";
import LoadingCircle from "../components/LoadingCircle";
import { AppContext } from "./App";
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
   * Function to check if the user has permission to access the page.
   */
  hasPermission: () => Promise<boolean>;
  /**
   * The page component to be rendered.
   */
  Page: React.ComponentType<BaseOEQRouteComponentProps>;
}

const redirectTo = `/logon.do?.page=${window.location.href}`;

/**
 * Component that protects the access to a New UI page by performing permission checks. If the user has permission,
 * the page is rendered, otherwise the user is redirected to the specified path which is usually the logon page.
 * A loading circle is displayed while the permission check is in progress.
 */
const ProtectedPage = ({
  path,
  Page,
  hasPermission,
  newUIProps,
}: ProtectedPageProps) => {
  const { appErrorHandler } = useContext(AppContext);
  const [permitted, setPermitted] = useState<boolean>();

  useEffect(() => {
    pipe(
      TE.tryCatch(
        hasPermission,
        (e) => new Error(`Failed to check permission for ${path}: ${e}`),
      ),
      TE.match((e) => {
        appErrorHandler(e);
        setPermitted(false); // Redirect if failed to check.
      }, setPermitted),
    )();
  }, [hasPermission, appErrorHandler, path]);

  return pipe(
    permitted,
    O.fromNullable,
    O.map((p) =>
      p ? <Page key={path} {...newUIProps} /> : <Redirect to={redirectTo} />,
    ),
    O.getOrElse(() => <LoadingCircle />),
  );
};

export default ProtectedPage;
