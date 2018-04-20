module Utils.UI where 

import Prelude

import Control.Monad.Eff (Eff)
import MaterialUI.ExpansionPanel (onChange)
import MaterialUI.PropTypes (Untyped, handle)
import MaterialUI.Properties (IProp)

textChange :: forall a r e ev. (({target::{value::String}} -> a) -> {target::{value::String}} -> Eff e Unit) -> (String -> a) -> IProp (onChange :: Untyped |r )
textChange d f = onChange $ handle $ d (\e -> f e.target.value)