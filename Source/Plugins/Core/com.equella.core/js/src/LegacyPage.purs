module LegacyPage where

import Prelude

import Control.Monad.Eff (Eff)
import Control.Monad.Eff.Console (CONSOLE)
import DOM (DOM)
import Data.Maybe (Maybe(..))
import Data.Nullable (Nullable)
import React (Ref)
import React.DOM as D
import React.DOM.Props as DP
import Template (renderMain, template)

foreign import setBodyHtml :: forall eff. Nullable Ref -> Eff eff Unit

main :: forall eff. Eff ( dom :: DOM, console :: CONSOLE | eff) Unit
main = renderMain (template {mainContent: D.div [ DP.withRef setBodyHtml ] [], titleExtra: Nothing})
