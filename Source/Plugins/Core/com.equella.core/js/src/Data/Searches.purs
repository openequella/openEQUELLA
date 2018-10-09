module OEQ.Data.Searches where 

import Prelude

import Data.Argonaut (Json, decodeJson, (.?), (.??))
import Data.Either (Either(..), note)
import Data.Maybe (Maybe(..))
import Data.Traversable (traverse)
import Foreign.Object (Object)
import OEQ.Data.Facet (FacetSetting, decodeFacetSetting)

data Order = Relevance | DateModified | Name | Rating | DateCreated

orderValue :: Order -> String
orderValue = case _ of 
  Relevance -> "relevance"
  Name -> "name"
  DateModified -> "modified"
  DateCreated -> "created"
  Rating -> "rating"

orderFromString :: String -> Maybe Order 
orderFromString = case _ of 
  "relevance" -> Just Relevance
  "name" -> Just Name 
  "modified" -> Just DateModified 
  "created" -> Just DateCreated 
  "rating" -> Just Rating 
  _ -> Nothing


type SearchConfig = {
    index :: String, 
    sections :: Object (Array SearchControlConfig)
}

type SortConfig = {
  editable :: Boolean, 
  default :: Order
}

type ModifiedWithinConfig = {
  editable :: Boolean, 
  default :: Number
}

type OwnerConfig = {
  editable :: Boolean
}

type CollectionsConfig = {
  editable :: Boolean, 
  collections :: Maybe (Array String)
}

data SearchControlConfig = Sort SortConfig
  | Owner OwnerConfig
  | Facet FacetSetting
  | ModifiedWithin ModifiedWithinConfig
  | Collections CollectionsConfig

decodeControlConfig :: Json -> Either String SearchControlConfig
decodeControlConfig v = do 
  o <- decodeJson v 
  o .? "type" >>= case _ of 
    "sort" -> do 
      default <- o .? "default" >>= (\os -> note ("Invalid order: " <> os) $ orderFromString os)
      editable <- o .? "editable"
      pure $ Sort {default,editable}
    "facet" -> Facet <$> decodeFacetSetting v
    "owner" -> do 
      editable <- o .? "editable"
      pure $ Owner {editable}
    "collections" -> do
      editable <- o .? "editable"
      collections <- o .?? "collections"
      pure $ Collections {editable, collections}
    "modifiedWithin" -> do 
      default <- o .? "default"
      editable <- o .? "editable"
      pure $ ModifiedWithin {editable,default}  
    c -> Left $ "Unknown control type: " <> c

decodeSearchConfig :: Json -> Either String SearchConfig 
decodeSearchConfig v = do 
  o <- decodeJson v 
  index <- o .? "index"
  sectionsM <- o .? "sections"
  sections <- traverse (traverse decodeControlConfig) sectionsM
  pure {index,sections}