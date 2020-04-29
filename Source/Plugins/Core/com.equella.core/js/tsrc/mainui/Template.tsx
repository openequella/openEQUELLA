import * as React from "react";
import { ErrorResponse } from "../api/errors";
import { MuiPickersUtilsProvider } from "material-ui-pickers";
import MenuIcon from "@material-ui/icons/Menu";
import BackIcon from "@material-ui/icons/ArrowBack";
import AccountIcon from "@material-ui/icons/AccountCircle";
import AssignmentIcon from "@material-ui/icons/Assignment";
import NotificationsIcon from "@material-ui/icons/Notifications";
import {
  CssBaseline,
  Theme,
  AppBar,
  Toolbar,
  IconButton,
  Typography,
  Hidden,
  Tooltip,
  Menu,
  MenuItem,
  Badge,
  Drawer,
  ListItem,
  Icon,
  List,
  ListItemIcon,
  ListItemText,
  Divider,
} from "@material-ui/core";
import luxonUtils from "@date-io/luxon";
import { makeStyles } from "@material-ui/styles";
import { languageStrings } from "../util/langstrings";
import { UserData, guestUser, MenuItem as MI } from "../api/currentuser";
import MessageInfo from "../components/MessageInfo";
import { Link } from "react-router-dom";
import { LocationDescriptor } from "history";
import { routes } from "./routes";

export type MenuMode = "HIDDEN" | "COLLAPSED" | "FULL";
export type FullscreenMode = "YES" | "YES_WITH_TOOLBAR" | "NO";
export interface TemplateProps {
  title: String;
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
  /* Unexpected errors can be displayed by setting this property */
  errorResponse?: ErrorResponse;
  fullscreenMode?: FullscreenMode;
  hideAppBar?: boolean;
  menuMode?: MenuMode;
  disableNotifications?: boolean;
  currentUser?: UserData;
  /* Extra meta tags */
  metaTags?: string;
}

export interface TemplateUpdateProps {
  updateTemplate: (update: TemplateUpdate) => void;
}

export type TemplateUpdate = (
  templateProps: Readonly<TemplateProps>
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
    } as TemplateProps);
}

export function templateError(errorResponse: ErrorResponse): TemplateUpdate {
  return (tp) => ({
    ...tp,
    errorResponse,
  });
}

export const strings = languageStrings.template;

export const coreStrings = languageStrings["com.equella.core"];

const topBarString = coreStrings.topbar.link;

declare const logoURL: string;

interface ExtTheme {
  menu: {
    background: string;
    text: string;
    icon: string;
  };
}

export const useStyles = makeStyles((theme: Theme) => {
  const menuColors = ((theme.palette as unknown) as ExtTheme).menu;
  const desktop = theme.breakpoints.up("md");
  const drawerWidth = 240;
  const tabHeight = 48;
  return {
    "@global": {
      a: {
        textDecoration: "none",
        color: theme.palette.primary.main,
      },
    },
    root: {
      width: "100%",
      zIndex: 1,
    },
    appFrame: {
      position: "relative",
    },
    appBar: {
      marginLeft: drawerWidth,
      [desktop]: {
        width: `calc(100% - ${drawerWidth}px)`,
      },
    },
    navIconHide: {
      [desktop]: {
        display: "none",
      },
    },
    content: {
      display: "flex",
      flexDirection: "column",
      [desktop]: {
        marginLeft: drawerWidth,
      },
    },
    contentArea: {
      flexGrow: 1,
      flexBasis: 0,
      minHeight: 0,
    },
    toolbar: theme.mixins.toolbar,
    tabs: {
      height: tabHeight,
    },
    contentMinHeight: {
      minHeight: "100vh",
    },
    contentFixedHeight: {
      height: "100vh",
    },
    titleArea: {
      flexGrow: 1,
      display: "flex",
      alignItems: "center",
      overflow: "hidden",
    },
    titlePadding: {
      [desktop]: {
        marginLeft: theme.spacing(4),
      },
      marginLeft: theme.spacing(1),
    },
    titleDense: {
      marginLeft: theme.spacing(1),
    },
    title: {
      overflow: "hidden",
      whiteSpace: "nowrap",
      textOverflow: "ellipsis",
    },
    footer: {
      position: "fixed",
      right: 0,
      bottom: 0,
      zIndex: 1000,
      width: "100%",
      [desktop]: {
        width: `calc(100% - ${drawerWidth}px)`,
      },
    },
    userMenu: {
      flexShrink: 0,
    },
    logo: {
      textAlign: "center",
      marginTop: theme.spacing(2),
    },
    drawerPaper: {
      [desktop]: {
        position: "fixed",
      },
      width: drawerWidth,
      zIndex: 1100,
      background: menuColors.background,
    },
    menuItem: {
      color: menuColors.text,
    },
    menuIcon: {
      color: menuColors.icon,
    },
  };
});

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
        return false;
    }
  })();
  return hideAppBar || modeIsFullscreen;
}

export const Template = React.memo(function Template({
  backRoute,
  children,
  currentUser = guestUser,
  disableNotifications,
  errorResponse,
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
}: TemplateProps) {
  const [menuAnchorEl, setMenuAnchorEl] = React.useState<HTMLElement>();
  const [navMenuOpen, setNavMenuOpen] = React.useState(false);
  const [errorOpen, setErrorOpen] = React.useState(false);

  // Record what customised meta tags have been added into <head>
  const [googleMetaTags, setGoogleMetaTags] = React.useState<Array<string>>([]);

  const classes = useStyles();

  React.useEffect(() => {
    if (errorResponse) {
      setErrorOpen(true);
    }
  }, [errorResponse]);

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
    updateMetaTags(metaTags);
  }, [metaTags]);

  function updateMetaTags(tags: string | undefined) {
    const head = document.head;
    if (tags) {
      // The meta tags generated on the server side, separated by new line symbols
      const newMetaTags = tags.split("\n");
      newMetaTags.forEach((newMetaTag) => {
        head.appendChild(
          document.createRange().createContextualFragment(newMetaTag)
        );
      });
      setGoogleMetaTags(newMetaTags);
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
  }

  function linkItem(
    link: LocationDescriptor,
    serverSide: boolean,
    text: string
  ) {
    return (
      <MenuItem
        component={(p) =>
          serverSide ? (
            <a {...p} href={link as string} />
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
    title: string
  ) {
    return (
      <Tooltip title={title}>
        <Link to={uri}>
          <IconButton aria-label={title}>
            {count == 0 ? (
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

  function navItem(item: MI, ind: number) {
    return (
      <ListItem
        component={(p) => {
          const props = {
            ...p,
            target: item.newWindow ? "_blank" : undefined,
            onClick: () => setNavMenuOpen(false),
          };
          return item.route ? (
            <Link {...props} to={item.route} />
          ) : (
            <a {...props} href={item.href} />
          );
        }}
        key={ind}
        button={true}
      >
        <ListItemIcon>
          {item.iconUrl ? (
            <img src={item.iconUrl} alt={item.title} />
          ) : (
            <Icon color="inherit" className={classes.menuIcon}>
              {item.systemIcon ? item.systemIcon : "folder"}
            </Icon>
          )}
        </ListItemIcon>
        <ListItemText
          disableTypography
          primary={
            <Typography
              variant="subtitle1"
              className={classes.menuItem}
              component="div"
            >
              {item.title}
            </Typography>
          }
        />
      </ListItem>
    );
  }

  const hasMenu = menuMode !== "HIDDEN";

  const menuContent = React.useMemo(
    () => (
      <div className={classes.logo}>
        <img role="presentation" src={logoURL} alt={"Logo"} />
        {hasMenu && (
          <div id="menulinks">
            {currentUser.menuGroups.map((group, ind) => (
              <React.Fragment key={ind}>
                {ind > 0 && <Divider />}
                <List component="nav">{group.map(navItem)}</List>
              </React.Fragment>
            ))}
          </div>
        )}
      </div>
    ),
    [currentUser, hasMenu]
  );

  const itemCounts = currentUser.counts
    ? currentUser.counts
    : { tasks: 0, notifications: 0 };

  function titleArea() {
    return (
      <div className={classes.titleArea}>
        {backRoute && (
          <Link to={backRoute}>
            <IconButton>
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
          <React.Fragment>
            <Hidden smDown>
              {badgedLink(
                <AssignmentIcon />,
                itemCounts.tasks,
                routes.TaskList.to,
                topBarString.tasks
              )}
              {badgedLink(
                <NotificationsIcon />,
                itemCounts.notifications,
                routes.Notifications.to,
                topBarString.notifications
              )}
            </Hidden>
            <Tooltip title={strings.menu.title}>
              <IconButton
                aria-label={strings.menu.title}
                onClick={(e) => setMenuAnchorEl(e.currentTarget)}
              >
                <AccountIcon />
              </IconButton>
            </Tooltip>
            <Menu
              anchorEl={menuAnchorEl}
              open={Boolean(menuAnchorEl)}
              onClose={(_) => setMenuAnchorEl(undefined)}
              anchorOrigin={{ vertical: "top", horizontal: "right" }}
              transformOrigin={{ vertical: "top", horizontal: "right" }}
            >
              {linkItem(routes.Logout.to, true, strings.menu.logout)}
              {currentUser.prefsEditable &&
                linkItem(routes.UserPreferences.to, false, strings.menu.prefs)}
            </Menu>
          </React.Fragment>
        )}
      </div>
    );
  }

  const layout = useFullscreen({ fullscreenMode, hideAppBar }) ? (
    <main>{children}</main>
  ) : (
    <div className={classes.appFrame}>
      <AppBar className={classes.appBar}>
        <Toolbar disableGutters>
          {hasMenu && (
            <IconButton
              className={classes.navIconHide}
              onClick={(_) => setNavMenuOpen(!navMenuOpen)}
            >
              <MenuIcon />
            </IconButton>
          )}
          {titleArea()}
          {menuArea()}
        </Toolbar>
        {tabs}
      </AppBar>
      <Hidden mdUp>
        <Drawer
          variant="temporary"
          anchor="left"
          open={navMenuOpen}
          onClose={(_) => setNavMenuOpen(false)}
        >
          {menuContent}
        </Drawer>
      </Hidden>
      <Hidden smDown implementation="css">
        <Drawer
          variant="permanent"
          anchor="left"
          open
          classes={{ paper: classes.drawerPaper }}
        >
          {menuContent}
        </Drawer>
      </Hidden>
      <main
        className={`${classes.content} ${
          fixedViewPort ? classes.contentFixedHeight : classes.contentMinHeight
        }`}
      >
        <div className={classes.toolbar} />
        {tabs && <div className={classes.tabs} />}
        <div className={classes.contentArea}>{children}</div>
      </main>
      {footer && <div className={classes.footer}>{footer}</div>}
    </div>
  );

  function renderError(error: ErrorResponse) {
    return (
      <MessageInfo
        open={errorOpen}
        onClose={() => setErrorOpen(false)}
        variant="error"
        title={error.error_description ? error.error_description : error.error}
      />
    );
  }

  return (
    <MuiPickersUtilsProvider utils={luxonUtils}>
      <CssBaseline />
      <div className={classes.root}>
        {layout}
        {errorResponse && renderError(errorResponse)}
      </div>
    </MuiPickersUtilsProvider>
  );
});
