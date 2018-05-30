module Template where

import Prelude

import Common.CommonStrings (commonString)
import Control.Monad.Aff.Console (log)
import Control.Monad.Eff (Eff)
import Control.Monad.Eff.Class (liftEff)
import Control.Monad.Eff.Console (CONSOLE)
import Control.Monad.IOEffFn (mkIOFn1, runIOFn1)
import Control.Monad.Reader (ask)
import Control.Monad.State (modify)
import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import DOM (DOM)
import DOM.Event.EventTarget (EventListener, addEventListener, removeEventListener)
import DOM.HTML (window)
import DOM.HTML.Event.EventTypes (beforeunload)
import DOM.HTML.Types (HTMLElement, htmlDocumentToDocument, windowToEventTarget)
import DOM.HTML.Window (document)
import DOM.Node.NonElementParentNode (getElementById)
import DOM.Node.Types (ElementId(ElementId), documentToNonElementParentNode)
import Data.Argonaut (decodeJson)
import Data.Array (catMaybes, concat, intercalate)
import Data.Either (either)
import Data.Maybe (Maybe(Just, Nothing), fromJust, fromMaybe, isJust, isNothing, maybe)
import Data.Nullable (Nullable, toMaybe, toNullable)
import Data.StrMap as M
import Data.String (joinWith)
import Dispatcher (DispatchEff(DispatchEff), fromContext)
import Dispatcher.React (ReactChildren(..), ReactProps(ReactProps), createLifecycleComponent, didMount, getProps, getState, modifyState)
import EQUELLA.Environment (baseUrl, prepLangStrings)
import MaterialUI.AppBar (appBar)
import MaterialUI.Badge (badge, badgeContent)
import MaterialUI.Button (button)
import MaterialUI.Color (inherit, secondary)
import MaterialUI.Color as C
import MaterialUI.CssBaseline (cssBaseline_)
import MaterialUI.Dialog (dialog)
import MaterialUI.DialogActions (dialogActions_)
import MaterialUI.DialogContent (dialogContent_)
import MaterialUI.DialogContentText (dialogContentText_)
import MaterialUI.DialogTitle (dialogTitle_)
import MaterialUI.Divider (divider)
import MaterialUI.Drawer (anchor, drawer, left, open, permanent, temporary)
import MaterialUI.Hidden (css, hidden, implementation, mdUp, smDown)
import MaterialUI.Icon (icon, icon_)
import MaterialUI.IconButton (iconButton)
import MaterialUI.List (list)
import MaterialUI.ListItem (button) as LI
import MaterialUI.ListItem (listItem)
import MaterialUI.ListItemIcon (listItemIcon_)
import MaterialUI.ListItemText (disableTypography, listItemText, primary)
import MaterialUI.Menu (anchorEl, menu)
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Popover (anchorOrigin, transformOrigin)
import MaterialUI.Properties (className, classes_, color, component, mkProp, onClick, onClose, variant)
import MaterialUI.Radio (default)
import MaterialUI.Styles (MediaQuery, allQuery, cssList, mediaQuery, withStyles)
import MaterialUI.TextStyle (subheading)
import MaterialUI.TextStyle as TS
import MaterialUI.Toolbar (disableGutters, toolbar)
import MaterialUI.Tooltip (tooltip, title)
import MaterialUI.Typography (typography)
import MaterialUIPicker.DateFns (momentUtils)
import MaterialUIPicker.MuiPickersUtilsProvider (muiPickersUtilsProvider, utils)
import Network.HTTP.Affjax (get)
import Partial.Unsafe (unsafePartial)
import React (ReactClass, ReactElement, createElement)
import React.DOM (text)
import React.DOM as D
import React.DOM.Props as DP
import ReactDOM (render)
import Routes (Route, forcePushRoute, matchRoute, pushRoute, routeHref, setPreventNav)
import SearchResults (SearchResultsMeta(SearchResultsMeta))

newtype MenuItem = MenuItem {href::String, title::String, systemIcon::Nullable String, route:: Nullable String}

data Command = Init | Updated {preventNavigation :: Nullable Boolean} | AttemptRoute Route | NavAway Boolean
  | ToggleMenu | UserMenuAnchor (Maybe HTMLElement)  | GoBack

type UserData = {
  id::String, 
  guest::Boolean, 
  autoLogin::Boolean, 
  prefsEditable::Boolean
}

type RenderData = {
  baseResources::String, 
  html::Nullable (M.StrMap String), 
  title::String, 
  menuItems :: Array (Array MenuItem), 
  menuMode :: String,
  fullscreenMode :: String,
  hideAppBar :: Boolean,
  newUI::Boolean, 
  user::UserData
}

foreign import preventUnload :: forall e. EventListener e

foreign import renderData :: RenderData

foreign import setTitle :: forall e. String -> Eff (dom::DOM|e) Unit

nullAny :: forall a. Nullable a
nullAny = toNullable Nothing

type TemplateProps = {fixedViewPort :: Nullable Boolean, 
  preventNavigation :: Nullable Boolean, 
  title::String, 
  titleExtra::Nullable ReactElement, 
  menuExtra:: Nullable (Array ReactElement), 
  tabs :: Nullable ReactElement, 
  backRoute :: Nullable Route
}

type State = {mobileOpen::Boolean, menuAnchor::Maybe HTMLElement, tasks :: Maybe Int, notifications :: Maybe Int, attempt :: Maybe Route}

initialState :: State
initialState = {mobileOpen:false, menuAnchor:Nothing, tasks:Nothing, notifications:Nothing, attempt : Nothing}

template :: String -> Array ReactElement -> ReactElement
template title = template' $ templateDefaults title

template' :: TemplateProps -> Array ReactElement -> ReactElement
template' = createElement templateClass

templateDefaults ::  String -> TemplateProps
templateDefaults title = {title,titleExtra:nullAny, fixedViewPort:nullAny,preventNavigation:nullAny, menuExtra:nullAny, 
  tabs:nullAny, backRoute: nullAny}

templateClass :: ReactClass TemplateProps
templateClass = withStyles ourStyles (createLifecycleComponent lifecycle initialState render eval)
  where
  lifecycle = do 
    didMount Init
    modify _ {
      componentDidUpdate = \this {preventNavigation} _ -> do
        (DispatchEff d) <- fromContext eval this
        d Updated {preventNavigation}, 
      componentWillUnmount = \this -> setUnloadListener false 
    }
  newPage = isNothing $ toMaybe renderData.html
  strings = prepLangStrings rawStrings
  coreString = prepLangStrings coreStrings
  drawerWidth = 240
  mobileAppBar = 64 
  desktopAppBar = 56 
  tabHeight = 48 
  ourStyles theme = 
    let desktop :: forall a. {|a} -> MediaQuery
        desktop = mediaQuery $ theme.breakpoints.up "md"
        mobile :: forall a. {|a} -> MediaQuery
        mobile = mediaQuery $ theme.breakpoints.up "sm"
        barVars h = cssList [ 
          mobile {
            "--top-bar": show (mobileAppBar + h) <> "px"
          },
          allQuery {
            "--top-bar": show (desktopAppBar + h) <> "px"
          }
        ]
    in {
    root: {
      width: "100%",
      zIndex: 1
    },
    title: cssList [ 
      desktop {
        marginLeft: theme.spacing.unit * 4
      }, 
      allQuery {
        overflow: "hidden", 
        whiteSpace: "nowrap", 
        textOverflow: "ellipsis",
        marginLeft: theme.spacing.unit
      }
    ],
    appFrame: {
      position: "relative",
      display: "flex"
    },
    appBar: desktop { 
      width: "calc(100% - " <> show drawerWidth <> "px)"
    },
    navIconHide: desktop { 
      display: "none" 
    },
    drawerHeader: theme.mixins.toolbar,
    drawerPaper: cssList [ 
      desktop {
        width: drawerWidth,
        position: "fixed",
        height: "100%",
        zIndex: 0
      },
      allQuery { 
        width: 250 
      }
    ],
    topBar: barVars 0,
    topBarTabs: barVars tabHeight,
    contentMinHeight: {
      minHeight: "calc(100vh - var(--top-bar))"
    },
    contentFixedHeight: {
      height: "calc(100vh - var(--top-bar))"
    },
    "@global": {
        a: {
          textDecoration: "none",
          color: theme.palette.primary.main
        }
    },
    content: cssList [ 
      mobile {
        width: "100%"
      },
      desktop { 
        marginLeft: 240,
        width: "calc(100vw - 245px)"
      },
      allQuery {
        marginTop: "var(--top-bar)"
      }
    ],
    logo: {
      textAlign: "center",
      marginTop: theme.spacing.unit * 2
    }, 
    titleArea: {
      flexGrow: 1,
      display: "flex", 
      alignItems: "center", 
      overflow: "hidden"
    }, 
    userMenu: {
      flexShrink: 0
    }
  }

  setUnloadListener :: forall e. Boolean -> Eff (dom::DOM|e) Unit
  setUnloadListener add = do 
    w <- window
    (if add then addEventListener else removeEventListener) beforeunload preventUnload false $ windowToEventTarget w 

  setPreventUnload add = do 
    (DispatchEff d) <- ask >>= fromContext eval 
    liftEff $ setPreventNav (mkIOFn1 \r -> do 
      if add then d AttemptRoute r else pure unit
      pure add
    )
    liftEff $ setUnloadListener add

  eval (GoBack) = do 
    {backRoute} <- getProps 
    liftEff $ maybe (pure unit) pushRoute $ toMaybe backRoute  
  eval (NavAway n) = do 
    {attempt} <- getState
    liftEff $ guard n *> attempt # maybe (pure unit) forcePushRoute
    modifyState _{attempt = Nothing}
  eval (AttemptRoute r) = do 
    modifyState _{attempt = Just r}
  eval (Updated {preventNavigation:oldpn}) = do 
    {preventNavigation} <- getProps
    let isTrue = fromMaybe false <<< toMaybe
        newPN = isTrue preventNavigation
    if isTrue oldpn /= newPN then setPreventUnload newPN else pure unit
    pure unit
  eval Init = do 
    {title,preventNavigation:pn} <- getProps
    liftEff $ setTitle $ title <> coreString.windowtitlepostfix
    if fromMaybe false $ toMaybe pn then setPreventUnload true else pure unit
    r <- lift $ get $ baseUrl <> "api/task"
    either (lift <<< log) (\(SearchResultsMeta {available}) -> modifyState _ {tasks = Just available})  (decodeJson r.response)
    r2 <- lift $ get $ baseUrl <> "api/notification"
    either (lift <<< log) (\(SearchResultsMeta {available}) -> modifyState _ {notifications = Just available})  (decodeJson r2.response)

  eval ToggleMenu = modifyState \(s :: State) -> s {mobileOpen = not s.mobileOpen}
  eval (UserMenuAnchor el) = modifyState \(s :: State) -> s {menuAnchor = el}

  render {mobileOpen,menuAnchor,tasks,notifications,attempt} (ReactChildren children) (ReactProps props@{fixedViewPort:fvp, classes, 
              title:titleText,titleExtra,menuExtra,backRoute}) 
    (DispatchEff d) = muiPickersUtilsProvider [utils momentUtils] [
    D.div [DP.className classes.root] $ [
      cssBaseline_ [],
      layout renderData.fullscreenMode renderData.menuMode renderData.hideAppBar, 
      dialog [ open $ isJust attempt] [
        dialogTitle_ [ text strings.navaway.title], 
        dialogContent_ [
          dialogContentText_ [ text strings.navaway.content ]
        ], 
        dialogActions_ [
          button [onClick $ d \_ -> NavAway false, color C.primary] [text commonString.action.cancel],
          button [onClick $ d \_ -> NavAway true, color C.secondary] [text commonString.action.discard]
        ]
      ]
    ]
  ]
    where
    tabsM = toMaybe props.tabs
    fixedViewPort = fromMaybe false $ toMaybe fvp 

    contentClass = if fixedViewPort then classes.contentFixedHeight else classes.contentMinHeight
    contentTabClass = if isJust tabsM then classes.topBarTabs else classes.topBar
    content = D.main [ DP.className $ joinWith " " $ [classes.content, contentClass, contentTabClass] ] children
    fullscreen = D.main' children
    layout "YES" _ _ = fullscreen
    layout "YES_WITH_TOOLBAR" _ _ = fullscreen 
    layout _ _ true = fullscreen
    layout _ _ _ = D.div [DP.className classes.appFrame] [
      topBar,
      hidden [ mdUp true ] [
        drawer [ variant temporary, anchor left, classes_ {paper: classes.drawerPaper},
                  open mobileOpen, onClose (d \_ -> ToggleMenu) ] menuContent ],
      hidden [ smDown true, implementation css ] [
        drawer [variant permanent, anchor left, open true, classes_ {paper: classes.drawerPaper} ] menuContent
      ],
      content 
    ]
    hasMenu = case renderData.menuMode of 
      "HIDDEN" -> false 
      _ -> true
    topBar = appBar [className $ classes.appBar] $ catMaybes [
      Just $ toolbar [disableGutters true] $ concat [
        guard hasMenu $> iconButton [
            color C.inherit, className classes.navIconHide, 
            onClick $ d \_ -> ToggleMenu] [ icon_ [D.text "menu" ] 
        ], [
          D.div [DP.className classes.titleArea] $ catMaybes [
            toMaybe backRoute $> iconButton [color C.inherit, onClick $ d \_ -> GoBack] [ icon_ [D.text "arrow_back" ] ],
            Just $ typography [variant TS.headline, color C.inherit, className classes.title] [ D.text titleText ], 
            toMaybe titleExtra
          ],
          userMenu 
        ]
      ], 
      tabsM
    ]
    topBarString = coreString.topbar.link

    userMenu = D.div [DP.className classes.userMenu ] $ (fromMaybe [] $ toMaybe menuExtra) <>
      (guard (not renderData.user.guest) *>
      [
        badgedLink "assignment" tasks "access/tasklist.do" topBarString.tasks , 
        badgedLink "notifications" notifications "access/notifications.do" topBarString.notifications,
        tooltip [title strings.menu.title] [ 
          iconButton [color inherit, mkProp "aria-label" strings.menu.title, onClick $ d \e -> UserMenuAnchor $ Just e.currentTarget] [
            icon_ [ D.text "account_circle"]
          ]
        ],
        menu [
            anchorEl $ toNullable menuAnchor,
            open $ isJust menuAnchor,
            onClose $ d \_ -> UserMenuAnchor Nothing,
            anchorOrigin $ { vertical: "top", horizontal: "right" },
            transformOrigin $ { vertical: "top", horizontal: "right" }
        ] $ catMaybes
          [ Just $ menuItem [component "a", mkProp "href" "logon.do?logout=true"] [D.text strings.menu.logout],
            guard renderData.user.prefsEditable $> menuItem [component "a", mkProp "href" "access/user.do"] [D.text strings.menu.prefs]
          ]
      ])
    badgedLink iconName count uri tip = 
      let iconOnly = icon_ [ D.text iconName ]
          buttonLink col content = iconButton [mkProp "href" uri, color col, mkProp "aria-label" tip ] [ content ]
       in tooltip [ title tip ] [ 
         case fromMaybe 0 count of
            0 -> buttonLink default iconOnly
            c -> buttonLink inherit $ badge [badgeContent c, color secondary] [iconOnly]
       ]
    menuContent = [D.div [DP.className classes.logo] [ D.img [ DP.role "presentation", DP.src logoSrc] []]] <>
                  intercalate [divider []] (group <$> renderData.menuItems)
      where
        logoSrc = renderData.baseResources <> "images/new-equella-logo.png"
        group items = [list [component "nav"] (navItem <$> items)]
        navItem (MenuItem {title,href,systemIcon,route}) = listItem (linkProps <> [LI.button true, component "a"])
          [
            listItemIcon_ [icon [ color C.inherit ] [ D.text $ fromMaybe "folder" $ toMaybe systemIcon ] ],
            listItemText [disableTypography true, primary $ typography [variant subheading, component "div"] [text title]]
          ]
          where 
            linkProps = case routeHref <$> (toMaybe route >>= matchRoute) of
              (Just {href:hr,onClick:oc}) | newPage -> [mkProp "href" hr, onClick $ runIOFn1 oc]
              _ -> [ mkProp "href" href ]


renderReact :: forall eff. String -> ReactElement -> Eff (dom :: DOM, console::CONSOLE | eff) Unit
renderReact divId main = do
  void (elm' >>= render main)
  where

  elm' = do
    win <- window
    doc <- document win
    elm <- getElementById (ElementId divId) (documentToNonElementParentNode (htmlDocumentToDocument doc))
    pure $ unsafePartial (fromJust elm)


renderMain :: forall eff. ReactElement -> Eff (dom :: DOM, console::CONSOLE | eff) Unit
renderMain = renderReact "mainDiv"

rawStrings = {prefix: "template", 
  strings: {
    menu: {
      title: "My Account",
      logout:"Logout",
      prefs:"My preferences"
    }, 
    navaway: {
      title:  "You have unsaved changes", 
      content: "If you leave this page you will lose your changes."
    }
  }
}

coreStrings = {prefix: "com.equella.core",
  strings: {
    windowtitlepostfix: " | EQUELLA",
    topbar: { 
      link: {
        notifications: "Notifications",
        tasks: "Tasks"
      }
    }
  }
}