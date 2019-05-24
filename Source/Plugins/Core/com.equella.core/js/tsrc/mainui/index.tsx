import "../util/polyfill";
import { Switch, Route, Prompt, RouteComponentProps } from "react-router";
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
import { LegacyPage } from "./LegacyPage";
import { initStrings } from "../util/langstrings";
import { oeqTheme } from "../theme";

declare const bridge: Bridge;

const baseFullPath = new URL(document.head.baseURI).pathname;
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
  const refreshUser = React.useRef(() => {});
  const innerRef = React.useCallback(
    api => (refreshUser.current = api.refreshUser),
    [refreshUser]
  );
  const [navAwayCallback, setNavAwayCallback] = React.useState<{
    message: string;
    cb: (confirm: boolean) => void;
  }>();
  const [preventNavMessage, setPreventNavMessage] = React.useState<string>();
  const [templateProps, setTemplateProps] = React.useState({
    title: "",
    fullscreenMode: "YES",
    children: [],
    innerRef
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
  function updateTemplate(edit: TemplateUpdate): void {
    setTemplateProps(tp => {
      const edited = edit(tp);
      return shallowEqual(edited, tp) ? tp : edited;
    });
  }
  const oeqRoutes: { [key: string]: OEQRoute } = routes;
  function mkRouteProps(
    p: RouteComponentProps<any>
  ): OEQRouteComponentProps<any> {
    return {
      ...p,
      updateTemplate,
      refreshUser: refreshUser.current,
      redirect: p.history.push,
      setPreventNavigation
    };
  }
  return (
    <BrowserRouter
      basename={basePath}
      getUserConfirmation={(message, cb) => {
        setNavAwayCallback({ message, cb });
      }}
    >
      <Prompt when={Boolean(preventNavMessage)} message={nonBlankNavMsg} />
      <NavAwayDialog
        open={Boolean(navAwayCallback)}
        message={nonBlankNavMsg}
        navigateConfirm={confirm => {
          if (navAwayCallback) navAwayCallback.cb(confirm);
          if (confirm) setPreventNavMessage(undefined);
          setNavAwayCallback(undefined);
        }}
      />
      <Template {...templateProps}>
        <Switch>
          {Object.keys(oeqRoutes).map((key, ind) => {
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
          })}
          <Route render={p => <LegacyPage {...mkRouteProps(p)} />} />
        </Switch>
      </Template>
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
      <BrowserRouter basename={basePath}>
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
