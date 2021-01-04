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
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { getRenderData } from "../AppConfig";
import HtmlParser from "react-html-parser";
import {
  BrowserRouter,
  Prompt,
  Redirect,
  Route,
  RouteComponentProps,
  Switch,
} from "react-router-dom";
import { shallowEqual } from "shallow-equal-object";
import { ErrorResponse } from "../api/errors";
import {
  LegacyContent,
  LegacyContentProps,
  PageContent,
} from "../legacycontent/LegacyContent";
import { LegacyForm } from "../legacycontent/LegacyForm";

import { getCurrentUserDetails } from "../modules/UserModule";
import { basePath } from "./App";
import ErrorPage from "./ErrorPage";
import { LegacyPage, templatePropsForLegacy } from "./LegacyPage";
import { defaultNavMessage, NavAwayDialog } from "./PreventNavigation";
import { OEQRoute, OEQRouteComponentProps, routes } from "./routes";
import { Template, TemplateProps, TemplateUpdate } from "./Template";

const SearchPage = React.lazy(() => import("../search/SearchPage"));

const renderData = getRenderData();

const beforeunload = function (e: BeforeUnloadEvent) {
  e.returnValue = "Are you sure?";
  return "Are you sure?";
};

export default function IndexPage() {
  const [
    currentUser,
    setCurrentUser,
  ] = React.useState<OEQ.LegacyContent.CurrentUserDetails>();
  const [fullPageError, setFullPageError] = React.useState<ErrorResponse>();
  const errorShowing = React.useRef(false);

  const refreshUser = React.useCallback(() => {
    getCurrentUserDetails().then(setCurrentUser);
  }, []);

  React.useEffect(() => refreshUser(), []);

  const [navAwayCallback, setNavAwayCallback] = React.useState<{
    message: string;
    cb: (confirm: boolean) => void;
  }>();

  const [preventNavMessage, setPreventNavMessage] = React.useState<string>();
  const [
    legacyContentProps,
    setLegacyContentProps,
  ] = React.useState<LegacyContentProps>({
    enabled: false,
    pathname: "",
    search: "",
    locationKey: "",
    userUpdated: refreshUser,
    redirected: () => {},
    onError: () => {},
    render: () => <div />,
  });

  const [templateProps, setTemplateProps] = React.useState<TemplateProps>({
    title: "",
    fullscreenMode: "YES",
    children: [],
  });

  const setPreventNavigation = React.useCallback(
    (prevent) => {
      const message = prevent ? defaultNavMessage() : undefined;
      if (message) {
        window.addEventListener("beforeunload", beforeunload, false);
      } else {
        window.removeEventListener("beforeunload", beforeunload, false);
      }
      setPreventNavMessage(message);
    },
    [setPreventNavMessage]
  );

  const nonBlankNavMessage = preventNavMessage ? preventNavMessage : "";

  const updateTemplate = React.useCallback((edit: TemplateUpdate) => {
    setTemplateProps((tp) => {
      const edited = edit(tp);
      return shallowEqual(edited, tp) ? tp : edited;
    });
  }, []);
  interface Routes {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    [key: string]: OEQRoute<any>;
  }
  const oeqRoutes: Routes = routes;

  function mkRouteProps<T>(
    p: RouteComponentProps<T>
  ): OEQRouteComponentProps<T> {
    return {
      ...p,
      updateTemplate,
      refreshUser,
      redirect: p.history.push,
      setPreventNavigation,
      isReloadNeeded: !renderData?.newUI, // Indicate that new UI is displayed but not enabled.
    };
  }

  const newUIRoutes = React.useMemo(() => {
    return Object.keys(oeqRoutes).map((key, ind) => {
      const oeqRoute = oeqRoutes[key];
      return (
        (oeqRoute.component || oeqRoute.render) && (
          <Route
            key={ind}
            exact={oeqRoute.exact}
            path={oeqRoute.path}
            render={(p) => {
              const oeqProps = mkRouteProps(p);
              if (oeqRoute.component) {
                return <oeqRoute.component {...oeqProps} />;
              }
              return oeqRoute.render?.(oeqProps);
            }}
          />
        )
      );
    });
  }, [refreshUser]);

  const errorCallback = React.useCallback((err) => {
    errorShowing.current = true;
    setTemplateProps((p) => ({ ...p, fullscreenMode: undefined }));
    setFullPageError(err);
  }, []);

  const routeSwitch = (content?: PageContent) => {
    const oldSearchPagePath = "/searching.do";
    const newSearchPagePath = "/page/search";

    /**
     * Determine whether the **new** search page or the **old/legacy** search page should be
     * displayed. This is based on the requested path (route) as well as the request params.
     * This is somewhat complicated due to the need to support shared searches from legacy UI, and
     * the need to support Advanced Searches in legacy UI which are accessed via `searching.do`.
     *
     * (In the future, when all is New UI, this will not be needed.)
     *
     * The truth table would look like:
     *
     * - path is `newSearchPagePath` : true
     * - path is **not** `newSearchPagePath` but New Search is enabled and there are no advanced
     *   search params : true
     * - path is **not** `newSearchPagePath` but New Search is enabled and there **are** advanced
     *   search params : false
     * - and anything else : false
     *
     * @param location the applicable `window.location` state
     * @return `true` for new, or `false` for old
     */
    const newOrOldSearch = (location: Location): boolean => {
      const currentParams = new URLSearchParams(location.search);
      const currentPath = location.pathname;

      const advancedSearchParamsPresent: boolean =
        currentParams.get("in")?.startsWith("P") ?? false;
      const advancedSearchRequested: boolean =
        currentPath.endsWith(oldSearchPagePath) && advancedSearchParamsPresent;
      // TODO: Before we release 2021.1 this can be removed, as the 'newSearch' toggle wil be removed
      const newSearchEnabled: boolean =
        typeof renderData !== "undefined" && renderData?.newSearch;

      return (
        currentPath.match(newSearchPagePath) !== null ||
        (newSearchEnabled && !advancedSearchRequested)
      );
    };

    const renderLegacyPage = (p: RouteComponentProps) => (
      <LegacyPage
        {...mkRouteProps(p)}
        errorCallback={errorCallback}
        legacyContent={{ content, setLegacyContentProps }}
      />
    );

    return (
      <Switch>
        {fullPageError && (
          <Route>
            <ErrorPage error={fullPageError} />
          </Route>
        )}
        <Route path="/" exact>
          <Redirect to="/home.do" />
        </Route>
        {newUIRoutes}
        <Route
          path={[newSearchPagePath, oldSearchPagePath]}
          render={(p) =>
            newOrOldSearch(window.location) ? (
              <SearchPage {...mkRouteProps(p)} />
            ) : (
              renderLegacyPage(p)
            )
          }
        />
        <Route render={renderLegacyPage} />
      </Switch>
    );
  };

  return (
    <BrowserRouter
      basename={basePath}
      getUserConfirmation={(message, cb) => {
        if (errorShowing.current) {
          errorShowing.current = false;
          setFullPageError(undefined);
          cb(true);
        } else {
          setNavAwayCallback({ message, cb });
        }
      }}
    >
      <Prompt
        when={Boolean(preventNavMessage) || errorShowing.current}
        message={nonBlankNavMessage}
      />
      <NavAwayDialog
        open={Boolean(navAwayCallback)}
        message={nonBlankNavMessage}
        navigateConfirm={(confirm) => {
          if (navAwayCallback) navAwayCallback.cb(confirm);
          if (confirm) setPreventNavMessage(undefined);
          setNavAwayCallback(undefined);
        }}
      />
      <LegacyContent
        {...legacyContentProps}
        render={(content) => {
          const tp = content
            ? templatePropsForLegacy(content)
            : {
                ...templateProps,
                fullscreenMode: legacyContentProps.enabled
                  ? templateProps.fullscreenMode
                  : undefined,
              };
          const withErr = fullPageError
            ? { ...tp, title: fullPageError.error, fullscreenMode: undefined }
            : tp;
          const template = (
            <Template {...withErr} currentUser={currentUser}>
              {routeSwitch(content)}
            </Template>
          );
          const render = () => {
            if (!content || content.noForm) {
              return template;
            } else {
              const { form } = content.html;
              return (
                <>
                  <LegacyForm state={content.state}>{template}</LegacyForm>
                  {form && HtmlParser(form)}
                </>
              );
            }
          };
          return render();
        }}
      />
    </BrowserRouter>
  );
}
