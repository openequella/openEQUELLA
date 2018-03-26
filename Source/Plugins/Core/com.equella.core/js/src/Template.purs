module Template where

import Prelude

import Control.Monad.Eff (Eff)
import Control.Monad.Eff.Console (CONSOLE)
import Control.Monad.IOEffFn (runIOFn1)
import Control.MonadZero (guard)
import DOM (DOM)
import DOM.HTML (window)
import DOM.HTML.Types (HTMLElement, htmlDocumentToDocument)
import DOM.HTML.Window (document)
import DOM.Node.NonElementParentNode (getElementById)
import DOM.Node.Types (ElementId(ElementId), documentToNonElementParentNode)
import Data.Array (catMaybes, intercalate)
import Data.Maybe (Maybe(..), fromJust, fromMaybe, isJust, isNothing)
import Data.Nullable (Nullable, toMaybe, toNullable)
import Data.StrMap as M
import Data.Tuple (Tuple(..))
import Data.Unfoldable as U
import Dispatcher (DispatchEff(DispatchEff), effEval)
import Dispatcher.React (ReactProps(ReactProps), createComponent, modifyState)
import EQUELLA.Environment (prepLangStrings)
import MaterialUI.AppBar (appBar)
import MaterialUI.ButtonBase (onClick)
import MaterialUI.Color (inherit)
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
import MaterialUI.Modal (onClose)
import MaterialUI.Popover (anchorOrigin, transformOrigin)
import MaterialUI.PropTypes (handle)
import MaterialUI.Properties (className, classes_, color, component, mkProp, variant)
import MaterialUI.Styles (mediaQuery, withStyles)
import MaterialUI.TextStyle (title)
import MaterialUI.Toolbar (disableGutters, toolbar)
import MaterialUI.Typography (typography)
import MaterialUIPicker.DateFns (dateFnsUtils)
import MaterialUIPicker.MuiPickersUtilsProvider (muiPickersUtilsProvider, utils)
import Partial.Unsafe (unsafePartial)
import React (ReactElement, createFactory)
import React.DOM as D
import React.DOM.Props as DP
import ReactDOM (render)
import Routes (matchRoute, routeHref)

newtype MenuItem = MenuItem {href::String, title::String, systemIcon::Nullable String, route:: Nullable String}

data Command = ToggleMenu | UserMenuAnchor (Maybe HTMLElement)

type RenderData = {baseResources::String, html::Nullable (M.StrMap String), title::String, menuItems :: Array (Array MenuItem), newUI::Boolean, user::UserData}
type UserData = {id::String, guest::Boolean, autoLogin::Boolean, prefsEditable::Boolean}

foreign import renderData :: RenderData

type State = {mobileOpen::Boolean, menuAnchor::Maybe HTMLElement}

initialState :: State
initialState = {mobileOpen:false, menuAnchor:Nothing}

template :: {mainContent :: ReactElement, title::String, titleExtra::Maybe ReactElement} -> ReactElement
template {mainContent,title,titleExtra} = template' {mainContent,title,titleExtra,menuExtra:[]}

template' :: {mainContent :: ReactElement, title::String, titleExtra::Maybe ReactElement, 
  menuExtra::Array ReactElement} -> ReactElement
template' = createFactory (withStyles ourStyles (createComponent initialState render (effEval eval)))
  where
  newPage = isNothing $ toMaybe renderData.html
  strings = prepLangStrings rawStrings
  drawerWidth = 240
  ourStyles theme = {
    root: {
      width: "100%",
      zIndex: 1
    },
    title: mediaQuery (theme.breakpoints.up "md") {
      marginLeft: theme.spacing.unit * 4
    } {
      marginLeft: theme.spacing.unit
    },
    extraTool: {
      flex: 1
    },
    appFrame: {
      position: "relative",
      display: "flex"
    },
    appBar: mediaQuery (theme.breakpoints.up "md") {
        width: "calc(100% - " <> show drawerWidth <> "px)"
      } {
    },
    navIconHide:
      mediaQuery (theme.breakpoints.up "md") {
        display: "none"
      } {
      }
    ,
    drawerHeader: theme.mixins.toolbar,
    drawerPaper: mediaQuery (theme.breakpoints.up "md") {
        width: drawerWidth,
        position: "relative",
        height: "100%"
      }
      { width: 250 },
    content: mediaQuery (theme.breakpoints.up "sm") {
        height: "calc(100% - 64px)",
        marginTop: 64
      } {
      backgroundColor: "#eee", -- theme.palette.background.default,
      width: "100%",
      padding: theme.spacing.unit * 2,
      height: "calc(100% - 56px)",
      marginTop: 56
    },
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
        (Just {href:hr,onClick:oc}) | newPage -> [mkProp "href" hr, onClick $ handle $ runIOFn1 oc]
        _ -> [ mkProp "href" href ]


  eval ToggleMenu = modifyState \(s :: State) -> s {mobileOpen = not s.mobileOpen}
  eval (UserMenuAnchor el) = modifyState \(s :: State) -> s {menuAnchor = el}

  render {mobileOpen,menuAnchor} (ReactProps {classes,mainContent,title:titleText,titleExtra,menuExtra}) 
    (DispatchEff d) = muiPickersUtilsProvider [utils dateFnsUtils] [
    D.div [DP.className classes.root] [
      cssBaseline_ [],
      D.div [DP.className classes.appFrame] [
        topBar,
        hidden [ mdUp true ] [
          drawer [ variant temporary, anchor left, classes_ {paper: classes.drawerPaper},
                    open mobileOpen, onClose (handle $ d \_ -> ToggleMenu) ] menuContent ],
        hidden [ smDown true, implementation css ] [
          drawer [variant permanent, anchor left, open true, classes_ {paper: classes.drawerPaper} ] menuContent
        ],
        D.main [ DP.className classes.content ] [mainContent]
      ]
    ]
  ]
    where
    topBar = appBar [className $ classes.appBar] [
      toolbar [disableGutters true] [
        iconButton [color C.inherit, className classes.navIconHide, onClick $ handle $ d \_ -> ToggleMenu] [ icon_ [D.text "menu" ] ],
        typography [variant title, color C.inherit, className classes.title] [ D.text titleText ],
        D.div [DP.className classes.extraTool] (U.fromMaybe titleExtra),
        userMenu
      ]
    ]
    userMenu = D.div' $ menuExtra <>
      (guard (not renderData.user.guest) *>
      [
        iconButton [color inherit, onClick $ handle $ d \e -> UserMenuAnchor $ Just e.currentTarget] [
          icon_ [ D.text "account_circle"]
        ],
        menu [
            anchorEl $ toNullable menuAnchor,
            open $ isJust menuAnchor,
            onClose $ handle $ d \_ -> UserMenuAnchor Nothing,
            anchorOrigin $ { vertical: "top", horizontal: "right" },
            transformOrigin $ { vertical: "top", horizontal: "right" }
        ] $ catMaybes
          [ Just $ menuItem [component "a", mkProp "href" "logon.do?logout=true"] [D.text strings.menu.logout],
            guard renderData.user.prefsEditable $> menuItem [component "a", mkProp "href" "access/user.do"] [D.text strings.menu.prefs]
          ]
      ])
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
