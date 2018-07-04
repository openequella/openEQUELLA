module Routes where 

import Prelude

import Control.Alt ((<|>))
import Data.Either (either)
import Data.Maybe (Maybe(Just, Nothing))
import Effect (Effect)
import Effect.Ref (Ref, new, read, write)
import Effect.Uncurried (EffectFn1, mkEffectFn1, runEffectFn1)
import Effect.Unsafe (unsafePerformEffect)
import Foreign (unsafeToForeign)
import React.SyntheticEvent (SyntheticMouseEvent, SyntheticEvent, preventDefault)
import Routing (match)
import Routing.Match (Match, lit, str)
import Routing.PushState (PushStateInterface, makeInterface)
import Unsafe.Coerce (unsafeCoerce)

data Route = SearchPage | 
    SettingsPage | 
    CoursesPage | 
    CourseEdit String |
    NewCourse

navGlobals :: {nav::PushStateInterface, preventNav :: Ref (EffectFn1 Route Boolean)}
navGlobals = unsafePerformEffect do 
    nav <- makeInterface 
    preventNav <- new emptyPreventNav
    pure {nav, preventNav}

nav :: PushStateInterface
nav = navGlobals.nav

emptyPreventNav :: EffectFn1 Route Boolean
emptyPreventNav = mkEffectFn1 $ const $ pure false

homeSlash :: Match Unit
homeSlash = lit ""

routeMatch :: Match Route
routeMatch = 
    SearchPage <$ (lit "search") <|>
    SettingsPage <$ (lit "settings") <|>
    NewCourse <$ (lit "course" *> lit "new") <|>
    CourseEdit <$> (lit "course" *> str <* lit "edit") <|>
    CoursesPage <$ (lit "course")

matchRoute :: String -> Maybe Route 
matchRoute = match routeMatch >>> (either (const Nothing) Just)

setPreventNav :: EffectFn1 Route Boolean -> Effect Unit
setPreventNav preventNav = write preventNav navGlobals.preventNav

forcePushRoute :: Route -> Effect Unit
forcePushRoute r = do 
    let href = append "page" $ routeURI r
    write emptyPreventNav navGlobals.preventNav
    nav.pushState (unsafeToForeign {}) href

pushRoute :: Route -> Effect Unit
pushRoute r = do 
    preventNav <- read navGlobals.preventNav >>= flip runEffectFn1 r
    if preventNav then pure unit else forcePushRoute r

routeHref :: Route -> {href::String, onClick :: EffectFn1 SyntheticEvent Unit}
routeHref r = 
    let href = append "page" $ routeURI r
        onClick = mkEffectFn1 $ \e -> preventDefault e *> pushRoute r
    in { href, onClick }

routeURI :: Route -> String
routeURI r = "/" <> ( case r of 
    SearchPage -> "search"
    SettingsPage -> "settings"
    CoursesPage -> "course"
    NewCourse -> "course/new"
    CourseEdit cid -> "course/" <> cid <> "/edit"
  )
