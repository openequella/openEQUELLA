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
import { Alert } from "@mui/material";
import type { SxProps, Theme } from "@mui/material/styles";
import { constFalse, constVoid, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as React from "react";
import { useCallback, useEffect, useRef, useState } from "react";
import { useHistory } from "react-router";
import { v4 } from "uuid";
import type { PageContent } from "../../legacycontent/LegacyContent";
import { LegacyContentRenderer } from "../../legacycontent/LegacyContentRenderer";
import { getEqPageForm, legacyFormId } from "../../legacycontent/LegacyForm";
import { OLD_DASHBOARD_PATH } from "../../mainui/routes";
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
} from "../../modules/LegacyContentModule";
import { DraggablePortlet } from "../components/DraggablePortlet";
import {
  getPortletLegacyContent,
  updateExtraFiles,
} from "./LegacyPortletHelper";
import type { PortletBasicProps } from "./PortletHelper";

/**
 * Type definitions for four most commonly used legacy JS functions invoked by legacy scripts. Other
 * functions such as `eventnv` may also need to be defined here as we continue working on individual
 * portlets. For details about how these functions are used, please refer to `ajaxhelper.js`.
 *
 * Note: Based on their implementations in {@link LegacyContent}, none of these functions need to
 * return a `Promise`, even though some perform asynchronous operations.
 */
export interface LegacyJsFunctions {
  /**
   * Function typically used to submit a legacy form to trigger a server event.
   *
   * @param cmd Name of the target event handler defined in a legacy Section.
   * @param args Arguments to be passed to the event handler.
   */
  event: (cmd: string, ...args: string[]) => boolean;
  /**
   * Function typically called from the legacy JS function `postAjaxJSON` to trigger
   * a server event and then perform some DOM updates.
   *
   * @param form A legacy form which provides certain parameters to be included in the request.
   * @param cmd Name of the target event handler defined in a legacy Section.
   * @param args Arguments to be passed to the event handler.
   * @param callback Callback function to be invoked after the request is completed.
   */
  postAjax: (
    form: HTMLFormElement,
    cmd: string,
    args: string[],
    callback: (response: SubmitResponse) => void,
  ) => boolean;
  /**
   * Function typically called from the legacy JS function `updateFormAttributes` to update
   * the state of a legacy form.
   *
   * @param formUpdate Details of the form update, including the new state and whether it's a partial update.
   */
  updateForm: (formUpdate: FormUpdate) => void;
  /**
   * Function typically called from the legacy JS function `updateIncludes` to load additional
   * JS and CSS files, and execute some inline scripts.
   *
   * @param includes JS/CSS files and scripts to be included/executed.
   * @param cb Callback function to be invoked after the operation is completed.
   */
  updateIncludes: (
    includes: { js: string[]; css?: string[]; script: string },
    cb: () => void,
  ) => void;
}

/** Class name of the legacy portlet. */
export const LEGACY_PORTLET_CLASS = "legacy-portlet";

/**
 * Extend the type definition for the global `window` object to include an object dynamically
 * created for providing the access to legacy JS functions defined for a specific portlet.
 */
declare global {
  interface Window {
    [key: `EQ-${string}`]: LegacyJsFunctions;
  }
}

interface LegacyPortletProps extends PortletBasicProps {
  /**
   * Optional styles to help render the legacy portlet content properly.
   * Notes:
   * 1. The styles must follow the MUI `SxProps` format.
   * 2. The styles are applied at `Card` level, so use nested CSS selectors to apply styles to child components.
   *
   * Example:
   * ```
   *   customStyles={{
   *     width: "100%", // At the Card level
   *     '& .MuiCardContent-root': {
   *       backgroundColor: 'red' // Applied to the CardContent.
   *     },
   *     '& .MuiCardHeader-root': {
   *         backgroundColor: 'blue' // Applied to the CardHeader.
   *     },
   *     "& p": {
   *       color: 'orange' // Applied to all the 'p' elements under the Card.
   *     },
   *    }}
   * ```
   */
  customStyles?: SxProps<Theme>;
}

/**
 * Component dedicate to rendering the legacy content of a portlet:
 *
 * - Using Legacy content API to retrieve the legacy content
 * - Preparing a unique legacy form for the portlet
 * - Preparing the implementation of legacy Javascript functions that are commonly used by legacy portlet functions.
 */
export const LegacyPortlet = ({
  cfg,
  customStyles,
  ...restProps
}: LegacyPortletProps) => {
  const portletId = cfg.commonDetails.uuid;

  const [formId] = useState(`${legacyFormId}-${portletId}`);

  const [content, setContent] = useState<PageContent>();

  const [isLoading, setIsLoading] = useState(true);

  const [error, setError] = useState<string>();

  // General error message reminding users that an error has occurred while using this portlet.
  const generalErrorMsg = `An error occurred while displaying portlet ${portletId}`;

  // Reference to this portlet to help confirm whether an error event is triggered from within this portlet.
  const portletRef = useRef<HTMLDivElement>(null);

  const history = useHistory();

  // Generate a full `PageContent` that can be consumed by `LegacyContentRenderer` for a portlet.
  // Also update any JS and CSS files required by the portlet.
  const generatePortletContent = useCallback(
    async (resp: LegacyContentResponse): Promise<PageContent> => {
      await updateExtraFiles(resp.js, resp.css);

      return {
        ...resp,
        contentId: v4(),
        formId,
        afterHtml: constVoid,
      };
    },
    [formId],
  );

  // A simplified version of submitting a Legacy content API request and handling the response where
  // the request is always sent to the Legacy endpoint `/home.do`.
  const submitLegacyContentRequest = useCallback(
    (payload: StateData, callback?: (response: SubmitResponse) => void) => {
      const handleResponse = async (resp: SubmitResponse) => {
        // Handle the short-circuit callback case first
        if (callback) {
          callback(resp);
          return;
        }

        switch (true) {
          case isPageContent(resp): {
            const content = await generatePortletContent(resp);
            setContent(content);
            break;
          }
          case isChangeRoute(resp):
            history.push(`/${resp.route}`);
            break;

          case isExternalRedirect(resp):
            window.location.href = resp.href;
            break;

          default:
            console.warn(
              `Unknown response structure for legacy portlet ${portletId}`,
              resp,
            );
        }
      };

      submitRequest(OLD_DASHBOARD_PATH, payload)
        .then(handleResponse)
        .catch((e) => {
          setError(generalErrorMsg);
          console.error(generalErrorMsg, payload, e);
        });
    },
    [history, portletId, generatePortletContent, generalErrorMsg],
  );

  // Function that handles `window[EQ-{portletId}].event(...)` calls from legacy scripts.
  const event = useCallback(
    (cmd: string, ...args: string[]): boolean =>
      pipe(
        getEqPageForm(formId),
        O.fromNullable,
        O.fold(
          () => {
            console.warn(`No form identified by ID ${formId}.`);
          },
          (form) => {
            const payload = collectParams(form, cmd, args);
            submitLegacyContentRequest(payload);
          },
        ),
        // False is returned to match existing conventions (see ajaxhelper.js etc.) - the why is lost in history
        constFalse,
      ),
    [formId, submitLegacyContentRequest],
  );

  // Function that handles `window[EQ-{portletId}].postAjax(...)` calls from legacy scripts.
  const postAjax = useCallback(
    (
      form: HTMLFormElement,
      cmd: string,
      args: string[],
      callback: (response: SubmitResponse) => void,
    ): boolean => {
      const payload = collectParams(form, cmd, args);
      submitLegacyContentRequest(payload, callback);
      return false;
    },
    [submitLegacyContentRequest],
  );

  // Function that handles `window[EQ-{portletId}].updateForm(...)` calls from legacy scripts.
  const updateForm = (formUpdate: FormUpdate): void =>
    setContent((prevContent) =>
      pipe(
        prevContent,
        O.fromNullable,
        O.map((c) => {
          const state = formUpdate.partial
            ? { ...c.state, ...formUpdate.state }
            : formUpdate.state;
          return { ...c, state };
        }),
        O.toUndefined,
      ),
    );

  // Function that handles `window[EQ-{portletId}].updateIncludes(...)` calls from legacy scripts.
  const updateIncludes = (
    includes: { js: string[]; css?: string[]; script: string },
    cb: () => void,
  ) => {
    updateExtraFiles(includes.js, includes.css).then((_) => {
      // eslint-disable-next-line no-eval
      window.eval(includes.script);
      cb();
    });
  };

  useEffect(() => {
    getPortletLegacyContent(portletId)
      .then(generatePortletContent)
      .then(setContent)
      .catch((e) => {
        setError(generalErrorMsg);
        console.error(`${generalErrorMsg}: ${e}`);
      })
      .finally(() => setIsLoading(false));
  }, [portletId, generatePortletContent, generalErrorMsg]);

  useEffect(() => {
    window[`EQ-${portletId}`] = {
      event,
      postAjax,
      updateForm,
      updateIncludes,
    };
  }, [event, postAjax, portletId]);

  useEffect(() => {
    // Register a global error handler to capture any errors thrown by legacy scripts.
    const errorHandler = pipe(
      portletRef.current,
      O.fromNullable,
      O.map((ref) => (event: ErrorEvent) => {
        if (ref.contains(document.activeElement)) {
          setError(generalErrorMsg);
          console.error(generalErrorMsg, event.message);
        }
      }),
      O.getOrElse(() => (_: ErrorEvent) => {}),
    );

    window.addEventListener("error", errorHandler);

    return () => {
      window.removeEventListener("error", errorHandler);
    };
  }, [portletId, generalErrorMsg]);

  return (
    <div ref={portletRef} className={LEGACY_PORTLET_CLASS}>
      <DraggablePortlet
        portlet={cfg}
        isLoading={isLoading}
        sx={customStyles}
        {...restProps}
      >
        {error && (
          <Alert severity="error" onClose={() => setError(undefined)}>
            {error}
          </Alert>
        )}
        {content && <LegacyContentRenderer {...content} />}
      </DraggablePortlet>
    </div>
  );
};
