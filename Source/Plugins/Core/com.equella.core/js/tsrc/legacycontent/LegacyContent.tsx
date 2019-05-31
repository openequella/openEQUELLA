import * as React from "react";
import { ErrorResponse } from "tsrc/api/errors";
import Axios from "axios";
import JQueryDiv from "./JQueryDiv";
import { v4 } from "uuid";
import { makeStyles } from "@material-ui/styles";

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

interface PageContent {
  html: { [key: string]: string };
  script: string;
  title: string;
  contentId: string;
  fullscreenMode: string;
  menuMode: string;
  hideAppBar: boolean;
  preventUnload: boolean;
  noForm: boolean;
  afterHtml: () => void;
}
export interface LegacyContentProps {
  pathname: string;
  search: string;
  contentUpdated: (content: PageContent) => void;
  userUpdated: () => void;
  redirected: (redir: { href: string; external: boolean }) => void;
  onError: (cb: { error: ErrorResponse; fullScreen: boolean }) => void;
}

type SubmitResponse = ExternalRedirect | LegacyContent | ChangeRoute;

const useStyles = makeStyles(t => ({
  withPadding: {
    padding: t.spacing.unit * 2
  }
}));

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
  let [content, setContent] = React.useState<PageContent>();
  let [stateData, setStateData] = React.useState<StateData>({});

  const classes = useStyles();

  function submitCurrentForm(
    fullScreen: boolean,
    scrollTop: boolean,
    submitValues: StateData,
    callback?: (response: SubmitResponse) => void
  ) {
    submitRequest(props.pathname, submitValues)
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
            setStateData(content.state);
            setContent(pageContent);
            props.contentUpdated(pageContent);
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
      .catch(error =>
        props.onError({ error: error.response.data, fullScreen })
      );
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
      var vals = collectParams(
        document.getElementById("eqpageForm") as HTMLFormElement,
        command,
        [].slice.call(arguments, 1)
      );
      submitCurrentForm(true, false, vals);
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
        if (formUpdate.partial) {
          setStateData(state => ({ ...state, ...formUpdate.state }));
        } else setStateData(formUpdate.state);
      }
    };
  }, [props.pathname]);

  React.useEffect(
    () => () => {
      updateStylesheets([]).then(deleteElements);
    },
    []
  );

  React.useEffect(() => {
    let params = new URLSearchParams(props.search);
    let urlValues = {};
    params.forEach((val, key) => {
      let exVal = urlValues[key];
      if (exVal) exVal.push(val);
      else urlValues[key] = [val];
    });
    submitCurrentForm(true, true, urlValues);
  }, [props.pathname, props.search]);

  function writeForm(children: React.ReactNode) {
    return (
      <form name="eqForm" id="eqpageForm" onSubmit={e => e.preventDefault()}>
        <div style={{ display: "none" }} className="_hiddenstate">
          {Object.keys(stateData).map((k, i) => {
            return (
              <React.Fragment key={i}>
                {stateData[k].map((v, i) => (
                  <input key={i} type="hidden" name={k} value={v} />
                ))}
              </React.Fragment>
            );
          })}
        </div>
        {children}
      </form>
    );
  }

  function renderContent(content: PageContent) {
    let body = content.html["body"];
    let crumbs = content.html["crumbs"];
    let upperbody = content.html["upperbody"];
    let extraClass = (function() {
      switch (content.fullscreenMode) {
        case "YES":
        case "YES_WITH_TOOLBAR":
          return "";
        default:
          switch (content.menuMode) {
            case "HIDDEN":
              return "";
            default:
              return classes.withPadding;
          }
      }
    })();
    let mainContent = (
      <div className={`content ${extraClass}`}>
        {crumbs && <JQueryDiv id="breadcrumbs" html={crumbs} />}
        {upperbody && <JQueryDiv html={upperbody} />}
        <JQueryDiv
          html={body}
          script={content.script}
          afterHtml={content.afterHtml}
        />
      </div>
    );
    return content.noForm ? mainContent : writeForm(mainContent);
  }
  return content ? renderContent(content) : <div />;
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
    var current = insertPoint.previousElementSibling;
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
        var newCss = doc.createElement("link");
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
    var e = elements[key];
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
  var vals = {};
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
