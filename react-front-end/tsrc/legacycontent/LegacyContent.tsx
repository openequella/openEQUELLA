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
import { Backdrop } from "@mui/material";
import { constFalse, pipe } from "fp-ts/function";
import { isEqual } from "lodash";
import * as O from "fp-ts/Option";
import * as React from "react";
import { useContext } from "react";
import { flushSync } from "react-dom";
import { v4 } from "uuid";
import {
  ErrorResponse,
  fromAxiosResponse,
  generateFromError,
} from "../api/errors";
import { getRelativeUrl } from "../AppConfig";
import LoadingCircle from "../components/LoadingCircle";
import { AppContext } from "../mainui/App";
import type { BaseOEQRouteComponentProps } from "../mainui/routes";
import {
  FullscreenMode,
  templateDefaults,
  templatePropsForLegacy,
} from "../mainui/Template";
import {
  collectParams,
  FormUpdate,
  isChangeRoute,
  isExternalRedirect,
  isPageContent,
  LegacyContentResponse,
  StateData,
  submitRequest,
  SubmitResponse,
  updateIncludes,
} from "../modules/LegacyContentModule";
import { deleteRawModeFromStorage } from "../search/SearchPageHelper";
import { LegacyContentRenderer } from "./LegacyContentRenderer";
import { getEqPageForm, legacyFormId } from "./LegacyForm";

declare global {
  interface Window {
    _trigger: undefined | ((value: string) => boolean);
    EQ: { [index: string]: unknown };
  }

  const _trigger: (value: string) => boolean;
}

export interface PageContent {
  contentId: string;
  html: { [key: string]: string };
  state: StateData;
  script: string;
  title: string;
  metaTags: string;
  fullscreenMode: string;
  menuMode: string;
  hideAppBar: boolean;
  preventUnload: boolean;
  noForm: boolean;
  afterHtml: () => void;
  formId: string;
}

export interface LegacyContentProps extends BaseOEQRouteComponentProps {
  pathname: string;
  search: string;
  locationKey?: string;
  /**
   * A callback that will display any errors as a full page error.
   * @param error The error to be displayed
   */
  onError: (error: ErrorResponse) => void;

  children?: never;
}

interface LegacyContentSubmission {
  /**
   * Indicate whether there is a request submitted to `LegacyContentApi` already but not completed yet.
   */
  submitting: boolean;
  /**
   * Where to send the form data.
   */
  action?: string;
  /**
   * Payload of the submission.
   */
  payload?: StateData;
}

export const LegacyContent = React.memo(function LegacyContent({
  locationKey,
  onError,
  pathname,
  search,
  redirect,
  setPreventNavigation,
  updateTemplate,
  isReloadNeeded,
}: LegacyContentProps) {
  const [content, setContent] = React.useState<PageContent>();
  // Flag indicating the Legacy page content is being updated. The value must be set to `true` before a Legacy API request
  // is submitted. Once the response is handled through either a callback provided externally (typically through the Legacy
  // server side) or the state of this component, this value must be reset to `false`.
  // If the response of a Legacy API request is in the format of `ChangeRoute`, a navigation will be performed to the
  // new route, and this usually will trigger another Legacy API request to update the page content. So in this case,
  // do NOT reset the value.
  // After a Legacy API request is submitted, if no response is received after 200ms, a Backdrop with a spinner will be displayed
  // to indicate the request is still in progress and stop further page actions.
  const [updatingContent, setUpdatingContent] = React.useState<boolean>(false);
  const [showSpinner, setShowSpinner] = React.useState<boolean>(false);

  const submittingForm = React.useRef<LegacyContentSubmission>({
    submitting: false,
  });
  const { appErrorHandler, refreshUser } = useContext(AppContext);

  React.useEffect(() => {
    const timer = setTimeout(() => {
      if (updatingContent) {
        setShowSpinner(true);
      }
    }, 200);

    return () => {
      setShowSpinner(false);
      clearTimeout(timer);
    };
  }, [updatingContent]);

  React.useEffect(() => {
    if (isReloadNeeded) {
      window.location.reload();
    }
  }, [isReloadNeeded]);

  const redirected = (href: string, external: boolean) => {
    if (external) {
      window.location.href = href;
    } else {
      const ind = href.indexOf("?");
      const redirloc =
        ind < 0
          ? { pathname: "/" + href, search: "" }
          : {
              pathname: "/" + href.substr(0, ind),
              search: href.substr(ind),
            };
      setPreventNavigation(false);
      redirect(redirloc);
    }
  };

  function updatePageContent(
    content: LegacyContentResponse,
    scrollTop: boolean,
  ) {
    updateIncludes(content.js, content.css).then((extraCss) => {
      const pageContent = {
        ...content,
        contentId: v4(),
        formId: legacyFormId,
        afterHtml: () => {
          for (const key in extraCss) {
            const e = extraCss[key];
            e.parentElement?.removeChild(e);
          }
          if (scrollTop) {
            document.documentElement.scrollTop = 0;
          }
        },
      } as PageContent;
      if (content.userUpdated) {
        refreshUser();
      }
      flushSync(() => setContent(pageContent));
      flushSync(() => setUpdatingContent(false));
    });
  }

  /**
   * Appropriately handle/display any error messages while submitting the current form - a la
   * `submitCurrentForm`.
   *
   * @param fullscreen Should the error be displayed fullscreen/page (`true`) or just as a toast
   *                   or however the `Template` currently does it (`false`).
   * @param error The error to be handled
   */
  function handleError(fullscreen: boolean, error: ErrorResponse): void {
    if (fullscreen) {
      onError(error);
    } else {
      const { error: errorTitle, error_description } = error;
      appErrorHandler(error_description ?? errorTitle);
    }
  }

  /**
   * temp.hn and temp.hb are two possible params contains in the request and response
   * and are used in old UI to hide navigation (menu bar) and banner.
   * In new UI, for normal legacy content we have a template props `fullscreenMode`
   * received from server to hide menu bar, but not the error page.
   * Thus, it is used to set `fullscreenMode` for error page.
   */
  const preUpdateFullscreenMode = (submitValues: StateData) => {
    pipe(
      O.fromNullable(submitValues["temp.hn"]),
      O.chain<string[], FullscreenMode>(([firstValue]) =>
        firstValue === "true" ? O.some("YES") : O.none,
      ),
      O.map((value) =>
        updateTemplate((tp) => ({
          ...tp,
          fullscreenMode: value,
        })),
      ),
    );
  };

  function submitCurrentForm(
    fullScreen: boolean,
    scrollTop: boolean,
    formAction: string | undefined,
    submitValues: StateData,
    callback?: (response: SubmitResponse) => void,
  ) {
    if (formAction) {
      const { submitting, action, payload } = submittingForm.current;
      if (
        submitting &&
        formAction === action &&
        isEqual(submitValues, payload)
      ) {
        console.error(`ignore redundant submission to ${formAction}`);
        return;
      }

      submittingForm.current = {
        submitting: true,
        action: formAction,
        payload: submitValues,
      };
    }

    preUpdateFullscreenMode(submitValues);

    // Before a request is submitted to update the legacy page content, show a backdrop with a spinner.
    // - If the response is resolved and an external callback is provided, the callback handles page content update, so remove the spinner.
    // - If the response is a `LegacyContentResponse`, the updated content is available in the response body, so remove the spinner.
    // - If the response is a `ChangeRoute`, which usually triggers another Legacy content API request, keep the spinner visible until the next request completes.
    // - In all other cases, remove the spinner.
    setUpdatingContent(true);
    submitRequest(getRelativeUrl(formAction || pathname), submitValues)
      .then(async (content) => {
        // Clear raw mode saved in local storage after a login request is resolved.
        if (pathname.indexOf("logon.do") > 0) {
          deleteRawModeFromStorage();
        }

        if (callback) {
          callback(content);
          // After the externally provided callback is executed, reset the updating flag.
          setUpdatingContent(false);
        } else if (isPageContent(content)) {
          updatePageContent(content, scrollTop);
        } else if (isChangeRoute(content)) {
          if (content.userUpdated) {
            await refreshUser();
          }
          redirected(content.route, false);
        } else if (isExternalRedirect(content)) {
          redirected(content.href, true);
        } else {
          setUpdatingContent(false);
        }
      })
      .catch((error) => {
        const errorResponse: ErrorResponse =
          error.response !== undefined
            ? fromAxiosResponse(error.response)
            : generateFromError(error);
        handleError(fullScreen, errorResponse);
      })
      .finally(() => {
        if (formAction) {
          submittingForm.current = { submitting: false };
        }
      });
  }

  const stdSubmit =
    (validate: boolean) =>
    (command: string, ...args: string[]) => {
      if (window._trigger) {
        _trigger("presubmit");
        if (validate) {
          if (!_trigger("validate")) {
            return false;
          }
        }
      }

      return pipe(
        getEqPageForm(),
        O.fromNullable,
        O.fold(
          () =>
            onError(
              generateFromError({
                name: "stdSubmit Failure",
                message:
                  "stdSubmit unable to proceed due to missing " + legacyFormId,
              }),
            ),
          (form) => {
            const vals = collectParams(form, command, args);
            submitCurrentForm(true, false, form.action, vals);
          },
        ),
        constFalse,
      );
    };

  React.useEffect(
    () => {
      window["EQ"] = {
        event: stdSubmit(true),
        eventnv: stdSubmit(false),
        postAjax(
          form: HTMLFormElement,
          name: string,
          params: string[],
          callback: (response: SubmitResponse) => void,
        ) {
          submitCurrentForm(
            false,
            false,
            form.action,
            collectParams(form, name, params),
            callback,
          );
          return false;
        },
        updateIncludes(
          includes: { js: string[]; css?: string[]; script: string },
          cb: () => void,
        ) {
          updateIncludes(includes.js, includes.css).then((_) => {
            // eslint-disable-next-line no-eval
            window.eval(includes.script);
            cb();
          });
        },
        updateForm: function (formUpdate: FormUpdate) {
          setContent((content) => {
            if (content) {
              const newState = formUpdate.partial
                ? { ...content.state, ...formUpdate.state }
                : formUpdate.state;
              return { ...content, state: newState };
            } else return undefined;
          });
        },
      };
      // The below is missing `submitCurrentForm` and `stdSubmit` (which depends on
      // `submitCurrentForm`) from the dependency list. However to add that in would require more
      // effort than justified - best to rework with a reducer. Long term we aim to not even have the
      // whole LegacyContent tree, so for now we leave this as is.
    }, // eslint-disable-next-line react-hooks/exhaustive-deps
    [pathname],
  );

  React.useEffect(
    () => {
      const params = new URLSearchParams(search);
      const urlValues: { [index: string]: string[] } = {};
      params.forEach((val, key) => {
        const exVal = urlValues[key];
        if (exVal) exVal.push(val);
        else urlValues[key] = [val];
      });
      submitCurrentForm(true, true, undefined, urlValues);
      // The below is missing `submitCurrentForm` from the dependency list. However to add that in
      // would require more effort than justified - best to rework with a reducer. Long term we aim
      // to not even have the whole LegacyContent tree, so for now we leave this as is.
    }, // eslint-disable-next-line react-hooks/exhaustive-deps
    [pathname, search, locationKey],
  );

  React.useEffect(
    () =>
      updateTemplate((tp) => ({
        ...tp,
        ...(content
          ? templatePropsForLegacy(content)
          : templateDefaults("Missing content!")),
      })),
    [content, updateTemplate],
  );

  return (
    <>
      {content && (
        <LegacyContentRenderer {...content} key={content.contentId} />
      )}
      {showSpinner && (
        <Backdrop
          invisible // invisible to avoid the page flicking
          sx={(theme) => ({ zIndex: theme.zIndex.drawer + 1 })}
          open
        >
          <LoadingCircle />
        </Backdrop>
      )}
    </>
  );
});
