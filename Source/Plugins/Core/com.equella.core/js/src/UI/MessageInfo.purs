module OEQ.UI.MessageInfo where 

import Prelude

import Data.Nullable (Nullable)
import Effect (Effect)
import MaterialUI.Properties (Enum)
import React (ReactClass, ReactElement, unsafeCreateLeafElement)

foreign import messageInfoClass :: forall a. ReactClass a

messageInfo :: {
    open :: Boolean,
    onClose :: Effect Unit,
    title :: String,
    code :: Nullable Int,
    variant :: Enum (success ::String, warning :: String, error::String, info::String)
} -> ReactElement
messageInfo = unsafeCreateLeafElement messageInfoClass