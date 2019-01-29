module OEQ.UI.MessageInfo where 

import Prelude

import Data.Nullable (Nullable)
import Data.TSCompat (OneOf, OptionRecord, StringConst)
import Data.TSCompat.Class (class IsTSEq)
import Effect (Effect)
import React (ReactClass, ReactElement, unsafeCreateLeafElement)

foreign import messageInfoClass :: forall a. ReactClass a

type MessageInfoProps = (
    open :: Boolean,
    onClose :: Effect Unit,
    title :: String,
    variant :: OneOf (
        typed :: StringConst "success", 
        typed :: StringConst "warning", 
        typed :: StringConst "error", 
        typed :: StringConst "info"
    )
)

messageInfo :: forall a. IsTSEq a (OptionRecord MessageInfoProps MessageInfoProps) => a -> ReactElement
messageInfo = unsafeCreateLeafElement messageInfoClass