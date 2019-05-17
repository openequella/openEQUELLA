import * as React from "react";
import { Route } from "../api/routes";
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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button
} from "@material-ui/core";
import luxonUtils from "@date-io/luxon";
import { makeStyles } from "@material-ui/styles";
import { prepLangStrings } from "../util/langstrings";
import {
  getCurrentUser,
  UserData,
  guestUser,
  MenuItem as MI
} from "../api/currentuser";
import MessageInfo from "../components/MessageInfo";
import { commonString } from "../util/commonstrings";
import { Bridge } from "../api/bridge";

declare const bridge: Bridge;

interface TemplateApi {
  refreshUser: () => void;
}

interface TemplateProps {
  title: String;
  /* Fix the height of the main content, otherwise use min-height */
  fixedViewPort?: boolean;
  /* Extra part of the App bar (e.g. Search control) */
  titleExtra?: React.ReactNode;
  /* Extra menu options */
  menuExtra?: [React.ReactChild];
  /* The main content */
  children: React.ReactNode;
  /* Additional markup for displaying tabs which integrate with the App bar */
  tabs?: React.ReactNode;
  /* Prevent navigation away from this page (e.g. Unsaved data) */
  preventNavigation?: boolean;
  /* An optional Route for showing a back icon button */
  backRoute?: Route;
  /* Markup to show at the bottom of the main area. E.g. save/cancel options */
  footer?: React.ReactNode;
  /* Unexpected errors can be displayed by setting this property */
  errorResponse?: ErrorResponse;
  fullscreenMode?: string;
  hideAppBar?: boolean;
  menuMode?: string;
  disableNotifications?: boolean;
  innerRef?: (api: TemplateApi) => void;
}

export const strings = prepLangStrings("template", {
  menu: {
    title: "My Account",
    logout: "Logout",
    prefs: "My preferences"
  },
  navaway: {
    title: "You have unsaved changes",
    content: "If you leave this page you will lose your changes."
  }
});

export const coreStrings = prepLangStrings("com.equella.core", {
  windowtitlepostfix: " | openEQUELLA",
  topbar: {
    link: {
      notifications: "Notifications",
      tasks: "Tasks"
    }
  }
});

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
        color: theme.palette.primary.main
      }
    },
    root: {
      width: "100%",
      zIndex: 1
    },
    appFrame: {
      position: "relative"
    },
    appBar: {
      marginLeft: drawerWidth,
      [desktop]: {
        width: `calc(100% - ${drawerWidth}px)`
      }
    },
    navIconHide: {
      [desktop]: {
        display: "none"
      }
    },
    content: {
      display: "flex",
      flexDirection: "column",
      [desktop]: {
        marginLeft: drawerWidth
      }
    },
    contentArea: {
      flexGrow: 1,
      flexBasis: 0,
      minHeight: 0
    },
    toolbar: theme.mixins.toolbar,
    tabs: {
      height: tabHeight
    },
    contentMinHeight: {
      minHeight: "100vh"
    },
    contentFixedHeight: {
      height: "100vh"
    },
    titleArea: {
      flexGrow: 1,
      display: "flex",
      alignItems: "center",
      overflow: "hidden"
    },
    titlePadding: {
      [desktop]: {
        marginLeft: theme.spacing.unit * 4
      }
    },
    titleDense: {
      marginLeft: theme.spacing.unit
    },
    title: {
      overflow: "hidden",
      whiteSpace: "nowrap",
      textOverflow: "ellipsis"
    },
    footer: {
      position: "fixed",
      right: 0,
      bottom: 0,
      zIndex: 1000,
      width: "100%",
      [desktop]: {
        width: `calc(100% - ${drawerWidth}px)`
      }
    },
    userMenu: {
      flexShrink: 0
    },
    logo: {
      textAlign: "center",
      marginTop: theme.spacing.unit * 2
    },
    drawerPaper: {
      [desktop]: {
        position: "fixed"
      },
      width: drawerWidth,
      zIndex: 1100,
      background: menuColors.background
    },
    menuItem: {
      color: menuColors.text
    },
    menuIcon: {
      color: menuColors.icon
    }
  };
});

function useFullscreen(props: TemplateProps) {
  const modeIsFullscreen = (function() {
    switch (props.fullscreenMode) {
      case "YES":
        return true;
      case "YES_WITH_TOOLBAR":
        return true;
      default:
        return false;
    }
  })();
  return props.hideAppBar || modeIsFullscreen;
}

const beforeunload = function(e: Event) {
  e.returnValue = ("Are you sure?" as unknown) as boolean;
  return "Are you sure?";
};

export function Template(props: TemplateProps) {
  const [currentUser, setCurrentUser] = React.useState<UserData>(guestUser);
  const [menuAnchorEl, setMenuAnchorEl] = React.useState<HTMLElement>();
  const [navMenuOpen, setNavMenuOpen] = React.useState(false);
  const [errorOpen, setErrorOpen] = React.useState(false);
  const [attemptedRoute, setAttemptedRoute] = React.useState<Route>();
  const { router, routes, matchRoute } = bridge;

  const classes = useStyles();

  React.useEffect(() => {
    bridge.setPreventNav(r => {
      if (props.preventNavigation) {
        setAttemptedRoute(r);
      }
      return Boolean(props.preventNavigation);
    });
    if (props.preventNavigation) {
      window.addEventListener("beforeunload", beforeunload, false);
    } else {
      window.removeEventListener("beforeunload", beforeunload, false);
    }
  }, [props.preventNavigation]);

  React.useEffect(() => {
    if (props.errorResponse) {
      setErrorOpen(true);
    }
  }, [props.errorResponse]);

  React.useEffect(() => {
    getCurrentUser().then(setCurrentUser);
    if (props.innerRef) {
      props.innerRef({
        refreshUser: () => {
          getCurrentUser().then(setCurrentUser);
        }
      });
    }
  }, []);

  React.useEffect(() => {
    const classList = window.document.getElementsByTagName("html")[0].classList;
    var remove = "fullscreen-toolbar";
    switch (props.fullscreenMode) {
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
  }, [props.fullscreenMode]);

  React.useEffect(() => {
    window.document.title = `${props.title}${coreStrings.windowtitlepostfix}`;
  }, [props.title]);

  function menuLink(route: Route) {
    setMenuAnchorEl(undefined);
    bridge.pushRoute(route);
  }

  function linkItem(link: Route, text: string) {
    return (
      <MenuItem
        component="a"
        href={bridge.routeURI(link)}
        onClick={_ => menuLink(link)}
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
        <IconButton aria-label={title} href={uri}>
          {count == 0 ? (
            icon
          ) : (
            <Badge badgeContent={count} color="secondary">
              {icon}
            </Badge>
          )}
        </IconButton>
      </Tooltip>
    );
  }

  function navItem(item: MI, ind: number) {
    const matched = item.route ? matchRoute(item.route) : null;
    const { href, onClick } = matched
      ? router(matched)
      : { href: item.href, onClick: undefined };
    return (
      <ListItem
        component="a"
        href={href}
        key={ind}
        onClick={onClick}
        target={item.newWindow ? "_blank" : undefined}
      >
        <ListItemIcon>
          {item.iconUrl ? (
            <img src={item.iconUrl} />
          ) : (
            <Icon color="inherit" className={classes.menuItem}>
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
  const menuContent = (function() {
    return (
      <div className={classes.logo}>
        <img role="presentation" src={logoURL} />
        {currentUser.menuGroups.map((group, ind) => (
          <React.Fragment key={ind}>
            {ind > 0 && <Divider />}
            <List component="nav">{group.map(navItem)}</List>
          </React.Fragment>
        ))}
      </div>
    );
  })();

  const hasMenu = props.menuMode !== "HIDDEN";

  const itemCounts = currentUser.counts
    ? currentUser.counts
    : { tasks: 0, notifications: 0 };

  function titleArea() {
    return (
      <div className={classes.titleArea}>
        {props.backRoute && (
          <IconButton onClick={_ => bridge.pushRoute(props.backRoute!)}>
            <BackIcon />
          </IconButton>
        )}
        <Typography
          variant="h5"
          color="inherit"
          className={`${
            props.backRoute ? classes.titleDense : classes.titlePadding
          } ${classes.title}`}
        >
          {props.title}
        </Typography>
        {props.titleExtra}
      </div>
    );
  }

  function menuArea() {
    return (
      <div className={classes.userMenu}>
        {props.menuExtra}
        {!props.disableNotifications && !currentUser.guest && (
          <React.Fragment>
            <Hidden mdDown>
              {badgedLink(
                <AssignmentIcon />,
                itemCounts.tasks,
                "access/tasklist.do",
                topBarString.tasks
              )}
              {badgedLink(
                <NotificationsIcon />,
                itemCounts.notifications,
                "access/notifications.do",
                topBarString.notifications
              )}
            </Hidden>
            <Tooltip title={strings.menu.title}>
              <IconButton
                aria-label={strings.menu.title}
                onClick={e => setMenuAnchorEl(e.currentTarget)}
              >
                <AccountIcon />
              </IconButton>
            </Tooltip>
            <Menu
              anchorEl={menuAnchorEl}
              open={Boolean(menuAnchorEl)}
              onClose={_ => setMenuAnchorEl(undefined)}
              anchorOrigin={{ vertical: "top", horizontal: "right" }}
              transformOrigin={{ vertical: "top", horizontal: "right" }}
            >
              {linkItem(routes.Logout, strings.menu.logout)}
              {currentUser.prefsEditable &&
                linkItem(routes.UserPrefs, strings.menu.prefs)}
            </Menu>
          </React.Fragment>
        )}
      </div>
    );
  }

  const layout = useFullscreen(props) ? (
    <main>{props.children}</main>
  ) : (
    <div className={classes.appFrame}>
      <AppBar className={classes.appBar}>
        <Toolbar disableGutters>
          {hasMenu && (
            <IconButton
              className={classes.navIconHide}
              onClick={_ => setNavMenuOpen(!navMenuOpen)}
            >
              <MenuIcon />
            </IconButton>
          )}
          {titleArea()}
          {menuArea()}
        </Toolbar>
        {props.tabs}
      </AppBar>
      {hasMenu && (
        <React.Fragment>
          <Hidden mdUp>
            <Drawer
              variant="temporary"
              anchor="left"
              open={navMenuOpen}
              onClose={_ => setNavMenuOpen(false)}
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
        </React.Fragment>
      )}
      <main
        className={`${classes.content} ${
          props.fixedViewPort
            ? classes.contentFixedHeight
            : classes.contentMinHeight
        }`}
      >
        <div className={classes.toolbar} />
        {props.tabs && <div className={classes.tabs} />}
        <div className={classes.contentArea}>{props.children}</div>
      </main>
      {props.footer && <div className={classes.footer}>{props.footer}</div>}
    </div>
  );

  function renderError(error: ErrorResponse) {
    return (
      <MessageInfo
        open={errorOpen}
        onClose={() => setErrorOpen(false)}
        variant="error"
        title={error.description ? error.description : error.error}
      />
    );
  }

  function navigateConfirm(leave: boolean) {
    if (leave && attemptedRoute) {
      bridge.forcePushRoute(attemptedRoute);
    }
    setAttemptedRoute(undefined);
  }

  return (
    <MuiPickersUtilsProvider utils={luxonUtils}>
      <CssBaseline />
      <div className={classes.root}>
        {layout}
        <Dialog open={Boolean(attemptedRoute)}>
          <DialogTitle>{strings.navaway.title}</DialogTitle>
          <DialogContent>
            <DialogContentText>{strings.navaway.content}</DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button color="secondary" onClick={_ => navigateConfirm(false)}>
              {commonString.action.cancel}
            </Button>
            <Button color="primary" onClick={_ => navigateConfirm(true)}>
              {commonString.action.discard}
            </Button>
          </DialogActions>
        </Dialog>
        {props.errorResponse && renderError(props.errorResponse)}
      </div>
    </MuiPickersUtilsProvider>
  );
}
