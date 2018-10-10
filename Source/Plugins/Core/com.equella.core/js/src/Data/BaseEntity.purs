module OEQ.Data.BaseEntity where 

import Prelude

import Data.Argonaut (class DecodeJson, Json, decodeJson, getField, (.?))
import Data.Either (Either)
import OEQ.Data.Security (TargetListEntry)


type BaseEntityShort r = {
  uuid :: String,
  name :: String
  | r
}

type BaseEntityLabel = BaseEntityShort ()

newtype BaseEntity = BaseEntity {security :: { rules :: Array TargetListEntry }}

instance decBase :: DecodeJson BaseEntity where 
  decodeJson v = do 
    o <- decodeJson v 
    rules <- (o .? "security") >>= flip getField "rules"
    pure $ BaseEntity {security:{rules}}


decodeBaseEntityLabel :: Json -> Either String BaseEntityLabel
decodeBaseEntityLabel v = do 
  o <- decodeJson v 
  uuid <- o .? "uuid"
  name <- o .? "name"
  pure {name,uuid}