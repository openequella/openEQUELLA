module Routes where 

import Prelude

import Control.Alt ((<|>))
import Control.Monad.Eff (Eff)
import Control.Monad.Eff.Ref (REF, Ref, newRef, readRef, writeRef)
import Control.Monad.Eff.Unsafe (unsafePerformEff)
import Control.Monad.IOEffFn (IOFn1, mkIOFn1, runIOFn1)
import DOM (DOM)
import DOM.HTML.Types (HISTORY)
import Data.Either (either)
import Data.Foreign (toForeign)
import Data.Maybe (Maybe(Just, Nothing))
import MaterialUI.Event (Event)
import React (preventDefault)
import Routing (match)
import Routing.Match (Match)
import Routing.Match.Class (lit, str)
import Routing.PushState (PushStateInterface, PushStateEffects, makeInterface)
import Unsafe.Coerce (unsafeCoerce)

data Route = SearchPage | 
    SettingsPage | 
    CoursesPage | 
    CourseEdit String |
    SchemasPage |
    SchemaEdit String

navGlobals :: forall eff. {nav::PushStateInterface (PushStateEffects eff), preventNav :: Ref (IOFn1 Route Boolean)}
navGlobals = unsafePerformEff do 
    nav <- makeInterface 
    preventNav <- newRef emptyPreventNav
    pure {nav, preventNav}

nav :: forall eff. PushStateInterface (PushStateEffects eff)
nav = navGlobals.nav

emptyPreventNav :: IOFn1 Route Boolean
emptyPreventNav = mkIOFn1 $ const $ pure false

homeSlash :: Match Unit
homeSlash = lit ""

routeMatch :: Match Route
routeMatch = 
    SearchPage <$ (lit "search") <|>
    SettingsPage <$ (lit "settings") <|>
    CourseEdit <$> (lit "course" *> str <* lit "edit") <|>
    SchemaEdit <$> (lit "schema" *> str <* lit "edit") <|>
    CoursesPage <$ (lit "course") <|>
    SchemasPage <$ (lit "schema")

matchRoute :: String -> Maybe Route 
matchRoute = match routeMatch >>> (either (const Nothing) Just)

setPreventNav :: forall eff. IOFn1 Route Boolean -> Eff (ref::REF|eff) Unit
setPreventNav preventNav = writeRef navGlobals.preventNav preventNav

forcePushRoute :: forall eff. Route -> Eff ( ref :: REF, dom :: DOM, history :: HISTORY | eff) Unit
forcePushRoute r = do 
    let href = append "page" $ routeHash r
    writeRef navGlobals.preventNav emptyPreventNav
    nav.pushState (toForeign {}) href

pushRoute :: forall eff. Route -> Eff ( ref :: REF, dom :: DOM, history :: HISTORY | eff) Unit
pushRoute r = do 
    preventNav <- readRef navGlobals.preventNav >>= flip runIOFn1 r
    if preventNav then pure unit else forcePushRoute r

routeHref :: forall eff. Route -> {href::String, onClick :: IOFn1 Event Unit}
routeHref r = 
    let href = append "page" $ routeHash r
        onClick = mkIOFn1 $ \e -> preventDefault (unsafeCoerce e) *> pushRoute r
    in { href, onClick }

routeHash :: Route -> String
routeHash r = "/" <> ( case r of 
    SearchPage -> "search"
    SettingsPage -> "settings"
    CoursesPage -> "course"
    CourseEdit cid -> "course/" <> cid <> "/edit"
    SchemasPage -> "schema"
    SchemaEdit cid -> "schema/" <> cid <> "/edit"
  )
