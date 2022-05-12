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

import { Theme, ThemeProvider, StyledEngineProvider } from "@mui/material";
import createGenerateClassName from "@mui/styles/createGenerateClassName";
import StylesProvider from "@mui/styles/StylesProvider";
import { pipe } from "fp-ts/function";
import * as React from "react";
import { ReactNode, useCallback } from "react";
import { BrowserRouter } from "react-router-dom";
import { getRouterBaseName } from "../AppConfig";
import MessageInfo from "../components/MessageInfo";
import { getAdvancedSearchIdFromLocation } from "../modules/AdvancedSearchModule";
import { getOeqTheme } from "../modules/ThemeModule";
import { startHeartbeat } from "../util/heartbeat";
import { simpleMatch } from "../util/match";
import type { EntryPage } from "./index";

declare module "@mui/styles/defaultTheme" {
  // eslint-disable-next-line @typescript-eslint/no-empty-interface
  interface DefaultTheme extends Theme {}
}

const SettingsPage = React.lazy(() => import("../settings/SettingsPage"));
const SearchPage = React.lazy(() => import("../search/SearchPage"));
const IndexPage = React.lazy(() => import("./IndexPage"));

interface NewPageProps {
  /**
   * A New UI page such as SearchPage.tsx.
   */
  children: ReactNode;
  /**
   * The prefix added in MUI styles.
   */
  classPrefix: string;
  /**
   * The MUI theme configured in the setting page.
   */
  theme?: Theme;
  /**
   * A string representing the base URL required by React Router.
   */
  basename?: string;
  /**
   * Whether to refresh the page when navigating to different route.
   */
  forceRefresh?: boolean;
}

/**
 * A wrapper component which is typically used in the environment where Old UI and New UI pages
 * are mixed.
 */
const NewPage = ({
  children,
  classPrefix,
  forceRefresh = false,
  theme = getOeqTheme(),
  basename = getRouterBaseName(),
}: NewPageProps) => {
  const generateClassName = createGenerateClassName({
    productionPrefix: classPrefix,
  });

  return (
    <StylesProvider generateClassName={generateClassName}>
      <StyledEngineProvider injectFirst>
        <ThemeProvider theme={theme}>
          <BrowserRouter basename={basename} forceRefresh={forceRefresh}>
            {children}
          </BrowserRouter>
        </ThemeProvider>
      </StyledEngineProvider>
    </StylesProvider>
  );
};

interface AppProps {
  entryPage: EntryPage;
}

const nop = () => {};

export interface WithErrorHandlerProps {
  /**
   * Function to handle a variety of unknown errors thrown from any component of the APP.
   */
  appErrorHandler: (error: Error | string) => void;
}

/**
 * Provide an error handler for the whole APP.
 */
export const AppRenderErrorContext = React.createContext<WithErrorHandlerProps>(
  {
    appErrorHandler: nop,
  }
);

/**
 * HOC function to inject the 'appErrorHandler' to a component. Typically used with a class component.
 * For functional components, use Context to access the 'appErrorHandler'.
 *
 * @param Page A class component
 */
export const withErrorHandler =
  <T,>(
    Page: React.ComponentType<T & WithErrorHandlerProps>
  ): ((props: T) => JSX.Element) =>
  (props: T) =>
    (
      <AppRenderErrorContext.Consumer>
        {({ appErrorHandler }) => (
          <Page {...props} appErrorHandler={appErrorHandler} />
        )}
      </AppRenderErrorContext.Consumer>
    );

const App = ({ entryPage }: AppProps): JSX.Element => {
  console.debug("START: <App>");

  const [error, setError] = React.useState<Error | string | undefined>();
  const appErrorHandler = useCallback(
    (error: Error | string) => setError(error),
    []
  );

  const appContent = () =>
    pipe(
      entryPage,
      simpleMatch<JSX.Element>({
        mainDiv: () => {
          startHeartbeat();
          return (
            <StyledEngineProvider injectFirst>
              <ThemeProvider theme={getOeqTheme()}>
                <IndexPage />
              </ThemeProvider>
            </StyledEngineProvider>
          );
        },
        searchPage: () => (
          <NewPage classPrefix="oeq-nsp">
            <SearchPage
              updateTemplate={nop}
              advancedSearchId={getAdvancedSearchIdFromLocation(
                window.location
              )}
            />
          </NewPage>
        ),
        settingsPage: () => (
          // When SettingsPage is used in old UI, each route change should trigger a refresh
          // for the whole page because there are no React component matching routes.
          <NewPage classPrefix="oeq-nst" forceRefresh>
            <SettingsPage
              refreshUser={nop}
              updateTemplate={nop}
              isReloadNeeded={false}
            />
          </NewPage>
        ),
        _: (s: string | number) => {
          throw new TypeError(`Unknown entry page target: ${s}`);
        },
      })
    );

  return (
    <AppRenderErrorContext.Provider
      value={{ appErrorHandler: appErrorHandler }}
    >
      {error && (
        <MessageInfo
          open
          onClose={() => setError(undefined)}
          variant="error"
          title={typeof error === "string" ? error : error.message}
        />
      )}
      {appContent()}
    </AppRenderErrorContext.Provider>
  );
};

export default App;
