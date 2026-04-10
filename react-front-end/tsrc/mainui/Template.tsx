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
import AccountIcon from "@mui/icons-material/AccountCircle";
import BackIcon from "@mui/icons-material/ArrowBack";
import AssignmentIcon from "@mui/icons-material/Assignment";
import HelpIcon from "@mui/icons-material/Help";
import MenuIcon from "@mui/icons-material/Menu";
import NotificationsIcon from "@mui/icons-material/Notifications";
import {
  AppBar,
  Badge,
  CssBaseline,
  Drawer,
  GlobalStyles,
  IconButton,
  Menu,
  MenuItem,
  Toolbar,
  Tooltip,
  Typography,
  useMediaQuery,
} from "@mui/material";
import type { Theme } from "@mui/material/styles";
import { styled } from "@mui/material/styles";
import clsx, { ClassValue } from "clsx";
import { LocationDescriptor } from "history";
import { isEqual } from "lodash";
import * as React from "react";
import { useContext } from "react";
import { Link } from "react-router-dom";
import { ErrorResponse } from "../api/errors";
import { TooltipIconButton } from "../components/TooltipIconButton";
import { PageContent } from "../legacycontent/LegacyContent";
import {
  isItemViewedFromIntegration,
  isSelectionSessionOpen,
} from "../modules/LegacySelectionSessionModule";
import { getOeqTheme } from "../modules/ThemeModule";
import { guestUser } from "../modules/UserModule";
import { languageStrings } from "../util/langstrings";
import { AppContext } from "./App";
import MainMenu from "./MainMenu";
import { legacyPageUrl, routes } from "./routes";
import ScreenOptions from "./ScreenOptions";

export type MenuMode = "HIDDEN" | "COLLAPSED" | "FULL";
export type FullscreenMode = "YES" | "YES_WITH_TOOLBAR" | "NO";
/* Vertical offset for AppBar height (64px) plus shadow clearance (10px).
 Used for scroll positioning to ensure content appears below the fixed header. */
export const HEADER_OFFSET = 74;

export interface TemplateProps {
  title: string;
  /* Fix the height of the main content, otherwise use min-height */
  fixedViewPort?: boolean;
  /* Extra part of the App bar (e.g. Search control) */
  titleExtra?: React.ReactNode;
  /* Extra menu options */
  menuExtra?: React.ReactNode;
  /* The main content */
  children: React.ReactNode;
  /* Additional markup for displaying tabs which integrate with the App bar */
  tabs?: React.ReactNode;
  /* An optional Route for showing a back icon button */
  backRoute?: LocationDescriptor;
  /* Markup to show at the bottom of the main area. E.g. save/cancel options */
  footer?: React.ReactNode;
  fullscreenMode?: FullscreenMode;
  hideAppBar?: boolean;
  menuMode?: MenuMode;
  disableNotifications?: boolean;
  /* Extra meta tags */
  metaTags?: string;
}

export interface TemplateUpdateProps {
  updateTemplate: (update: TemplateUpdate) => void;
}

export type TemplateUpdate = (
  templateProps: Readonly<TemplateProps>,
) => TemplateProps;

/**
 * Return a template {@link TemplateUpdate} which resets to the template to sensible defaults.
 *
 * @remarks
 *
 * The defaults are:
 *
 * No extra title content
 * No back route
 * No tabs
 * No fixed viewport
 * Show app bar
 * No footer content
 * Show the menu
 * Allow notifications links
 * No extra meta tags
 */
export function templateDefaults(title: string): TemplateUpdate {
  return (tp) =>
    ({
      ...tp,
      title,
      backRoute: undefined,
      menuExtra: undefined,
      titleExtra: undefined,
      tabs: undefined,
      fixedViewPort: undefined,
      footer: undefined,
      hideAppBar: undefined,
      fullscreenMode: undefined,
      menuMode: undefined,
      disableNotifications: undefined,
      metaTags: undefined,
    }) as TemplateProps;
}

export function templateError(errorResponse: ErrorResponse): TemplateUpdate {
  return (tp) => ({
    ...tp,
    errorResponse,
  });
}

export function templatePropsForLegacy({
  title,
  metaTags,
  html,
  contentId,
  hideAppBar,
  fullscreenMode,
  menuMode,
}: PageContent): TemplateProps {
  const soHtml = html["so"];
  const menuExtra = soHtml ? (
    <ScreenOptions optionsHtml={soHtml} contentId={contentId} key={contentId} />
  ) : undefined;
  return {
    title,
    metaTags,
    hideAppBar,
    fullscreenMode: fullscreenMode as FullscreenMode,
    menuMode: menuMode as MenuMode,
    menuExtra,
    children: undefined,
  };
}

export const strings = languageStrings.template;

export const coreStrings = languageStrings["com.equella.core"];

const topBarString = coreStrings.topbar.link;

declare const logoURL: string;

interface useFullscreenProps {
  fullscreenMode?: FullscreenMode;
  hideAppBar?: boolean;
}

function useFullscreen({ fullscreenMode, hideAppBar }: useFullscreenProps) {
  const modeIsFullscreen = (function () {
    switch (fullscreenMode) {
      case "YES":
      case "YES_WITH_TOOLBAR":
        return true;

      default:
        return isSelectionSessionOpen() || isItemViewedFromIntegration();
    }
  })();
  return hideAppBar || modeIsFullscreen;
}

const classesPrefix = "Template";
const classes = {
  appBar: `${classesPrefix}-appBar`,
  appFrame: `${classesPrefix}-appFrame`,
  content: `${classesPrefix}-content`,
  contentArea: `${classesPrefix}-contentArea`,
  contentFixedHeight: `${classesPrefix}-contentFixedHeight`,
  contentMinHeight: `${classesPrefix}-contentMinHeight`,
  drawerPaper: `${classesPrefix}-drawerPaper`,
  footer: `${classesPrefix}-footer`,
  logo: `${classesPrefix}-logo`,
  logoImg: `${classesPrefix}-logoImg`,
  navIconHide: `${classesPrefix}-navIconHide`,
  tabs: `${classesPrefix}-tabs`,
  title: `${classesPrefix}-title`,
  titleArea: `${classesPrefix}-titleArea`,
  titleDense: `${classesPrefix}-titleDense`,
  titlePadding: `${classesPrefix}-titlePadding`,
  toolbar: `${classesPrefix}-toolbar`,
  userMenu: `${classesPrefix}-userMenu`,
};

const TemplateRoot = styled("div")(({ theme }) => {
  const menuColors = getOeqTheme().menu;
  const desktop = theme.breakpoints.up("md");
  const drawerWidth = 240;
  const tabHeight = 48;

  return {
    width: "100%",
    zIndex: 1,
    [`& .${classes.appBar}`]: {
      marginLeft: drawerWidth,
      [desktop]: {
        width: `calc(100% - ${drawerWidth}px)`,
      },
    },
    [`& .${classes.appFrame}`]: {
      position: "relative",
    },
    [`& .${classes.content}`]: {
      display: "flex",
      flexDirection: "column",
      [desktop]: {
        marginLeft: drawerWidth,
      },
    },
    [`& .${classes.contentArea}`]: {
      flexGrow: 1,
      flexBasis: 0,
      minHeight: 0,
      padding: theme.spacing(2),
    },
    [`& .${classes.contentFixedHeight}`]: {
      height: "100vh",
    },
    [`& .${classes.contentMinHeight}`]: {
      minHeight: "100vh",
    },
    [`& .${classes.drawerPaper}`]: {
      [desktop]: {
        position: "fixed",
      },
      width: drawerWidth,
      zIndex: 1100,
      background: menuColors.background,
    },
    [`& .${classes.footer}`]: {
      position: "fixed",
      right: 0,
      bottom: 0,
      zIndex: 1000,
      width: "100%",
      [desktop]: {
        width: `calc(100% - ${drawerWidth}px)`,
      },
    },
    [`& .${classes.logo}`]: {
      textAlign: "center",
      marginTop: theme.spacing(2),
    },
    [`& .${classes.logoImg}`]: {
      maxWidth: "100%",
    },
    [`& .${classes.navIconHide}`]: {
      [desktop]: {
        display: "none",
      },
    },
    [`& .${classes.tabs}`]: {
      height: tabHeight,
    },
    [`& .${classes.title}`]: {
      overflow: "hidden",
      whiteSpace: "nowrap",
      textOverflow: "ellipsis",
    },
    [`& .${classes.titleArea}`]: {
      flexGrow: 1,
      display: "flex",
      alignItems: "center",
      overflow: "hidden",
    },
    [`& .${classes.titleDense}`]: {
      marginLeft: theme.spacing(1),
    },
    [`& .${classes.titlePadding}`]: {
      [desktop]: {
        marginLeft: theme.spacing(4),
      },
      marginLeft: theme.spacing(1),
    },
    [`& .${classes.toolbar}`]: theme.mixins.toolbar,
    [`& .${classes.userMenu}`]: {
      flexShrink: 0,
    },
  };
});

export const Template = ({
  backRoute,
  children,
  disableNotifications,
  fixedViewPort,
  footer,
  fullscreenMode,
  hideAppBar,
  menuExtra,
  menuMode,
  tabs,
  title,
  titleExtra,
  metaTags,
}: TemplateProps) => {
  const currentUser = useContext(AppContext).currentUser ?? guestUser;
  const [menuAnchorEl, setMenuAnchorEl] = React.useState<HTMLElement>();
  const [navMenuOpen, setNavMenuOpen] = React.useState(false);

  // Record what customised meta tags have been added into <head>
  const [googleMetaTags, setGoogleMetaTags] = React.useState<Array<string>>([]);

  const theme = getOeqTheme();
  const isMdUp = useMediaQuery<Theme>((theme) => theme.breakpoints.up("md"));

  React.useEffect(() => {
    const classList = window.document.getElementsByTagName("html")[0].classList;
    let remove = "fullscreen-toolbar";
    switch (fullscreenMode) {
      case "YES":
        classList.add("fullscreen");
        break;
      case "YES_WITH_TOOLBAR":
        classList.add(remove);
        remove = "fullscreen";
        break;
      default:
        classList.remove("fullscreen");
    }
    classList.remove(remove);
  }, [fullscreenMode]);

  React.useEffect(() => {
    window.document.title = `${title}${coreStrings.windowtitlepostfix}`;
  }, [title]);

  React.useEffect(() => {
    const head = document.head;
    if (metaTags) {
      const newMetaTags = metaTags.split("\n");
      if (!isEqual(newMetaTags, googleMetaTags)) {
        // The meta tags generated on the server side, separated by new line symbols
        newMetaTags.forEach((newMetaTag) => {
          head.appendChild(
            document.createRange().createContextualFragment(newMetaTag),
          );
        });
        setGoogleMetaTags(newMetaTags);
      }
    } else {
      // While there are no new meta tags to display, also remove old customised meta tags
      const existingMetaTags = document.querySelectorAll("meta");
      existingMetaTags.forEach((existingMetaTag) => {
        if (
          googleMetaTags.some((tag) => {
            return tag === existingMetaTag.outerHTML;
          })
        ) {
          head.removeChild(existingMetaTag);
        }
      });
    }
  }, [metaTags, googleMetaTags]);

  function linkItem(
    link: LocationDescriptor,
    serverSide: boolean,
    text: string,
  ) {
    return (
      <MenuItem
        onClick={() => setMenuAnchorEl(undefined)}
        component={(p: React.AnchorHTMLAttributes<HTMLAnchorElement>) =>
          serverSide ? (
            <a {...p} href={link as string}>
              {}
            </a>
          ) : (
            <Link {...p} to={link} />
          )
        }
      >
        {text}
      </MenuItem>
    );
  }

  function badgedLink(
    icon: React.ReactNode,
    count: number,
    uri: string,
    title: string,
  ) {
    return (
      <Tooltip title={title}>
        <Link to={uri}>
          <IconButton aria-label={title} size="large">
            {count === 0 ? (
              icon
            ) : (
              <Badge badgeContent={count} color="secondary">
                {icon}
              </Badge>
            )}
          </IconButton>
        </Link>
      </Tooltip>
    );
  }

  const hasMenu = menuMode !== "HIDDEN";

  const menuContent = React.useMemo(
    () => (
      <div className={classes.logo}>
        <img
          className={classes.logoImg}
          role="presentation"
          src={logoURL}
          alt="Logo"
        />
        {hasMenu && (
          <MainMenu
            menuGroups={currentUser.menuGroups}
            onClickNavItem={() => setNavMenuOpen(false)}
          />
        )}
      </div>
    ),
    [currentUser, hasMenu],
  );

  const itemCounts = currentUser.counts
    ? currentUser.counts
    : { tasks: 0, notifications: 0 };

  function titleArea() {
    return (
      <div className={classes.titleArea}>
        {backRoute && (
          <Link to={backRoute}>
            <IconButton size="large">
              <BackIcon />
            </IconButton>
          </Link>
        )}
        <Typography
          variant="h5"
          color="inherit"
          className={`${
            backRoute && hasMenu ? classes.titleDense : classes.titlePadding
          } ${classes.title}`}
        >
          {title}
        </Typography>
        {titleExtra}
      </div>
    );
  }

  function menuArea() {
    return (
      <div className={classes.userMenu}>
        {menuExtra}
        {!disableNotifications && !currentUser.guest && (
          <>
            {isMdUp && (
              <>
                <TooltipIconButton
                  title={strings.menu.help}
                  onClick={() =>
                    window.open("https://docs.edalex.com", "_blank")
                  }
                >
                  <HelpIcon />
                </TooltipIconButton>
                {badgedLink(
                  <AssignmentIcon />,
                  itemCounts.tasks,
                  legacyPageUrl(routes.TaskList.to),
                  topBarString.tasks,
                )}
                {badgedLink(
                  <NotificationsIcon />,
                  itemCounts.notifications,
                  legacyPageUrl(routes.Notifications.to),
                  topBarString.notifications,
                )}
              </>
            )}
            <TooltipIconButton
              title={
                currentUser
                  ? currentUser.username
                  : strings.menu.usernameUnknown
              }
              onClick={(e) => setMenuAnchorEl(e.currentTarget)}
              aria-label={strings.menu.title}
            >
              <AccountIcon />
            </TooltipIconButton>
            <Menu
              anchorEl={menuAnchorEl}
              open={Boolean(menuAnchorEl)}
              onClose={(_) => setMenuAnchorEl(undefined)}
              anchorOrigin={{ vertical: "top", horizontal: "right" }}
              transformOrigin={{ vertical: "top", horizontal: "right" }}
            >
              {linkItem(
                legacyPageUrl(routes.Logout.to),
                true,
                strings.menu.logout,
              )}
              {currentUser.prefsEditable &&
                linkItem(
                  legacyPageUrl(routes.UserPreferences.to),
                  false,
                  strings.menu.prefs,
                )}
            </Menu>
          </>
        )}
      </div>
    );
  }

  const fullScreen = useFullscreen({ fullscreenMode, hideAppBar });

  const layoutAppBar = !fullScreen && (
    <AppBar className={classes.appBar}>
      <Toolbar disableGutters>
        {hasMenu && (
          <IconButton
            className={classes.navIconHide}
            onClick={() => setNavMenuOpen(!navMenuOpen)}
            size="large"
          >
            <MenuIcon />
          </IconButton>
        )}
        {titleArea()}
        {menuArea()}
      </Toolbar>
      {tabs}
    </AppBar>
  );

  const layoutDrawer =
    !fullScreen &&
    (isMdUp ? (
      <Drawer
        variant="permanent"
        anchor="left"
        open
        classes={{ paper: classes.drawerPaper }}
      >
        {menuContent}
      </Drawer>
    ) : (
      <Drawer
        variant="temporary"
        anchor="left"
        open={navMenuOpen}
        onClose={(_) => setNavMenuOpen(false)}
      >
        {menuContent}
      </Drawer>
    ));

  const layoutToolbarAndTabs = !fullScreen && (
    <>
      <div className={classes.toolbar} />
      {tabs && <div className={classes.tabs} />}
    </>
  );

  const layoutFooter = !fullScreen && footer && (
    <div className={classes.footer}>{footer}</div>
  );

  // Simple wrapper for `clsx` for easy consideration of fullscreen mode.
  const layoutClasses = (...classes: ClassValue[]): string =>
    clsx(!fullScreen && classes);

  /**
   * Defines the main layout of the page by structuring the above `layoutXyz` constants. This is
   * done to enhance readability to emphasise the structure - especially with regards to support for
   * fullscreen mode.
   *
   * Originally fullscreen mode support was done with a simple ternary expression that would
   * set the layout as `<main>children</main>` when in fullscreen, but otherwise build up a
   * component tree similar to the below. However this meant `children` would be unmounted when
   * changing modes triggering excessive re-rendering. This became an issue with `LegacyContent`
   * making duplicate calls to the server and ending up with incorrect results.
   */
  const layout = (
    <div className={layoutClasses(classes.appFrame)}>
      {layoutAppBar}
      {layoutDrawer}
      <main
        className={layoutClasses([
          classes.content,
          fixedViewPort ? classes.contentFixedHeight : classes.contentMinHeight,
        ])}
      >
        {layoutToolbarAndTabs}
        <div className={layoutClasses(classes.contentArea)}>{children}</div>
      </main>
      {layoutFooter}
    </div>
  );

  return (
    <>
      <CssBaseline />
      <GlobalStyles
        styles={{
          "a, p": {
            // handle long strings without breaking the layout
            overflowWrap: "anywhere",
          },
          button: {
            // button text should not overflow mid-word
            overflowWrap: "break-word",
          },
          a: {
            textDecoration: "none",
            color: theme.palette.primary.main,
          },
        }}
      />
      <TemplateRoot>{layout}</TemplateRoot>
    </>
  );
};
