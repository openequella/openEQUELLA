module OEQ.Data.Settings where 

import Prelude

import Data.Argonaut (class DecodeJson, Json, decodeJson, jsonEmptyObject, (.?), (.??), (:=), (~>))
import Data.Either (Either)
import Data.Maybe (Maybe)

newtype Setting = Setting {
  id :: String,
  group :: String,
  name :: String,
  description :: String,
  pageUrl :: Maybe String
}

instance decodeSetting :: DecodeJson Setting where
  decodeJson v = do
    o <- decodeJson v
    id <- o .? "id"
    name <- o .? "name"
    group <- o .? "group"
    description <- o .? "description"
    links <- o .? "links"
    pageUrl <- links .?? "web"
    pure $ Setting {id,group,name,description,pageUrl}

type NewUISettings = { enabled :: Boolean, newSearch :: Boolean }
type UISettings = { newUI :: NewUISettings }


decodeNewUISettings :: Json -> Either String NewUISettings
decodeNewUISettings v = do
  o <- decodeJson v
  enabled <- o .? "enabled"
  newSearch <- o .? "newSearch"
  pure $ {enabled,newSearch}

decodeUISettings :: Json -> Either String UISettings
decodeUISettings v = do
  o <- decodeJson v
  newUI <- o .? "newUI" >>= decodeNewUISettings
  pure $ {newUI}

encodeNewUISettings :: NewUISettings -> Json
encodeNewUISettings {enabled,newSearch} =
    "enabled" := enabled ~>
    "newSearch" := newSearch ~>
    jsonEmptyObject

encodeUISettings :: UISettings -> Json
encodeUISettings {newUI} = "newUI" := encodeNewUISettings newUI ~> jsonEmptyObject
