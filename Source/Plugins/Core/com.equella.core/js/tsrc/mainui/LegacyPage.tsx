import * as React from "react";
import {
  templateError,
  TemplateUpdateProps,
  TemplateProps,
  FullscreenMode,
  MenuMode
} from "./Template";
import { Location, LocationDescriptor } from "history";
import { ErrorResponse } from "../api/errors";
import ScreenOptions from "./ScreenOptions";
import {
  PageContent,
  LegacyContentProps
} from "../legacycontent/LegacyContent";
import { LegacyContentRenderer } from "../legacycontent/LegacyContentRenderer";
import { shallowEqual } from "shallow-equal-object";

interface LegacyPageProps extends TemplateUpdateProps {
  location: Location;
  refreshUser(): void;
  redirect(location: LocationDescriptor): void;
  setPreventNavigation(prevent: boolean): void;
  errorCallback(error?: ErrorResponse): void;
  legacyContent: {
    content?: PageContent;
    setLegacyContentProps: (
      cb: (p: LegacyContentProps) => LegacyContentProps
    ) => void;
  };
}

export function templatePropsForLegacy(content: PageContent): TemplateProps {
  let soHtml = content.html["so"];
  let menuExtra = soHtml ? (
    <ScreenOptions optionsHtml={soHtml} contentId={content.contentId} />
  ) : (
    undefined
  );
  return {
    title: content.title,
    hideAppBar: content.hideAppBar,
    fullscreenMode: content.fullscreenMode as FullscreenMode,
    menuMode: content.menuMode as MenuMode,
    menuExtra,
    children: undefined
  };
}

export const LegacyPage = React.memo(function LegacyPage(
  props: LegacyPageProps
) {
  const { legacyContent, errorCallback } = props;
  const { content } = legacyContent;
  const { location, updateTemplate, setPreventNavigation } = props;
  const shouldPreventNav = content ? content.preventUnload : false;

  React.useEffect(() => setPreventNavigation(shouldPreventNav), [
    shouldPreventNav
  ]);

  const redirected = React.useCallback(
    redir => {
      const { href, external } = redir;
      if (external) {
        window.location.href = href;
      } else {
        const ind = href.indexOf("?");
        const redirloc =
          ind < 0
            ? { pathname: "/" + href, search: "" }
            : {
                pathname: "/" + href.substr(0, ind),
                search: href.substr(ind)
              };
        setPreventNavigation(false);
        props.redirect(redirloc);
      }
    },
    [setPreventNavigation, props.redirect]
  );

  const onError = React.useCallback(
    (err: { error: ErrorResponse; fullScreen: boolean }) => {
      const { error, fullScreen } = err;
      if (fullScreen) {
        errorCallback(error);
      } else {
        updateTemplate(templateError(error));
      }
    },
    [updateTemplate, errorCallback]
  );

  React.useEffect(() => {
    legacyContent.setLegacyContentProps(p => ({
      ...p,
      enabled: true,
      pathname: location.pathname,
      search: location.search,
      redirected,
      onError
    }));
  }, [location, redirected, onError]);

  React.useEffect(
    () => () =>
      legacyContent.setLegacyContentProps(p => ({ ...p, enabled: false })),
    []
  );

  return content ? <LegacyContentRenderer {...content} /> : <div />;
});
