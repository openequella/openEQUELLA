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
import { constFalse, constVoid, flow, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as React from "react";
import { useCallback, useEffect, useState } from "react";
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
import {
  getPortletLegacyContent,
  updateExtraFiles,
} from "./LegacyPortletHelper";

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

declare global {
  interface Window {
    [key: `EQ-${string}`]: LegacyJsFunctions;
  }
}

export interface LegacyPortletProps {
  /**
   * Id of the portlet that reuses its legacy content in the new Dashboard.
   */
  portletId: string;
}

/**
 * Component dedicate to rendering the legacy content of a portlet:
 *
 * - Using Legacy content API to retrieve the legacy content
 * - Preparing a unique legacy form for the portlet
 * - Preparing the implementation of legacy Javascript functions that are commonly used by legacy portlet functions.
 */
export const LegacyPortlet = ({ portletId }: LegacyPortletProps) => {
  const [formId] = useState(`${legacyFormId}-${portletId}`);

  const [content, setContent] = useState<PageContent>();

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
      submitRequest(OLD_DASHBOARD_PATH, payload)
        .then(async (resp) => {
          if (callback) {
            callback(resp);
            return;
          }

          if (isPageContent(resp)) {
            const content = await generatePortletContent(resp);
            setContent(content);
            return;
          }

          if (isChangeRoute(resp)) {
            history.push(`/${resp.route}`);
            return;
          }

          if (isExternalRedirect(resp)) {
            window.location.href = resp.href;
            return;
          }

          console.warn(
            `Unknown response structure for legacy portlet ${portletId}`,
            resp,
          );
        })
        .catch((e) => {
          // todo: Update error handling when working on OEQ-2685.
          console.error(
            `Request to /home.do with payload ${payload} failed: ${e}`,
          );
        });
    },
    [history, portletId, generatePortletContent],
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
    setContent(
      flow(
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
      // todo: Update error handling when working on OEQ-2685.
      .catch((e) =>
        console.error(
          `Failed to retrieve legacy content for portlet ${portletId}: ${e}`,
        ),
      );
  }, [portletId, generatePortletContent]);

  useEffect(() => {
    window[`EQ-${portletId}`] = {
      event,
      postAjax,
      updateForm,
      updateIncludes,
    };
  }, [event, postAjax, portletId]);

  return content && <LegacyContentRenderer {...content} />;
};
