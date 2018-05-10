module Utils.UI where 

import Prelude

import Control.Monad.Eff (Eff)
import MaterialUI.Event (Event)
import MaterialUI.PropTypes (EventHandler, toHandler)
import MaterialUI.Properties (IProp, onChange)

textChange :: forall r c e. ((Event -> c) -> Event -> Eff e Unit) -> (String -> c) -> IProp (onChange :: EventHandler Event|r)
textChange d f = onChange $ d (\e -> f e.target.value)

enterSubmit :: forall e. Eff e Unit -> EventHandler Event
enterSubmit s = toHandler \e -> if e.keyCode == 13 then s else pure unit 

-- enterSubmit :: forall r c e. ((Unit -> c) -> Unit -> Eff e Unit) -> c -> IProp (onKeyDown :: EventHandler Event|r)
-- enterSubmit d c = onKeyDown $ (\e -> if e.target.keyCode == 13 then (d \_ -> c) unit else pure unit)