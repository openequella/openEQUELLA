module OEQ.MainUI.Routes where 

import Prelude

import Control.Alt ((<|>))
import Data.Either (either)
import Data.Maybe (Maybe(Just, Nothing))
import Effect (Effect)
import Effect.Ref (Ref, new, read, write)
import Effect.Uncurried (EffectFn1, mkEffectFn1, runEffectFn1)
import Effect.Unsafe (unsafePerformEffect)
import Foreign (unsafeToForeign)
import Foreign.Object as Object
import OEQ.Data.Item (ItemRef(..))
import OEQ.Data.LegacyContent (LegacyURI(..), legacyRoute, legacyURIToString)
import OEQ.UI.Common (ClickableHref)
import React.SyntheticEvent (SyntheticEvent_, preventDefault, stopPropagation)
import Routing (match)
import Routing.Match (Match, end, int, lit, str)
import Routing.PushState (PushStateInterface, makeInterface)

data Route = 
    LegacyPage LegacyURI |
    SearchPage | 
    SettingsPage | 
    CoursesPage | 
    CourseEdit String |
    ViewItemPage ItemRef |
    SearchConfigsPage |
    ThemePage |
    NewCourse |
    LoginNoticeConfigPage

navGlobals :: forall route. {nav::PushStateInterface, preventNav :: Ref (EffectFn1 route Boolean)}
navGlobals = unsafePerformEffect do 
    nav <- makeInterface  
    preventNav <- new emptyPreventNav
    pure {nav, preventNav}

globalNav :: PushStateInterface
globalNav = navGlobals.nav

emptyPreventNav :: forall route. EffectFn1 route Boolean
emptyPreventNav = mkEffectFn1 $ const $ pure false

homeRoute :: Route 
homeRoute = LegacyPage (LegacyURI "home.do" Object.empty)

routeMatch :: Match Route
routeMatch = 
    ViewItemPage <$> (ItemRef <$> (lit "integ" *> lit "gen" *> str) <*> int) <|>
    SettingsPage <$ (lit "access" *> lit "settings.do") <|>
    homeRoute <$ end <|> 
    lit "page" *>
        (SearchPage <$ (lit "search") <|>
        SettingsPage <$ (lit "settings") <|>
        NewCourse <$ (lit "course" *> lit "new") <|>
        CourseEdit <$> (lit "course" *> str <* lit "edit") <|>
        CoursesPage <$ (lit "course") <|>
        ThemePage <$ (lit "themeconfiguration") <|>
        LoginNoticeConfigPage <$ (lit "loginconfiguration") <|>
        SearchConfigsPage <$ (lit "searchconfigs"))
        <|> (LegacyPage <$> legacyRoute) 


matchRoute :: String -> Maybe Route 
matchRoute = match routeMatch >>> (either (const Nothing) Just)

setPreventNav :: forall route. EffectFn1 route Boolean -> Effect Unit
setPreventNav preventNav = write preventNav navGlobals.preventNav

forcePushRoute :: Route -> Effect Unit
forcePushRoute = forcePushRoute' <<< routeURI

forcePushRoute' :: String -> Effect Unit
forcePushRoute' href = do 
    write emptyPreventNav navGlobals.preventNav
    globalNav.pushState (unsafeToForeign {}) href

pushRoute :: Route -> Effect Unit
pushRoute = pushRoute' routeURI

pushRoute' :: forall route. (route -> String) -> route -> Effect Unit
pushRoute' f r = do 
    preventNav <- read navGlobals.preventNav >>= flip runEffectFn1 r
    if preventNav then pure unit else forcePushRoute' $ f r

routeHref :: Route -> ClickableHref
routeHref r = 
    let href = routeURI r
        onClick :: forall e. EffectFn1 (SyntheticEvent_ e) Unit
        onClick = mkEffectFn1 $ \e -> do 
            preventDefault e
            stopPropagation e 
            pushRoute r
    in { href, onClick }

routeURI :: Route -> String
routeURI r = (case r of 
    SearchPage -> "page/search"
    SettingsPage -> "page/settings"
    CoursesPage -> "page/course"
    NewCourse -> "page/course/new"
    ThemePage -> "page/themeconfiguration"
    LoginNoticeConfigPage -> "page/loginconfiguration"
    CourseEdit cid -> "page/course/" <> cid <> "/edit"
    ViewItemPage (ItemRef uuid version) -> "integ/gen/" <> uuid <> "/" <> show version
    SearchConfigsPage -> "page/searchconfigs"
    LegacyPage leg -> legacyURIToString leg
  )

logoutRoute :: Route 
logoutRoute = LegacyPage (LegacyURI "logon.do" $ Object.singleton "logout" ["true"])

userPrefsRoute :: Route
userPrefsRoute = LegacyPage (LegacyURI "access/user.do" Object.empty)

oldViewItemRoute :: String -> Int -> Route 
oldViewItemRoute uuid version = LegacyPage (LegacyURI ("items/" <> uuid <> "/" <> show version <> "/" ) Object.empty)
