import * as React from "react";
import JQueryDiv from "./JQueryDiv";
import { makeStyles } from "@material-ui/core";
import { PageContent } from "./LegacyContent";

const useStyles = makeStyles(t => ({
  withPadding: {
    padding: t.spacing(2)
  }
}));

export function LegacyContentRenderer({
  afterHtml,
  fullscreenMode,
  html,
  menuMode,
  script
}: PageContent) {
  const classes = useStyles();

  const { body, crumbs, upperbody } = html;
  const extraClass = (function() {
    switch (fullscreenMode) {
      case "YES":
      case "YES_WITH_TOOLBAR":
        return "";
      default:
        switch (menuMode) {
          case "HIDDEN":
            return "";
          default:
            return classes.withPadding;
        }
    }
  })();
  const mainContent = (
    <div className={`content ${extraClass}`}>
      {crumbs && <JQueryDiv id="breadcrumbs" html={crumbs} />}
      {upperbody && <JQueryDiv html={upperbody} />}
      <JQueryDiv html={body} script={script} afterHtml={afterHtml} />
    </div>
  );

  return mainContent;
}
