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
import { useContext } from "react";
import * as React from "react";
import {
  BrowserRouter,
  Prompt,
  Redirect,
  Route,
  RouteComponentProps,
  Switch,
} from "react-router-dom";
import ReactGA from "react-ga4";
import { shallowEqual } from "shallow-equal-object";
import { ErrorResponse } from "../api/errors";
import { getRenderData, getRouterBaseName, LEGACY_CSS_URL } from "../AppConfig";
import { LegacyContent } from "../legacycontent/LegacyContent";
import { isLegacyAdvancedSearchLocation } from "../modules/AdvancedSearchModule";
import { LegacyBrowseHierarchyLiteral } from "../modules/LegacyContentModule";
import { isSelectionSessionOpen } from "../modules/LegacySelectionSessionModule";
import {
  hasAuthenticated,
  isHierarchyPageACLGranted,
  isSearchPageACLGranted,
  isViewHierarchyTopicACLGranted,
  RequiredPermissionCheck,
} from "../modules/SecurityModule";
import { AppContext } from "./App";
import ErrorPage from "./ErrorPage";
import { defaultNavMessage, NavAwayDialog } from "./PreventNavigation";
import ProtectedPage from "./ProtectedPage";
import {
  BaseOEQRouteComponentProps,
  isNewUIRoute,
  NEW_DASHBOARD_PATH,
  OEQRouteNewUI,
  OLD_DASHBOARD_PATH,
  OLD_HIERARCHY_PATH,
  OLD_MY_RESOURCES_PATH,
  OLD_SEARCH_PATH,
  routes,
} from "./routes";
import { Template, TemplateProps, TemplateUpdate } from "./Template";
import { isEmpty } from "fp-ts/string";

const SearchPage = React.lazy(() => import("../search/SearchPage"));
const AdvancedSearchPage = React.lazy(
  () => import("../search/AdvancedSearchPage"),
);
const RootHierarchyPage = React.lazy(
  () => import("../hierarchy/RootHierarchyPage"),
);
const BrowseHierarchyPage = React.lazy(
  () => import("../hierarchy/BrowseHierarchyPage"),
);
const MyResourcesPage = React.lazy(
  () => import("../myresources/MyResourcesPage"),
);
const DashboardPage = React.lazy(() => import("../dashboard/DashboardPage"));

const renderData = getRenderData();

if (renderData?.analyticsId != null) {
  ReactGA.initialize(renderData.analyticsId);
}

const beforeunload = function (e: BeforeUnloadEvent) {
  e.returnValue = "Are you sure?";
  return "Are you sure?";
};

const removeLegacyCss = (): void => {
  const head = document.getElementsByTagName("head")[0];
  const legacyCss = window.document.querySelector(
    `link[href="${LEGACY_CSS_URL}"]`,
  );
  if (legacyCss) {
    head.removeChild(legacyCss);
  }
};

export default function IndexPage() {
  const { currentUser } = useContext(AppContext);
  const [fullPageError, setFullPageError] = React.useState<ErrorResponse>();
  const errorShowing = React.useRef(false);

  const [navAwayCallback, setNavAwayCallback] = React.useState<{
    message: string;
    cb: (confirm: boolean) => void;
  }>();

  const [preventNavMessage, setPreventNavMessage] = React.useState<string>();

  const [templateProps, setTemplateProps] = React.useState<TemplateProps>({
    title: "",
    fullscreenMode: "NO", // Match default on the server to avoid superfluous template updates
    children: [],
  });

  const setPreventNavigation = React.useCallback(
    (prevent: boolean) => {
      const message = prevent ? defaultNavMessage() : undefined;
      if (message) {
        window.addEventListener("beforeunload", beforeunload, false);
      } else {
        window.removeEventListener("beforeunload", beforeunload, false);
      }
      setPreventNavMessage(message);
    },
    [setPreventNavMessage],
  );

  const nonBlankNavMessage = preventNavMessage ? preventNavMessage : "";

  const updateTemplate = React.useCallback((edit: TemplateUpdate) => {
    setTemplateProps((tp) => {
      const edited = edit(tp);
      return shallowEqual(edited, tp) ? tp : edited;
    });
  }, []);

  const mkRouteProps = React.useCallback(
    (p: RouteComponentProps): BaseOEQRouteComponentProps => ({
      ...p,
      updateTemplate,
      redirect: p.history.push,
      setPreventNavigation,
      isReloadNeeded: !renderData?.newUI, // Indicate that new UI is displayed but not enabled.
    }),
    [setPreventNavigation, updateTemplate],
  );

  const errorCallback = React.useCallback((err: ErrorResponse) => {
    errorShowing.current = true;
    setFullPageError(err);
  }, []);

  const isAuthenticated = React.useMemo(() => {
    const isNotGuest = currentUser !== undefined && currentUser.id !== "guest";

    return isNotGuest || hasAuthenticated;
  }, [currentUser]);

  const renderProtectedPage = React.useCallback(
    (
      routeProps: RouteComponentProps,
      component: React.ComponentType<BaseOEQRouteComponentProps>,
      permissionChecks?: RequiredPermissionCheck[],
    ) => {
      return (
        <ProtectedPage
          permissionChecks={permissionChecks}
          path={routeProps.location.pathname}
          Page={component}
          newUIProps={mkRouteProps(routeProps)}
          isAuthenticated={isAuthenticated}
        />
      );
    },
    [isAuthenticated, mkRouteProps],
  );

  const renderLegacyContent = React.useCallback(
    (p: RouteComponentProps) => (
      <LegacyContent
        {...mkRouteProps(p)}
        search={p.location.search}
        pathname={p.location.pathname}
        locationKey={p.location.key}
        onError={errorCallback}
      />
    ),
    [mkRouteProps, errorCallback],
  );

  const newPageRoutes = React.useMemo(
    () =>
      Object.keys(routes)
        .map<OEQRouteNewUI | undefined>((name) => {
          // @ts-expect-error:  Element implicitly has an 'any' type because expression of type 'string' can't be used to index type 'Routes'
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          const maybeRoute: any = routes[name];
          return isNewUIRoute(maybeRoute) ? maybeRoute : undefined;
        })
        .filter((maybeRoute): maybeRoute is OEQRouteNewUI => !!maybeRoute)
        .map((oeqRoute: OEQRouteNewUI, ind) => (
          <Route
            key={ind}
            path={oeqRoute.path}
            render={(p) => {
              removeLegacyCss();
              return renderProtectedPage(
                p,
                oeqRoute.component,
                oeqRoute.permissionChecks,
              );
            }}
          />
        )),
    [renderProtectedPage],
  );

  const legacyPageRoutes = React.useMemo(
    () => (
      <Switch>
        <Route
          path={[OLD_SEARCH_PATH]}
          render={(routeProps) => {
            const newSearchEnabled: boolean =
              renderData !== undefined ? renderData.newSearch : false;
            const location = window.location;

            // If the path matches the Old Search UI path and new Search UI is disabled, use `LegacyContent`.
            // In other situations, use `SearchPage`.
            if (!newSearchEnabled) {
              return renderLegacyContent(routeProps);
            }
            removeLegacyCss();

            return renderProtectedPage(
              routeProps,
              isLegacyAdvancedSearchLocation(location)
                ? AdvancedSearchPage
                : SearchPage,
              [isSearchPageACLGranted],
            );
          }}
        />
        <Route
          path={OLD_MY_RESOURCES_PATH}
          render={(routeProps) =>
            isSelectionSessionOpen()
              ? renderLegacyContent(routeProps)
              : renderProtectedPage(routeProps, MyResourcesPage)
          }
        />
        <Route
          path={OLD_HIERARCHY_PATH}
          render={(routeProps) => {
            if (isSelectionSessionOpen()) {
              return renderLegacyContent(routeProps);
            }

            const searchParams = new URLSearchParams(
              routeProps.location.search,
            );
            const topic = searchParams.get("topic");

            // When the legacy path doesn't have query param 'topic or when it has but the value
            // is 'ALL', render 'BrowseHierarchyPage'.
            const page =
              topic === null || LegacyBrowseHierarchyLiteral.is(topic)
                ? renderProtectedPage(routeProps, BrowseHierarchyPage, [
                    isHierarchyPageACLGranted,
                  ])
                : renderProtectedPage(routeProps, RootHierarchyPage, [
                    isHierarchyPageACLGranted,
                    isViewHierarchyTopicACLGranted,
                  ]);

            return page;
          }}
        />
        <Route
          path={OLD_DASHBOARD_PATH}
          exact
          render={(routeProps) => {
            if (
              !isEmpty(routeProps.location.search) ||
              isSelectionSessionOpen()
            )
              return renderLegacyContent(routeProps);
            else return renderProtectedPage(routeProps, DashboardPage);
          }}
        />
        <Route path={OLD_DASHBOARD_PATH} render={renderLegacyContent} />
        <Route render={renderLegacyContent} />
      </Switch>
    ),
    [renderLegacyContent, renderProtectedPage],
  );

  return (
    <BrowserRouter
      basename={getRouterBaseName()}
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
      <Template {...templateProps}>
        <React.Suspense fallback={<>loading</>}>
          <Switch>
            {fullPageError && (
              <Route>
                <ErrorPage
                  error={fullPageError}
                  updateTemplate={updateTemplate}
                />
              </Route>
            )}
            <Route path="/" exact>
              <Redirect to={NEW_DASHBOARD_PATH} />
            </Route>
            {newPageRoutes}
            {legacyPageRoutes}
          </Switch>
        </React.Suspense>
      </Template>
    </BrowserRouter>
  );
}
