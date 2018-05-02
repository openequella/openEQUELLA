module Template where

import Prelude

import Control.Monad.Aff.Console (log)
import Control.Monad.Eff (Eff)
import Control.Monad.Eff.Class (liftEff)
import Control.Monad.Eff.Console (CONSOLE)
import Control.Monad.IOEffFn (runIOFn1)
import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import DOM (DOM)
import DOM.HTML (window)
import DOM.HTML.Types (HTMLElement, htmlDocumentToDocument)
import DOM.HTML.Window (document)
import DOM.Node.NonElementParentNode (getElementById)
import DOM.Node.Types (ElementId(ElementId), documentToNonElementParentNode)
import Data.Argonaut (decodeJson)
import Data.Array (catMaybes, intercalate)
import Data.Either (either)
import Data.Maybe (Maybe(Just, Nothing), fromJust, fromMaybe, isJust, isNothing)
import Data.Nullable (Nullable, toMaybe, toNullable)
import Data.StrMap as M
import Data.String (joinWith)
import Data.Tuple (Tuple(..))
import Data.Unfoldable as U
import Dispatcher (DispatchEff(DispatchEff))
import Dispatcher.React (ReactProps(ReactProps), createLifecycleComponent, didMount, getProps, modifyState)
import EQUELLA.Environment (baseUrl, prepLangStrings)
import MaterialUI.AppBar (appBar)
import MaterialUI.Badge (badge, badgeContent)
import MaterialUI.Button (disabled)
import MaterialUI.Color (inherit, secondary)
import MaterialUI.Color as C
import MaterialUI.CssBaseline (cssBaseline_)
import MaterialUI.Divider (divider)
import MaterialUI.Drawer (anchor, drawer, left, open, permanent, temporary)
import MaterialUI.Hidden (css, hidden, implementation, mdUp, smDown)
import MaterialUI.Icon (icon, icon_)
import MaterialUI.IconButton (iconButton)
import MaterialUI.List (list_)
import MaterialUI.ListItem (button, listItem)
import MaterialUI.ListItemIcon (listItemIcon_)
import MaterialUI.ListItemText (listItemText, primary)
import MaterialUI.Menu (anchorEl, menu)
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Popover (anchorOrigin, transformOrigin)
import MaterialUI.Properties (className, classes_, color, component, mkProp, onClick, onClose, variant)
import MaterialUI.Radio (default)
import MaterialUI.Styles (MediaQuery, allQuery, cssList, mediaQuery, withStyles)
import MaterialUI.TextStyle as TS
import MaterialUI.Toolbar (disableGutters, toolbar)
import MaterialUI.Tooltip (tooltip, title)
import MaterialUI.Typography (typography)
import MaterialUIPicker.DateFns (dateFnsUtils)
import MaterialUIPicker.MuiPickersUtilsProvider (muiPickersUtilsProvider, utils)
import Network.HTTP.Affjax (get)
import Partial.Unsafe (unsafePartial)
import React (ReactElement, createFactory)
import React.DOM as D
import React.DOM.Props as DP
import ReactDOM (render)
import Routes (matchRoute, routeHref)
import SearchResults (SearchResultsMeta(SearchResultsMeta))

newtype MenuItem = MenuItem {href::String, title::String, systemIcon::Nullable String, route:: Nullable String}

data Command = Init | ToggleMenu | UserMenuAnchor (Maybe HTMLElement)

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


foreign import renderData :: RenderData

foreign import setTitle :: forall e. String -> Eff (dom::DOM|e) Unit

type State = {mobileOpen::Boolean, menuAnchor::Maybe HTMLElement, tasks :: Maybe Int, notifications :: Maybe Int}

initialState :: State
initialState = {mobileOpen:false, menuAnchor:Nothing, tasks:Nothing, notifications:Nothing}

template :: {mainContent :: ReactElement, title::String, titleExtra::Maybe ReactElement} -> ReactElement
template {mainContent,title,titleExtra} = template' {fixedViewPort : false, mainContent,title,titleExtra,menuExtra:[]}

template' :: {fixedViewPort :: Boolean, mainContent :: ReactElement, title::String, titleExtra::Maybe ReactElement, 
  menuExtra::Array ReactElement} -> ReactElement
template' = createFactory (withStyles ourStyles (createLifecycleComponent (didMount Init) initialState render eval))
  where
  newPage = isNothing $ toMaybe renderData.html
  strings = prepLangStrings rawStrings
  coreString = prepLangStrings coreStrings
  drawerWidth = 240
  ourStyles theme = 
    let desktop :: forall a. {|a} -> MediaQuery
        desktop = mediaQuery $ theme.breakpoints.up "md"
        mobile :: forall a. {|a} -> MediaQuery
        mobile = mediaQuery $ theme.breakpoints.up "sm"
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
        marginLeft: theme.spacing.unit
      }
    ],
    extraTool: {
      flex: 1
    },
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
    contentNoScroll: cssList [
      mobile {
        height: "calc(100vh - 64px)"
      }, 
      allQuery {
        height: "calc(100vh - 56px)"
      }
    ],
    contentScroll: cssList [
      mobile { 
        minHeight: "calc(100vh - 64px)"
      }, 
      allQuery {
        minHeight: "calc(100vh - 56px)"
      }
    ],
    content: cssList [ 
      mobile {
        marginTop: 64, 
        width: "100%"
      },
      desktop { 
        marginLeft: 240,
        width: "calc(100vw - 245px)"
      },
      allQuery {
        marginTop: 56,
        backgroundColor: "#eee", -- theme.palette.background.default,
        padding: theme.spacing.unit * 2
      }
    ],
    logo: {
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      marginTop: theme.spacing.unit * 2,
      marginBottom: theme.spacing.unit
    }
  }

  navItem (MenuItem {title,href,systemIcon,route}) = listItem (linkProps <> [button true, component "a" ])
    [
      listItemIcon_ [icon [ color C.inherit ] [ D.text $ fromMaybe "folder" $ toMaybe systemIcon ] ],
      listItemText [primary title]
    ]
    where 
      linkProps = case routeHref <$> (toMaybe route >>= matchRoute) of
        (Just {href:hr,onClick:oc}) | newPage -> [mkProp "href" hr, onClick $ runIOFn1 oc]
        _ -> [ mkProp "href" href ]


  eval Init = do 
    {title} <- getProps
    liftEff $ setTitle $ title <> coreString.windowtitlepostfix
    r <- lift $ get $ baseUrl <> "api/task"
    either (lift <<< log) (\(SearchResultsMeta {available}) -> modifyState _ {tasks = Just available})  (decodeJson r.response)
    r2 <- lift $ get $ baseUrl <> "api/notification"
    either (lift <<< log) (\(SearchResultsMeta {available}) -> modifyState _ {notifications = Just available})  (decodeJson r2.response)

  eval ToggleMenu = modifyState \(s :: State) -> s {mobileOpen = not s.mobileOpen}
  eval (UserMenuAnchor el) = modifyState \(s :: State) -> s {menuAnchor = el}

  render {mobileOpen,menuAnchor,tasks,notifications} (ReactProps {fixedViewPort, classes, mainContent, 
              title:titleText,titleExtra,menuExtra}) 
    (DispatchEff d) = muiPickersUtilsProvider [utils dateFnsUtils] [
    D.div [DP.className classes.root] $ [
      cssBaseline_ [],
      layout renderData.fullscreenMode renderData.menuMode renderData.hideAppBar
    ]
  ]
    where
    contentClass = if fixedViewPort then classes.contentNoScroll else classes.contentScroll
    content = D.main [ DP.className $ joinWith " " [classes.content, contentClass] ] [ mainContent]
    fullscreen = D.main' [ mainContent ]
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
    
    topBar = appBar [className $ classes.appBar] [
      toolbar [disableGutters true] [
        iconButton [color C.inherit, className classes.navIconHide, onClick $ d \_ -> ToggleMenu] [ icon_ [D.text "menu" ] ],
        typography [variant TS.title, color C.inherit, className classes.title] [ D.text titleText ],
        D.div [DP.className classes.extraTool] (U.fromMaybe titleExtra),
        userMenu
      ]
    ]
    topBarString = coreString.topbar.link

    userMenu = D.div' $ menuExtra <>
      (guard (not renderData.user.guest) *>
      [
        badgedLink "assignment" tasks "access/tasklist.do" topBarString.tasks , 
        badgedLink "notifications" notifications "access/notifications.do" topBarString.notifications,
        iconButton [color inherit, onClick $ d \e -> UserMenuAnchor $ Just e.currentTarget] [
          icon_ [ D.text "account_circle"]
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
          buttonLink col content = iconButton [mkProp "href" uri, color col] [ content ]
       in tooltip [ title tip ] [ 
         case fromMaybe 0 count of
            0 -> buttonLink default iconOnly
            c -> buttonLink inherit $ badge [badgeContent c, color secondary] [iconOnly]
       ]
    menuContent = [D.div [DP.className classes.logo] [ D.img [ DP.src logoSrc] []]] <>
                  intercalate [divider []] (group <$> renderData.menuItems)
      where
        logoSrc = renderData.baseResources <> "images/new-equella-logo.png"
        group items = [list_ (navItem <$> items)]


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

rawStrings :: Tuple String
  { menu :: { logout :: String
            , prefs :: String
            }
  }
rawStrings = Tuple "template" {
  menu: {
    logout:"Logout",
    prefs:"My preferences"
  }
}

coreStrings = Tuple "com.equella.core" {
  windowtitlepostfix: " | EQUELLA",
  topbar: { 
    link: {
      notifications: "Notifications",
      tasks: "Tasks"
    }
  }
}