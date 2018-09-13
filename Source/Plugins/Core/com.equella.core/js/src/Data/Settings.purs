module OEQ.Data.Settings where 

import Prelude

import Data.Argonaut (class DecodeJson, class EncodeJson, decodeJson, jsonEmptyObject, (.?), (.??), (:=), (~>))
import Data.Maybe (Maybe)
import Data.Newtype (class Newtype)

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

newtype FacetSetting = FacetSetting { name :: String, path :: String }
newtype NewUISettings = NewUISettings { enabled :: Boolean, newSearch :: Boolean, facets :: Array FacetSetting }
newtype UISettings = UISettings { newUI :: NewUISettings }

derive instance newtypeUISettings :: Newtype UISettings _
derive instance newtypeNewUISettings :: Newtype NewUISettings _
derive instance newtypeFacetSettings :: Newtype FacetSetting _

instance facetDec :: DecodeJson FacetSetting where
  decodeJson v = do
    o <- decodeJson v
    name <- o .? "name"
    path <- o .? "path"
    pure $ FacetSetting {name, path}

instance decNewUISettings :: DecodeJson NewUISettings where
  decodeJson v = do
    o <- decodeJson v
    enabled <- o .? "enabled"
    newSearch <- o .? "newSearch"
    facets <- o .? "facets"
    pure $ NewUISettings {enabled,newSearch,facets}

instance decUISettings :: DecodeJson UISettings where
  decodeJson v = do
    o <- decodeJson v
    newUI <- o .? "newUI"
    pure $ UISettings {newUI}

instance encFacetSetting :: EncodeJson FacetSetting where
  encodeJson (FacetSetting {name,path}) =
    "name" := name ~>
    "path" := path ~>
    jsonEmptyObject

instance encNewUISettings :: EncodeJson NewUISettings where
  encodeJson (NewUISettings {enabled,newSearch,facets}) =
     "enabled" := enabled ~>
     "newSearch" := newSearch ~>
     "facets" := facets ~>
     jsonEmptyObject

instance encUISettings :: EncodeJson UISettings where
  encodeJson (UISettings {newUI}) = "newUI" := newUI ~> jsonEmptyObject
