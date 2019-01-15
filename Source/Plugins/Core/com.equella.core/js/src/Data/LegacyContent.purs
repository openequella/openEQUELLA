module OEQ.Data.LegacyContent where 

import Prelude

import Control.Alt ((<|>))
import Data.Argonaut (class DecodeJson, Json, decodeJson, (.?))
import Data.Foldable (foldMap)
import Data.List (List)
import Data.Map as Map
import Data.Newtype (wrap)
import Data.Nullable (Nullable)
import Data.Tuple (Tuple(..))
import Effect (Effect)
import Effect.Uncurried (EffectFn1)
import Foreign.Object (Object, isEmpty)
import Foreign.Object as Object
import OEQ.Utils.QueryString (queryStringObj)
import Routing.Match (Match)
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

remainingParts :: Match (List RoutePart)
remainingParts = wrap $ \r -> pure $ Tuple r r

legacyRoute :: Match LegacyURI
legacyRoute = foldMap toLegURI <$> remainingParts
  where 
    toLegURI (Path p) = LegacyURI p Object.empty
    toLegURI (Query qm) = LegacyURI "" $ pure <$> (Object.fromFoldable $ Map.toUnfoldable qm :: Array (Tuple String String))

legacyURIToString :: LegacyURI -> String
legacyURIToString (LegacyURI path params) =
    if isEmpty params then path 
    else path <> "?" <> queryStringObj params

type LegacyContentR = {
  html:: Object String, 
  state :: Object (Array String),
  css :: Array String,
  js :: Array String,
  script :: String, 
  title :: String, 
  fullscreenMode :: String, 
  menuMode :: String, 
  hideAppBar :: Boolean, 
  noForm :: Boolean,
  preventUnload :: Boolean
} 

data ContentResponse = Redirect String | ChangeRoute String Boolean | LegacyContent LegacyContentR Boolean | Callback (Effect Unit)

type SubmitOptions = {vals::Object (Array String), callback :: Nullable (EffectFn1 Json Unit)} 

instance decodeLC :: DecodeJson ContentResponse where 
  decodeJson v = do 
    o <- decodeJson v 
    let decodeChange = do 
          route <- o .? "route"
          userUpdated <- o .? "userUpdated"
          pure $ ChangeRoute route userUpdated
        decodeRedirect = do 
          href <- o .? "href"
          pure $ Redirect href
        decodeContent = do 
          html <- o .? "html"
          state <- o .? "state"
          css <- o .? "css"
          js <- o .? "js"
          script <- o .? "script"
          title <- o .? "title"
          fullscreenMode <- o .? "fullscreenMode"
          menuMode <- o .? "menuMode"
          hideAppBar <- o .? "hideAppBar"
          userUpdated <- o .? "userUpdated"
          noForm <- o .? "noForm"
          preventUnload <- o .? "preventUnload"
          pure $ LegacyContent {html, state, css, js, script, title, fullscreenMode, menuMode, 
            hideAppBar, preventUnload, noForm} userUpdated
    decodeChange <|> decodeRedirect <|> decodeContent

