module Routes where 

import Prelude

import Control.Alt ((<|>))
import Data.Either (either)
import Data.List (List, foldMap)
import Data.Map as Map
import Data.Maybe (Maybe(Just, Nothing))
import Data.Newtype (wrap)
import Data.Tuple (Tuple(..))
import Effect (Effect)
import Effect.Ref (Ref, new, read, write)
import Effect.Uncurried (EffectFn1, mkEffectFn1, runEffectFn1)
import Effect.Unsafe (unsafePerformEffect)
import Foreign (unsafeToForeign)
import Foreign.Object (Object, isEmpty)
import Foreign.Object as Object
import QueryString (queryStringObj)
import React.SyntheticEvent (SyntheticEvent, preventDefault)
import Routing (match)
import Routing.Match (Match, lit, str)
import Routing.PushState (PushStateInterface, makeInterface)
import Routing.Types (RoutePart(..))

data LegacyURI = LegacyURI String (Object (Array String))
instance legacySG :: Semigroup LegacyURI where 
  append (LegacyURI path1 qp) (LegacyURI path2 qp2) = LegacyURI (case {path1,path2} of 
    {path1:""} -> path2 
    {path2:""} -> path1 
    _ -> path1 <> "/" <> path2) (qp <> qp2)
instance legacyMonoid :: Monoid LegacyURI where 
  mempty = LegacyURI "" Object.empty
derive instance eqLURI :: Eq LegacyURI 

data Route = 
    LegacyPage LegacyURI |
    SearchPage | 
    SettingsPage | 
    CoursesPage | 
    CourseEdit String |
    NewCourse

navGlobals :: {nav::PushStateInterface, preventNav :: Ref (EffectFn1 Route Boolean)}
navGlobals = unsafePerformEffect do 
    nav <- makeInterface 
    preventNav <- new emptyPreventNav
    pure {nav, preventNav}

globalNav :: PushStateInterface
globalNav = navGlobals.nav

emptyPreventNav :: EffectFn1 Route Boolean
emptyPreventNav = mkEffectFn1 $ const $ pure false

homeSlash :: Match Unit
homeSlash = lit ""

remainingParts :: Match (List RoutePart)
remainingParts = wrap $ \r -> pure $ Tuple r r

legacyRoute :: Match LegacyURI
legacyRoute = foldMap toLegURI <$> remainingParts
  where 
    toLegURI (Path p) = LegacyURI p Object.empty
    toLegURI (Query qm) = LegacyURI "" $ pure <$> (Object.fromFoldable $ Map.toUnfoldable qm :: Array (Tuple String String))

routeMatch :: Match Route
routeMatch = lit "page" *>
    (SearchPage <$ (lit "search") <|>
    SettingsPage <$ (lit "settings") <|>
    NewCourse <$ (lit "course" *> lit "new") <|>
    CourseEdit <$> (lit "course" *> str <* lit "edit") <|>
    CoursesPage <$ (lit "course")) <|> (LegacyPage <$> legacyRoute)

matchRoute :: String -> Maybe Route 
matchRoute = match routeMatch >>> (either (const Nothing) Just)

setPreventNav :: EffectFn1 Route Boolean -> Effect Unit
setPreventNav preventNav = write preventNav navGlobals.preventNav

forcePushRoute :: Route -> Effect Unit
forcePushRoute r = do 
    let href = routeURI r
    write emptyPreventNav navGlobals.preventNav
    globalNav.pushState (unsafeToForeign {}) href

pushRoute :: Route -> Effect Unit
pushRoute r = do 
    preventNav <- read navGlobals.preventNav >>= flip runEffectFn1 r
    if preventNav then pure unit else forcePushRoute r

routeHref :: Route -> {href::String, onClick :: EffectFn1 SyntheticEvent Unit}
routeHref r = 
    let href = routeURI r
        onClick = mkEffectFn1 $ \e -> preventDefault e *> pushRoute r
    in { href, onClick }

routeURI :: Route -> String
routeURI r = (case r of 
    SearchPage -> "page/search"
    SettingsPage -> "page/settings"
    CoursesPage -> "page/course"
    NewCourse -> "page/course/new"
    CourseEdit cid -> "page/course/" <> cid <> "/edit"
    LegacyPage (LegacyURI path o) | isEmpty o -> path
    LegacyPage (LegacyURI path params) -> path <> "?" <> queryStringObj params
  )
