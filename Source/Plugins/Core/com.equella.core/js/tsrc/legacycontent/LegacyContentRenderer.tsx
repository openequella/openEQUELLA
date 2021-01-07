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
import { makeStyles } from "@material-ui/core";
import * as React from "react";
import HtmlParser from "react-html-parser";
import JQueryDiv from "./JQueryDiv";
import { PageContent } from "./LegacyContent";
import { getEqPageForm, LegacyForm } from "./LegacyForm";

const useStyles = makeStyles((t) => ({
  noPadding: {
    padding: 0,
  },
}));

export function LegacyContentRenderer({
  afterHtml,
  fullscreenMode,
  html: { body, crumbs, form, upperbody },
  menuMode,
  noForm,
  script,
  state,
}: PageContent) {
  const classes = useStyles();

  // Effect responsible for the execution of the legacy scripts etc which historically were
  // simply added at the end of the server-side HTML rendered. Some of the scripts would
  // fail with `form` being `null` as the React rendering needs to be done before we kick
  // these off.
  React.useEffect(() => {
    const isReady = (): boolean =>
      (noForm ? document.getElementById(mainContentDivId) : getEqPageForm()) !==
      null;

    const runPostRenderTasks = () => {
      if (!isReady()) {
        window.requestAnimationFrame(runPostRenderTasks);
      } else {
        // eslint-disable-next-line no-eval
        if (script) window.eval(script);
        if (afterHtml) afterHtml();
      }
    };

    if (script || afterHtml) {
      runPostRenderTasks();
    }
  }, [afterHtml, noForm, script]);

  const extraClass =
    !fullscreenMode && menuMode !== "HIDDEN" ? classes.noPadding : "";

  const mainContentDivId = "oeqMainLegacyContent";

  const mainContent = (
    <div id={mainContentDivId} className={`content ${extraClass}`}>
      {crumbs && <JQueryDiv id="breadcrumbs" html={crumbs} />}
      {upperbody && <JQueryDiv html={upperbody} />}
      <JQueryDiv html={body} />
    </div>
  );

  return noForm ? (
    mainContent
  ) : (
    <>
      <LegacyForm state={state}>{mainContent}</LegacyForm>
      {form && HtmlParser(form)}
    </>
  );
}
