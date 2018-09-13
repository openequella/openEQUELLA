module OEQ.Data.BaseEntity where 

import Prelude

import Data.Argonaut (class DecodeJson, decodeJson, getField, (.?))
import OEQ.Data.Security (TargetListEntry)

newtype BaseEntity = BaseEntity {security :: { rules :: Array TargetListEntry }}

instance decBase :: DecodeJson BaseEntity where 
  decodeJson v = do 
    o <- decodeJson v 
    rules <- (o .? "security") >>= flip getField "rules"
    pure $ BaseEntity {security:{rules}}