import * as React from "react";
import JQueryDiv from "./JQueryDiv";
import { makeStyles } from "@material-ui/styles";
import { PageContent } from "./LegacyContent";

const useStyles = makeStyles(t => ({
  withPadding: {
    padding: t.spacing.unit * 2
  }
}));

export function LegacyContentRenderer(props: PageContent) {
  const { html, fullscreenMode, script, afterHtml, noForm, menuMode } = props;
  const classes = useStyles();

  let { body, crumbs, upperbody } = html;
  let extraClass = (function() {
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
  let mainContent = (
    <div className={`content ${extraClass}`}>
      {crumbs && <JQueryDiv id="breadcrumbs" html={crumbs} />}
      {upperbody && <JQueryDiv html={upperbody} />}
      <JQueryDiv html={body} script={script} afterHtml={afterHtml} />
    </div>
  );

  return mainContent;
}
