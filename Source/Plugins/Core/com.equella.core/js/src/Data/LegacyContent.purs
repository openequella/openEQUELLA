module OEQ.Data.LegacyContent where 

import Prelude

import Control.Alt ((<|>))
import Data.Argonaut (class DecodeJson, Json, decodeJson, (.?))
import Data.Nullable (Nullable)
import Effect (Effect)
import Effect.Uncurried (EffectFn1)
import Foreign.Object (Object)

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

