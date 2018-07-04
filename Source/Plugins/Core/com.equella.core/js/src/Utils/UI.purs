module Utils.UI where 

import Prelude

import Data.Int (floor)
import Effect (Effect)
import MaterialUI.PropTypes (EventHandler, toHandler)
import MaterialUI.Properties (IProp, onChange)
import React.SyntheticEvent (NativeEventTarget, SyntheticEvent, SyntheticEvent_, SyntheticKeyboardEvent, currentTarget, keyCode, target)
import Unsafe.Coerce (unsafeCoerce)
import Web.HTML (HTMLElement)

valueChange :: forall v r. (v -> Effect Unit) -> SyntheticEvent_ (target :: NativeEventTarget|r) -> Effect Unit 
valueChange f = target >=> \t -> f $ (unsafeCoerce t).value

textChange :: forall r c. (c -> Effect Unit) -> (String -> c) -> IProp (onChange :: EventHandler SyntheticEvent|r)
textChange d f = onChange $ valueChange $ f >>> d

enterSubmit :: Effect Unit -> EventHandler SyntheticKeyboardEvent
enterSubmit s = toHandler \e -> keyCode e >>= \k -> case floor k of 
    13 -> s 
    _ -> pure unit 

withCurrentTarget :: (HTMLElement -> Effect Unit) -> SyntheticEvent -> Effect Unit 
withCurrentTarget f e = currentTarget e >>= \t -> f $ unsafeCoerce t
