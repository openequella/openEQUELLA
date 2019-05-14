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
  Divider
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
import { Bridge } from "../api/bridge";
import MessageInfo from "../components/MessageInfo";

interface TemplateProps {
  bridge: Bridge;
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
}

const strings = prepLangStrings("template", {
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

const coreString = prepLangStrings("com.equella.core", {
  windowtitlepostfix: " | openEQUELLA",
  topbar: {
    link: {
      notifications: "Notifications",
      tasks: "Tasks"
    }
  }
});

const topBarString = coreString.topbar.link;

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
      position: "fixed",
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

export function Template(props: TemplateProps) {
  const [currentUser, setCurrentUser] = React.useState<UserData>(guestUser);
  const [menuAnchorEl, setMenuAnchorEl] = React.useState<HTMLElement | null>(
    null
  );
  const [navMenuOpen, setNavMenuOpen] = React.useState(false);
  const [errorOpen, setErrorOpen] = React.useState(false);

  const { router, routes, matchRoute } = props.bridge;

  const classes = useStyles();

  React.useEffect(() => {
    getCurrentUser().then(setCurrentUser);
  }, []);

  function linkItem(clickable: Route, text: string) {
    const { href, onClick } = router(clickable);
    return (
      <MenuItem component="a" href={href} onClick={onClick}>
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
          <IconButton>
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
              onClose={_ => setMenuAnchorEl(null)}
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
            <IconButton className={classes.navIconHide}>
              <MenuIcon onClick={_ => setNavMenuOpen(!navMenuOpen)} />
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

  return (
    <MuiPickersUtilsProvider utils={luxonUtils}>
      <CssBaseline />
      <div className={classes.root}>
        {layout}
        {/* dialog {open: isJust attempt} [
          dialogTitle_ [ text strings.navaway.title], 
          dialogContent_ [
            dialogContentText_ [ text strings.navaway.content ]
          ], 
          dialogActions_ [
            button {onClick: d $ NavAway false, color: secondary} [text commonString.action.cancel],
            button {onClick: d $ NavAway true, color: primary} [text commonString.action.discard]
          ]
        ] ] <> catMaybes [
        toMaybe props.errorResponse <#> \{error, description} -> messageInfo {
                              open: state.errorOpen, 
                              onClose: d CloseError, 
                              title: fromMaybe error $ toMaybe description,
                              variant: String.error 
                            } */}

        {props.errorResponse && renderError(props.errorResponse)}
      </div>
    </MuiPickersUtilsProvider>
  );
}
