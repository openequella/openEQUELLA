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

export function templatePropsForLegacy({
  title,
  metaTags,
  html,
  contentId,
  hideAppBar,
  fullscreenMode,
  menuMode
}: PageContent): TemplateProps {
  const soHtml = html["so"];
  const menuExtra = soHtml ? (
    <ScreenOptions optionsHtml={soHtml} contentId={contentId} key={contentId} />
  ) : (
    undefined
  );
  return {
    title,
    metaTags,
    hideAppBar,
    fullscreenMode: fullscreenMode as FullscreenMode,
    menuMode: menuMode as MenuMode,
    menuExtra,
    children: undefined
  };
}

export const LegacyPage = React.memo(
  ({
    legacyContent,
    errorCallback,
    location,
    updateTemplate,
    setPreventNavigation,
    redirect
  }: LegacyPageProps) => {
    const { content } = legacyContent;
    const shouldPreventNav = content ? content.preventUnload : false;

    React.useEffect(() => {
      if (content) {
        updateTemplate(tp => ({ ...tp, ...templatePropsForLegacy(content) }));
      }
    }, [content]);

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
          redirect(redirloc);
        }
      },
      [setPreventNavigation, redirect]
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
        locationKey: location.key,
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
  }
);
