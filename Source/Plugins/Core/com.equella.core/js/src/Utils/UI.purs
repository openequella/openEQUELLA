module Utils.UI where 

import Prelude

import Control.Monad.Eff (Eff)
import MaterialUI.Event (Event)
import MaterialUI.PropTypes (EventHandler)
import MaterialUI.Properties (IProp, onChange)

textChange :: forall r c e. ((Event -> c) -> Event -> Eff e Unit) -> (String -> c) -> IProp (onChange :: EventHandler Event|r)
textChange d f = onChange $ d (\e -> f e.target.value)