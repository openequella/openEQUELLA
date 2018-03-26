module Routes where 

import Prelude

import Control.Alt ((<|>))
import Control.Monad.Eff.Unsafe (unsafePerformEff)
import Control.Monad.IOEffFn (IOFn1, mkIOFn1)
import Data.Either (either)
import Data.Foreign (toForeign)
import Data.Maybe (Maybe(..))
import Debug.Trace (spy)
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
    CourseEdit String

nav :: forall eff. PushStateInterface (PushStateEffects eff)
nav = unsafePerformEff makeInterface

homeSlash :: Match Unit
homeSlash = lit ""

routeMatch :: Match Route
routeMatch = 
    SearchPage <$ (lit "search") <|>
    SettingsPage <$ (lit "settings") <|>
    CourseEdit <$> (lit "course" *> str <* lit "edit") <|>
    CoursesPage <$ (lit "courses")

matchRoute :: String -> Maybe Route 
matchRoute = match routeMatch >>> (either (const Nothing) Just)

routeHref :: forall eff. Route -> {href::String, onClick :: IOFn1 Event Unit}
routeHref r = 
    let href = append "page" $ routeHash r
        onClick = mkIOFn1 $ \e -> preventDefault (unsafeCoerce e) *> 
            nav.pushState (toForeign {}) href
    in { href, onClick }

routeHash :: Route -> String
routeHash r = "/" <> ( case r of 
    SearchPage -> "search"
    SettingsPage -> "settings"
    CoursesPage -> "courses"
    CourseEdit cid -> "course/" <> cid <> "/edit"
  )
