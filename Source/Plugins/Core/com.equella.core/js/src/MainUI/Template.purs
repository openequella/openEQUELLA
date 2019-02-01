module OEQ.MainUI.Template where

import Prelude

import Common.CommonStrings (commonString)
import Control.Bind (bindFlipped)
import Control.Monad.Reader (ReaderT)
import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import Data.Argonaut (class DecodeJson, Json, decodeJson, (.?), (.??))
import Data.Array (catMaybes, concat, intercalate)
import Data.Either (Either(..), either)
import Data.Lens (Lens', _Just, preview, (^?))
import Data.Lens.Record (prop)
import Data.Maybe (Maybe(Just, Nothing), fromJust, fromMaybe, isJust, maybe)
import Data.Nullable (Nullable, toMaybe, toNullable)
import Data.String (joinWith)
import Data.Symbol (SProxy(..))
import Data.Traversable (traverse)
import Dispatcher (affAction)
import Dispatcher.React (getProps, getState, modifyState, renderer)
import Effect (Effect)
import Effect.Aff (Aff)
import Effect.Class (liftEffect)
import Effect.Class.Console (log)
import Effect.Uncurried (EffectFn1, mkEffectFn1)
import Foreign.Object as Object
import MaterialUI.AppBar (appBar)
import MaterialUI.Badge (badge)
import MaterialUI.Button (button)
import MaterialUI.Dialog (dialog)
import MaterialUI.DialogActions (dialogActions_)
import MaterialUI.DialogContent (dialogContent_)
import MaterialUI.DialogContentText (dialogContentText_)
import MaterialUI.DialogTitle (dialogTitle_)
import MaterialUI.Divider (divider_)
import MaterialUI.Drawer (drawer)
import MaterialUI.Enums (css, headline, inherit, left, permanent, primary, secondary, subheading, temporary)
import MaterialUI.Enums as String
import MaterialUI.Hidden (hidden)
import MaterialUI.Icon (icon, icon_)
import MaterialUI.IconButton (iconButton)
import MaterialUI.List (list)
import MaterialUI.ListItem as RMUI
import MaterialUI.ListItemIcon (listItemIcon_)
import MaterialUI.ListItemText (listItemText')
import MaterialUI.Menu (menu)
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Styles (MediaQuery, allQuery, cssList, mediaQuery, withStyles)
import MaterialUI.Toolbar (toolbar)
import MaterialUI.Tooltip (tooltip)
import MaterialUI.Typography (typography)
import Network.HTTP.Affjax (get)
import Network.HTTP.Affjax.Response as Resp
import OEQ.Data.Error (ErrorResponse)
import OEQ.Environment (baseUrl, prepLangStrings)
import OEQ.MainUI.Routes (Route, forcePushRoute, logoutRoute, matchRoute, pushRoute, routeHref, routeURI, setPreventNav, userPrefsRoute)
import OEQ.UI.Common (rootTag, withCurrentTarget)
import OEQ.UI.MessageInfo (messageInfo)
import OEQ.Utils.Interop (nullAny)
import Partial.Unsafe (unsafePartial)
import React (Children, ReactClass, ReactElement, ReactThis, childrenToArray, createElement)
import React as R
import React.DOM (footer, img, text)
import React.DOM as D
import React.DOM.Props (src)
import React.DOM.Props as DP
import Web.DOM.DOMTokenList as DOMTokens
import Web.DOM.Document (documentElement)
import Web.Event.EventTarget (EventListener, addEventListener, removeEventListener)
import Web.HTML (HTMLElement, window)
import Web.HTML.Event.BeforeUnloadEvent.EventTypes (beforeunload)
import Web.HTML.HTMLDocument (toDocument)
import Web.HTML.HTMLElement (classList)
import Web.HTML.HTMLElement as HTML
import Web.HTML.Window (document, toEventTarget)

newtype ExternalHref = ExternalHref String 
newtype MenuItem = MenuItem {
  route::Either ExternalHref String, 
  title::String, 
  systemIcon::Maybe String, 
  iconUrl :: Maybe String,
  newWindow :: Boolean
}

data Command = Init | AttemptRoute Route | NavAway Boolean
  | ToggleMenu | UserMenuAnchor (Maybe HTMLElement) | MenuClick Route | GoBack | CloseError

type Counts = {
  tasks :: Int, 
  notifications :: Int
}

type UserData = {
  id::String, 
  guest::Boolean, 
  autoLoggedIn::Boolean, 
  prefsEditable::Boolean,
  counts :: Maybe Counts,
  menuGroups :: Array (Array MenuItem)
}

type RenderData = {
  baseResources::String, 
  newUI::Boolean
}

foreign import preventUnload :: EventListener

foreign import renderData :: RenderData

foreign import setDocumentTitle :: String -> Effect Unit 

foreign import logoPath :: String

newtype TemplateRef = TemplateRef (ReactThis {|TemplateProps} State)

type TemplateProps = (
  title::String, 
  fixedViewPort :: Nullable Boolean, -- Fix the height of the main content, otherwise use min-height
  preventNavigation :: Nullable Boolean, -- Prevent navigation away from this page (e.g. Unsaved data) 
  titleExtra :: Nullable ReactElement, -- Extra part of the App bar (e.g. Search control)
  menuExtra :: Nullable (Array ReactElement), -- Extra menu options
  tabs :: Nullable ReactElement, -- Additional markup for displaying tabs which integrate with the App bar
  footer :: Nullable ReactElement, -- Markup to show at the bottom of the main area. E.g. save/cancel options
  backRoute :: Nullable Route, -- An optional Route for showing a back icon button
  menuMode :: String,
  fullscreenMode :: String,
  hideAppBar :: Boolean, 
  disableNotifications :: Boolean,
  innerRef :: Nullable (EffectFn1 (Nullable TemplateRef) Unit),
  errorResponse :: Nullable ErrorResponse
)

type State = {
  mobileOpen::Boolean, 
  menuAnchor::Maybe HTMLElement, 
  user :: Maybe UserData,
  attempt :: Maybe Route,
  errorOpen :: Boolean
}

initialState :: State
initialState = {
  mobileOpen: false, 
  menuAnchor: Nothing, 
  user: Nothing, 
  attempt: Nothing, 
  errorOpen: false
}

template :: String -> Array ReactElement -> ReactElement
template title = template' $ templateDefaults title

template' :: {|TemplateProps} -> Array ReactElement -> ReactElement
template' = createElement templateClass

templateDefaults ::  String ->  {|TemplateProps} 
templateDefaults title = {
    title,
    titleExtra:nullAny, 
    fixedViewPort:nullAny,
    preventNavigation:nullAny, 
    menuExtra:nullAny, 
    disableNotifications: false,
    tabs:nullAny, 
    backRoute: nullAny, 
    footer: nullAny, 
    menuMode:"", 
    fullscreenMode:"", 
    hideAppBar: false, 
    innerRef:nullAny, 
    errorResponse: nullAny
}

loadNewUser :: forall p. ReaderT (ReactThis p State) Aff Unit
loadNewUser = do 
  mr <- lift $ get Resp.json $ baseUrl <> "api/content/currentuser"
  either (lift <<< log) (\ud -> modifyState _ {user = Just ud})  (decodeUserData mr.response)

refreshUser :: TemplateRef -> Effect Unit 
refreshUser (TemplateRef r) = affAction r loadNewUser

templateClass :: ReactClass {children::Children|TemplateProps}
templateClass = withStyles ourStyles $ R.component "Template" $ \this -> do
  let
    d = eval >>> affAction this
    boolNull = fromMaybe false <<< toMaybe

    strings = prepLangStrings rawStrings
    coreString = prepLangStrings coreStrings
    _counts = prop (SProxy :: SProxy "counts")
    _tasks = prop (SProxy :: SProxy "tasks")
    _guest = prop (SProxy :: SProxy "guest")
    _notifications = prop (SProxy :: SProxy "notifications")
    _prefsEditable = prop (SProxy :: SProxy "prefsEditable")
    _menuGroups = prop (SProxy :: SProxy "menuGroups")

    setUnloadListener :: Boolean -> Effect Unit
    setUnloadListener add = do 
      w <- window
      (if add then addEventListener else removeEventListener) beforeunload preventUnload false $ toEventTarget w 

    setPreventUnload add = do
      setPreventNav (mkEffectFn1 \r -> do 
        if add then affAction this $ eval (AttemptRoute r) else pure unit
        pure add
      )
      setUnloadListener add

    render {state: state@{mobileOpen,menuAnchor,user,attempt}, props:props@{fixedViewPort:fvp, classes, 
                title:titleText,titleExtra,menuExtra,backRoute}} = rootTag classes.root $ [
        layout, 
        dialog {open: isJust attempt} [
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
                            }
      ]
      where
      children = childrenToArray props.children
      tabsM = toMaybe props.tabs
      fixedViewPort = fromMaybe false $ toMaybe fvp 

      contentClass = if fixedViewPort then classes.contentFixedHeight else classes.contentMinHeight
      content = D.main [ DP.className $ joinWith " " $ [classes.content, contentClass] ] $  
        catMaybes [
          Just $ D.div [DP.className classes.toolbar] [],
          tabsM $> D.div [DP.className classes.tabs] [],
          Just $ D.div [DP.className classes.contentArea] children
        ]
      
      useFullscreen = 
        props.hideAppBar || case props.fullscreenMode of 
          "YES" -> true
          "YES_WITH_TOOLBAR" -> true
          _ -> false

      menuParts = if hasMenu then [
                    hidden {mdUp: true} [
                        drawer {variant: temporary, anchor: left, classes: {paper: classes.drawerPaper},
                                  open: mobileOpen, onClose: d ToggleMenu } menuContent ],
                    hidden {smDown: true, implementation: css} [
                      drawer {variant: permanent, anchor: left, open: true, classes: {paper: classes.drawerPaper} } menuContent
                    ]
                  ] else []
      layout = if useFullscreen 
        then D.main' children 
        else D.div [DP.className classes.appFrame] $ [topBar] <> menuParts <> [content] <> catMaybes [
          toMaybe props.footer <#> \fc -> footer [DP.className classes.footer] [ 
            fc
          ]
        ]
      hasMenu = case props.menuMode of 
        "HIDDEN" -> false 
        _ -> true
      topBar = appBar {className: classes.appBar} $ catMaybes [
        Just $ toolbar {disableGutters: true} $ concat [
          guard hasMenu $> iconButton {
              color: inherit, className: classes.navIconHide,
              onClick: d ToggleMenu } [ icon_ [D.text "menu" ] 
          ], [
            D.div [DP.className classes.titleArea] $ catMaybes [
              toMaybe backRoute $> iconButton { onClick: d GoBack} [ icon_ [D.text "arrow_back" ] ],
              Just $ typography {variant: headline, color: inherit, className: classes.title} [ D.text titleText ],
              toMaybe titleExtra
            ],
            userMenu 
          ]
        ], 
        tabsM
      ]
      topBarString = coreString.topbar.link
      linkItem clickable t = menuItem { component: "a", 
                              href:  routeURI clickable,
                              onClick: d $ MenuClick clickable} [ D.text t ]
      userMaybe :: forall a. Lens' UserData a -> Maybe a
      userMaybe l = user ^? (_Just <<< l)
      userMenu = D.div [DP.className classes.userMenu ] $ (fromMaybe [] $ toMaybe menuExtra) <>
        (
          (guard $ not props.disableNotifications && (not $ fromMaybe true $ userMaybe _guest)) *>
          [
            hidden {mdDown:true} [
              badgedLink "assignment" _tasks "access/tasklist.do" topBarString.tasks , 
              badgedLink "notifications" _notifications "access/notifications.do" topBarString.notifications
            ],
            tooltip {title: strings.menu.title} $ 
              iconButton {"aria-label": strings.menu.title,
                          onClick: withCurrentTarget $ d <<< UserMenuAnchor <<< Just} 
                [ icon_ [ D.text "account_circle"] ],
            menu {
                anchorEl: toNullable menuAnchor,
                open: isJust menuAnchor,
                onClose: d $ UserMenuAnchor Nothing,
                anchorOrigin: { vertical: "top", horizontal: "right" },
                transformOrigin: { vertical: "top", horizontal: "right" }
             } $ catMaybes
              [ Just $ linkItem logoutRoute strings.menu.logout,
                (guard $ fromMaybe false $ userMaybe _prefsEditable) $> 
                      linkItem userPrefsRoute strings.menu.prefs
              ]
          ])
      badgedLink iconName count uri tip = 
        let iconOnly = icon_ [ D.text iconName ] 
            buttonLink :: ReactElement -> ReactElement
            buttonLink linkContent = iconButton {"aria-label": tip, href: uri} [ linkContent ]
        in tooltip {title: tip}
          case fromMaybe 0 $ preview (_Just <<< _counts <<< _Just <<< count) user of
              0 -> buttonLink iconOnly
              c -> buttonLink $ badge {badgeContent: c, color: secondary} [iconOnly]
        
      menuContent = [D.div [DP.className classes.logo] [ D.img [ DP.role "presentation", DP.src logoSrc] ]] <>
                    intercalate [divider_ []] (map group $ fromMaybe [] $ userMaybe _menuGroups)
        where 
          logoSrc = logoPath
          group items = [list {component: "nav"} (navItem <$> items)]
          navItem (MenuItem {title,systemIcon,route,iconUrl,newWindow}) = linkProps
            [
              listItemIcon_ $ case iconUrl of 
                Just url -> img [src url]
                Nothing -> icon {color: inherit} [ D.text $ fromMaybe "folder" $ systemIcon ],
              listItemText' {disableTypography: true, primary: typography {variant: subheading, component: "div"} [text title] }
            ]
            where 
              linkProps = case route of 
                Right r | Just m <- routeHref <$> matchRoute r -> RMUI.listItem { component: "a", href: m.href, onClick: m.onClick }
                Left (ExternalHref href) -> RMUI.listItem { component: "a", href, target: if newWindow then "_blank" else "" }
                Right r -> RMUI.listItem {component: "a", href: show r}

    htmlElement :: Effect HTMLElement
    htmlElement = unsafePartial $ fromJust <$> bindFlipped HTML.fromElement 
              <$> (window >>= document >>= toDocument >>> documentElement)
    setWindowTitle title = setDocumentTitle (title <> coreString.windowtitlepostfix)
    fullscreenClass = case _ of 
      "YES" -> Just "fullscreen"
      "YES_WITH_TOOLBAR" -> Just "fullscreen-toolbar"
      _ -> Nothing

    setHtmlClasses oldMode newMode = do
      htmlClasses <- htmlElement >>= classList
      let addClass (Just clz) = DOMTokens.add htmlClasses clz
          addClass _ = pure unit
          remClass (Just clz) = DOMTokens.remove htmlClasses clz
          remClass _ = pure unit
      remClass (fullscreenClass oldMode)
      addClass (fullscreenClass newMode)

    maybeEff b e = if b then e else pure unit
    eval (CloseError) = do 
      modifyState _{errorOpen=false}
    eval (GoBack) = do 
      {backRoute} <- getProps
      liftEffect $ maybe (pure unit) pushRoute $ toMaybe backRoute  
    eval (MenuClick route) = do 
      modifyState _{menuAnchor = Nothing}
      liftEffect $ pushRoute route
    eval (NavAway n) = do 
      {attempt} <- getState
      liftEffect $ guard n *> attempt # maybe (pure unit) forcePushRoute
      modifyState _{attempt = Nothing}
    eval (AttemptRoute r) = do 
      modifyState _{attempt = Just r}
    eval Init = do 
      {title,preventNavigation:pn,fullscreenMode} <- getProps
      liftEffect $ do 
        setWindowTitle title
        setHtmlClasses "NO" fullscreenMode
        maybeEff (fromMaybe false $ toMaybe pn) $ setPreventUnload true
      loadNewUser

    eval ToggleMenu = modifyState \(s :: State) -> s {mobileOpen = not s.mobileOpen}
    eval (UserMenuAnchor el) = modifyState \(s :: State) -> s {menuAnchor = el}

  pure {
    render: renderer render this, 
    state:initialState, 
    componentDidMount: d Init, 
    componentDidUpdate: \oldProps@{fullscreenMode:oldfsm} _ _ -> do
      p@{preventNavigation, title, fullscreenMode} <- R.getProps this
      let isTrue = fromMaybe false <<< toMaybe
          newPN = isTrue preventNavigation
      maybeEff (isTrue oldProps.preventNavigation /= newPN) $ setPreventUnload newPN
      maybeEff (oldProps.title /= title) $ setWindowTitle title
      maybeEff (oldfsm /= fullscreenMode) $ setHtmlClasses oldfsm fullscreenMode
      maybeEff (oldProps.errorResponse /= p.errorResponse) $ do 
        R.setState this {errorOpen: true}
    , componentWillUnmount: setUnloadListener false
  }
  where
    drawerWidth = 240
    tabHeight = 48 
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
          overflow: "hidden", 
          whiteSpace: "nowrap", 
          textOverflow: "ellipsis",
          marginLeft: theme.spacing.unit
        }
      ],
      appFrame: {
        position: "relative"
      },
      appBar: cssList [ 
        allQuery {
          position: "fixed",
          marginLeft: drawerWidth
        },
        desktop { 
          width: "calc(100% - " <> show drawerWidth <> "px)"
        }
      ],
      navIconHide: desktop { 
        display: "none" 
      },
      toolbar: theme.mixins.toolbar,
      drawerPaper: cssList [ 
        desktop {
          position: "fixed"
        },
        allQuery { 
          width: drawerWidth,
          zIndex: 1100
        }
      ],
      tabs: {
        height: tabHeight
      },
      contentMinHeight: { 
        minHeight: "100vh"
      },
      contentFixedHeight: {
        height: "100vh"
      },
      "@global": cssList [
        allQuery {
          a: {
            textDecoration: "none",
            color: theme.palette.primary.main
          }
        }
      ],
      content: cssList [
        allQuery {
          display: "flex",
          flexDirection: "column"
        },
        desktop {
          marginLeft: drawerWidth
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
      contentArea: {
        flexGrow: 1, 
        flexBasis: 0,
        minHeight: 0
      },
      userMenu: {
        flexShrink: 0
      }, 
      footer: cssList [
        allQuery {
          position: "fixed", 
          right: 0,
          bottom: 0, 
          zIndex: 1000,
          width: "100%"
        }, 
        desktop {
          width: "calc(100% - " <> show drawerWidth <> "px)"
        }
      ]
    }

rawStrings :: { prefix :: String
, strings :: { menu :: { title :: String
                       , logout :: String
                       , prefs :: String
                       }
             , navaway :: { title :: String
                          , content :: String
                          }
             }
}
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

coreStrings :: { prefix :: String
, strings :: { windowtitlepostfix :: String
             , topbar :: { link :: { notifications :: String
                                   , tasks :: String
                                   }
                         }
             }
}
coreStrings = {prefix: "com.equella.core",
  strings: {
    windowtitlepostfix: " | openEQUELLA",
    topbar: { 
      link: {
        notifications: "Notifications",
        tasks: "Tasks"
      }
    }
  }
}

instance decodeMI :: DecodeJson MenuItem where 
  decodeJson v = do 
    o <- decodeJson v
    href <- o .?? "href"
    route <- o .?? "route" <#> case _ of 
      Just r -> Right r 
      _ | Just h <- href -> Left $ ExternalHref h
      _ -> Right "home.do"
    title <- o .? "title"
    systemIcon <- o .?? "systemIcon"
    iconUrl <- o .?? "iconUrl"
    newWindow <- o .? "newWindow"
    pure $ MenuItem {title, route, systemIcon, iconUrl, newWindow}

decodeCounts :: Json -> Either String Counts
decodeCounts v = do 
  o <- decodeJson v
  tasks <- o .? "tasks"
  notifications <- o .? "notifications"
  pure {tasks, notifications}

decodeUserData :: Json -> Either String UserData
decodeUserData v = do 
  o <- decodeJson v
  id <- o .? "id"
  autoLoggedIn <- o .? "autoLoggedIn"
  guest <- o .? "guest"
  prefsEditable <- o .? "prefsEditable"
  menuGroups <- o .? "menuGroups"
  counts <- traverse decodeCounts $ Object.lookup "counts" o
  pure {id, autoLoggedIn, guest, prefsEditable, menuGroups, counts}
