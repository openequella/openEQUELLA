module LegacyPage where

import Prelude

import Control.Monad.Eff (Eff)
import Data.Maybe (Maybe(..))
import Data.Nullable (Nullable)
import Data.StrMap (StrMap)
import React (ReactElement, Ref)
import React.DOM as D
import React.DOM.Props as DP
import Template (renderData, template)

foreign import setBodyHtml :: forall eff. StrMap String -> Nullable Ref -> Eff eff Unit

legacy :: StrMap String -> ReactElement
legacy html = template {title:renderData.title, mainContent: D.div [ DP.withRef $ setBodyHtml html ] [], titleExtra: Nothing}
