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
import ErrorPage from "./ErrorPage";
import ScreenOptions from "./ScreenOptions";
import { LegacyContent } from "../legacycontent/LegacyContent";

interface LegacyPageProps extends TemplateUpdateProps {
  location: Location;
  refreshUser: () => void;
  redirect: (location: LocationDescriptor) => void;
  setPreventNavigation: (prevent: boolean) => void;
}

export const LegacyPage = React.memo(function LegacyPage(
  props: LegacyPageProps
) {
  const { location, updateTemplate, setPreventNavigation } = props;
  const [fullPageError, setFullPageError] = React.useState<ErrorResponse>();
  React.useEffect(() => setFullPageError(undefined), [location]);
  React.useEffect(() => {
    updateTemplate(tp => ({
      ...tp,
      backRoute: undefined,
      titleExtra: undefined,
      tabs: undefined,
      fixedViewPort: undefined,
      footer: undefined,
      disableNotifications: undefined
    }));
    setPreventNavigation(false);
  }, []);

  function processError(err: { error: ErrorResponse; fullScreen: boolean }) {
    const { error, fullScreen } = err;
    if (fullScreen) {
      setFullPageError(error);
      updateTemplate(
        tp =>
          ({ ...tp, fullscreenMode: "NO", title: error.error } as TemplateProps)
      );
    } else {
      updateTemplate(templateError(error));
    }
  }

  return (
    <>
      {fullPageError && <ErrorPage error={fullPageError} />}
      <LegacyContent
        pathname={location.pathname}
        search={location.search}
        contentUpdated={content => {
          let soHtml = content.html["so"];
          let menuExtra = soHtml ? (
            <ScreenOptions optionsHtml={soHtml} contentId={content.contentId} />
          ) : (
            undefined
          );
          setPreventNavigation(content.preventUnload);
          updateTemplate(tp => ({
            ...tp,
            title: content.title,
            hideAppBar: content.hideAppBar,
            fullscreenMode: content.fullscreenMode as FullscreenMode,
            menuMode: content.menuMode as MenuMode,
            menuExtra
          }));
        }}
        userUpdated={props.refreshUser}
        redirected={redir => {
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
            props.setPreventNavigation(false);
            props.redirect(redirloc);
          }
        }}
        onError={processError}
      />
    </>
  );
});
