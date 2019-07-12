import * as React from "react";
import { ErrorResponse, fromAxiosResponse } from "../api/errors";
import Axios from "axios";
import { v4 } from "uuid";

declare global {
  interface Window {
    _trigger: any;
    eval: any;
  }
  const _trigger: any;
}

interface ExternalRedirect {
  href: string;
}

interface ChangeRoute {
  route: string;
  userUpdated: boolean;
}

interface StateData {
  [key: string]: string[];
}

type FormUpdate = {
  state: StateData;
  partial: boolean;
};

type LegacyContent = {
  html: { [key: string]: string };
  state: StateData;
  css: string[];
  js: string[];
  script: string;
  noForm: boolean;
  title: string;
  fullscreenMode: string;
  menuMode: string;
  hideAppBar: boolean;
  preventUnload: boolean;
  userUpdated: boolean;
};

export interface PageContent {
  contentId: string;
  html: { [key: string]: string };
  state: StateData;
  script: string;
  title: string;
  fullscreenMode: string;
  menuMode: string;
  hideAppBar: boolean;
  preventUnload: boolean;
  noForm: boolean;
  afterHtml: () => void;
}

export interface LegacyContentProps {
  enabled: boolean;
  pathname: string;
  search: string;
  userUpdated: () => void;
  redirected: (redir: { href: string; external: boolean }) => void;
  onError: (cb: { error: ErrorResponse; fullScreen: boolean }) => void;
  render(content: PageContent | undefined): React.ReactElement;
  children?: never;
}

type SubmitResponse = ExternalRedirect | LegacyContent | ChangeRoute;

function isPageContent(response: SubmitResponse): response is LegacyContent {
  return (response as LegacyContent).html !== undefined;
}

function isChangeRoute(response: SubmitResponse): response is ChangeRoute {
  return (response as ChangeRoute).route !== undefined;
}

export function submitRequest(
  path: string,
  vals: StateData
): Promise<SubmitResponse> {
  return Axios.post<SubmitResponse>(
    "api/content/submit" + encodeURI(path),
    vals
  ).then(res => res.data);
}

export const LegacyContent = React.memo(function LegacyContent(
  props: LegacyContentProps
) {
  const [content, setContent] = React.useState<PageContent>();
  const { enabled } = props;
  const baseUrl = (document.getElementsByTagName("base")[0] as HTMLBaseElement)
    .href;

  function toRelativeUrl(url: string) {
    let relUrl =
      url.indexOf(baseUrl) == 0 ? url.substring(baseUrl.length) : url;
    return relUrl.indexOf("/") == 0 ? relUrl : "/" + relUrl;
  }

  function submitCurrentForm(
    fullScreen: boolean,
    scrollTop: boolean,
    formAction: string | undefined,
    submitValues: StateData,
    callback?: (response: SubmitResponse) => void
  ) {
    submitRequest(toRelativeUrl(formAction || props.pathname), submitValues)
      .then(content => {
        if (callback) {
          callback(content);
        } else if (isPageContent(content)) {
          updateIncludes(content.css, content.js).then(extraCss => {
            const pageContent = {
              ...content,
              contentId: v4(),
              afterHtml: () => {
                deleteElements(extraCss);
                if (scrollTop) {
                  document.documentElement.scrollTop = 0;
                }
              }
            } as PageContent;
            if (content.userUpdated) {
              props.userUpdated();
            }
            setContent(pageContent);
          });
        } else if (isChangeRoute(content)) {
          if (content.userUpdated) {
            props.userUpdated();
          }
          props.redirected({ href: content.route, external: false });
        } else {
          props.redirected({ href: content.href, external: true });
        }
      })
      .catch(error => {
        props.onError({ error: fromAxiosResponse(error.response), fullScreen });
      });
  }

  function stdSubmit(validate: boolean) {
    return function(command: string) {
      if (window._trigger) {
        _trigger("presubmit");
        if (validate) {
          if (!_trigger("validate")) {
            return false;
          }
        }
      }
      const form = document.getElementById("eqpageForm") as HTMLFormElement;
      const vals = collectParams(form, command, [].slice.call(arguments, 1));
      submitCurrentForm(true, false, form.action, vals);
      return false;
    };
  }

  React.useEffect(() => {
    window["EQ"] = {
      event: stdSubmit(true),
      eventnv: stdSubmit(false),
      postAjax(
        form: HTMLFormElement,
        name: string,
        params: string[],
        callback: (response: SubmitResponse) => void,
        errorcallback: () => void
      ) {
        submitCurrentForm(
          false,
          false,
          form.action,
          collectParams(form, name, params),
          callback
        );
        return false;
      },
      updateIncludes(
        includes: { css: string[]; js: string[]; script: string },
        cb: () => void
      ) {
        updateIncludes(includes.css, includes.js).then(_ => {
          window.eval(includes.script);
          cb();
        });
      },
      updateForm: function(formUpdate: FormUpdate) {
        setContent(content => {
          if (content) {
            let newState = formUpdate.partial
              ? { ...content.state, ...formUpdate.state }
              : formUpdate.state;
            return { ...content, state: newState };
          } else return undefined;
        });
      }
    };
  }, [props.pathname]);

  React.useEffect(() => {
    if (enabled) {
      let params = new URLSearchParams(props.search);
      let urlValues = {};
      params.forEach((val, key) => {
        let exVal = urlValues[key];
        if (exVal) exVal.push(val);
        else urlValues[key] = [val];
      });
      submitCurrentForm(true, true, undefined, urlValues);
    }
    if (!enabled) {
      setContent(undefined);
      updateStylesheets([]).then(deleteElements);
    }
  }, [enabled, props.pathname, props.search]);

  return props.render(enabled ? content : undefined);
});

function resolveUrl(url: string) {
  return new URL(url, $("base").attr("href")).href;
}

async function updateIncludes(
  css: string[],
  js: string[]
): Promise<{ [url: string]: HTMLLinkElement }> {
  let extraCss = await updateStylesheets(css);
  await loadMissingScripts(js);
  return extraCss;
}

function updateStylesheets(
  _sheets: string[]
): Promise<{ [url: string]: HTMLLinkElement }> {
  return new Promise((resolve, reject) => {
    const sheets = _sheets.map(resolveUrl);
    const doc = window.document;
    const insertPoint = doc.getElementById("_dynamicInsert")!;
    const head = doc.getElementsByTagName("head")[0];
    let current = insertPoint.previousElementSibling;
    const existingSheets = {};
    while (current != null && current.tagName == "LINK") {
      existingSheets[(current as HTMLLinkElement).href] = current;
      current = current.previousElementSibling;
    }
    const lastSheet = sheets.reduce((lastLink, cssUrl) => {
      if (existingSheets[cssUrl]) {
        delete existingSheets[cssUrl];
        return lastLink;
      } else {
        const newCss = doc.createElement("link");
        newCss.rel = "stylesheet";
        newCss.href = cssUrl;
        head.insertBefore(newCss, insertPoint);
        return newCss;
      }
    }, null);
    if (!lastSheet) resolve(existingSheets);
    else {
      lastSheet.addEventListener("load", () => resolve(existingSheets), false);
      lastSheet.addEventListener(
        "error",
        err => {
          console.error(`Failed to load css: ${lastSheet.href}`);
          resolve();
        },
        false
      );
    }
  });
}

function deleteElements(elements: { [url: string]: HTMLElement }) {
  for (const key in elements) {
    const e = elements[key];
    e.parentElement!.removeChild(e);
  }
}

function loadMissingScripts(_scripts: string[]) {
  return new Promise((resolve, reject) => {
    const scripts = _scripts.map(resolveUrl);
    const doc = window.document;
    const head = doc.getElementsByTagName("head")[0];
    const scriptTags = doc.getElementsByTagName("script");
    const scriptSrcs = {};
    for (let i = 0; i < scriptTags.length; i++) {
      const scriptTag = scriptTags[i];
      if (scriptTag.src) {
        scriptSrcs[scriptTag.src] = true;
      }
    }
    const lastScript = scripts.reduce((lastScript, scriptUrl) => {
      if (scriptSrcs[scriptUrl]) {
        return lastScript;
      } else {
        let newScript = doc.createElement("script");
        newScript.src = scriptUrl;
        newScript.async = false;
        head.appendChild(newScript);
        return newScript;
      }
    }, null);
    if (!lastScript) resolve();
    else {
      lastScript.addEventListener("load", resolve, false);
      lastScript.addEventListener(
        "error",
        err => {
          console.error(`Failed to load script: ${lastScript.src}`);
          resolve();
        },
        false
      );
    }
  });
}

function collectParams(
  form: HTMLFormElement,
  command: string | null,
  args: string[]
) {
  const vals = {};
  if (command) {
    vals["event__"] = [command];
  }
  args.forEach((c, i) => {
    let outval = c;
    switch (typeof c) {
      case "object":
        if (c != null) {
          outval = JSON.stringify(c);
        }
    }
    vals["eventp__" + i] = [outval];
  });
  form.querySelectorAll("input,textarea").forEach((v: HTMLInputElement) => {
    if (v.type) {
      switch (v.type) {
        case "button":
          return;
        case "checkbox":
        case "radio":
          if (!v.checked || v.disabled) return;
      }
    }
    let ex = vals[v.name];
    if (ex) {
      ex.push(v.value);
    } else vals[v.name] = [v.value];
  });
  form.querySelectorAll("select").forEach((v: HTMLSelectElement) => {
    for (let i = 0; i < v.length; i++) {
      let o = v[i] as HTMLOptionElement;
      if (o.selected) {
        let ex = vals[v.name];
        if (ex) {
          ex.push(o.value);
        } else vals[v.name] = [o.value];
      }
    }
  });
  return vals;
}
