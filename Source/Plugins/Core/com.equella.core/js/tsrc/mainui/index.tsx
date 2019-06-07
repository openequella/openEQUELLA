import "../util/polyfill";
import {
  Switch,
  Route,
  Prompt,
  RouteComponentProps,
  Redirect
} from "react-router";
import * as React from "react";
import * as ReactDOM from "react-dom";
import { BrowserRouter } from "react-router-dom";
import { Template, TemplateProps, TemplateUpdate } from "./Template";
import { ThemeProvider } from "@material-ui/styles";
import { Provider } from "react-redux";
import store from "../store";
import { routes, OEQRoute, OEQRouteComponentProps } from "./routes";
import { shallowEqual } from "shallow-equal-object";
import { Bridge } from "../api/bridge";
import { startHeartbeat } from "../util/heartbeat";
import { NavAwayDialog, defaultNavMessage } from "./PreventNavigation";
import { LegacyPage, templatePropsForLegacy } from "./LegacyPage";
import { initStrings } from "../util/langstrings";
import { oeqTheme } from "../theme";
import {
  LegacyContent,
  LegacyContentProps,
  PageContent
} from "../legacycontent/LegacyContent";
import { getCurrentUser } from "../api/currentuser";
import { ErrorResponse } from "../api/errors";
import ErrorPage from "./ErrorPage";

declare const bridge: Bridge;

const baseFullPath = new URL(document.head.getElementsByTagName("base")[0].href)
  .pathname;
const basePath = baseFullPath.substr(0, baseFullPath.length - 1);

declare const renderData:
  | {
      baseResources: string;
      newUI: boolean;
    }
  | undefined;

const beforeunload = function(e: Event) {
  e.returnValue = ("Are you sure?" as unknown) as boolean;
  return "Are you sure?";
};

function IndexPage() {
  const [currentUser, setCurrentUser] = React.useState();
  const [fullPageError, setFullPageError] = React.useState<ErrorResponse>();
  const errorShowing = React.useRef(false);

  const refreshUser = React.useCallback(() => {
    getCurrentUser().then(setCurrentUser);
  }, []);

  React.useEffect(() => refreshUser(), []);

  const [navAwayCallback, setNavAwayCallback] = React.useState<{
    message: string;
    cb: (confirm: boolean) => void;
  }>();

  const [preventNavMessage, setPreventNavMessage] = React.useState<string>();
  const [legacyContentProps, setLegacyContentProps] = React.useState<
    LegacyContentProps
  >({
    enabled: false,
    pathname: "",
    search: "",
    userUpdated: refreshUser,
    redirected: () => {},
    onError: () => {},
    render: () => <div />
  });

  const [templateProps, setTemplateProps] = React.useState({
    title: "",
    fullscreenMode: "YES",
    children: []
  } as TemplateProps);

  const setPreventNavigation = React.useCallback(
    prevent => {
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

  const nonBlankNavMsg = preventNavMessage ? preventNavMessage : "";

  const updateTemplate = React.useCallback((edit: TemplateUpdate) => {
    setTemplateProps(tp => {
      const edited = edit(tp);
      return shallowEqual(edited, tp) ? tp : edited;
    });
  }, []);
  const oeqRoutes: { [key: string]: OEQRoute } = routes;

  function mkRouteProps(
    p: RouteComponentProps<any>
  ): OEQRouteComponentProps<any> {
    return {
      ...p,
      updateTemplate,
      refreshUser,
      redirect: p.history.push,
      setPreventNavigation
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
            render={p => {
              const oeqProps = mkRouteProps(p);
              if (oeqRoute.component) {
                return <oeqRoute.component {...oeqProps} />;
              }
              return oeqRoute.render!(oeqProps);
            }}
          />
        )
      );
    });
  }, [refreshUser]);

  const errorCallback = React.useCallback(err => {
    errorShowing.current = true;
    setTemplateProps(p => ({ ...p, fullscreenMode: undefined }));
    setFullPageError(err);
  }, []);

  function routeSwitch(content?: PageContent) {
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
          render={p => (
            <LegacyPage
              {...mkRouteProps(p)}
              errorCallback={errorCallback}
              legacyContent={{ content, setLegacyContentProps }}
            />
          )}
        />
      </Switch>
    );
  }

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
        message={nonBlankNavMsg}
      />
      <NavAwayDialog
        open={Boolean(navAwayCallback)}
        message={nonBlankNavMsg}
        navigateConfirm={confirm => {
          if (navAwayCallback) navAwayCallback.cb(confirm);
          if (confirm) setPreventNavMessage(undefined);
          setNavAwayCallback(undefined);
        }}
      />
      <LegacyContent
        {...legacyContentProps}
        render={content => {
          const tp = content
            ? templatePropsForLegacy(content)
            : {
                ...templateProps,
                fullscreenMode: legacyContentProps.enabled
                  ? templateProps.fullscreenMode
                  : undefined
              };
          const withErr = fullPageError
            ? { ...tp, title: fullPageError.error, fullscreenMode: undefined }
            : tp;
          return (
            <Template {...withErr} currentUser={currentUser}>
              {routeSwitch(content)}
            </Template>
          );
        }}
      />
    </BrowserRouter>
  );
}

export default function() {
  initStrings();
  if (typeof renderData !== "undefined") {
    startHeartbeat();
    ReactDOM.render(
      <Provider store={store}>
        <ThemeProvider theme={oeqTheme}>
          <IndexPage />
        </ThemeProvider>
      </Provider>,
      document.getElementById("mainDiv")
    );
  } else {
    ReactDOM.render(
      <BrowserRouter basename={basePath} forceRefresh>
        <ThemeProvider theme={oeqTheme}>
          <bridge.SettingsPage
            refreshUser={() => {}}
            updateTemplate={_ => {}}
          />
        </ThemeProvider>
      </BrowserRouter>,
      document.getElementById("settingsPage")
    );
  }
}
