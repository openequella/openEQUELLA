module OEQ.Data.SearchResults where 

import Prelude

import Data.Argonaut (class DecodeJson, Json, decodeJson, getField, (.?))
import Data.Either (Either)
import Data.Newtype (class Newtype)
import Data.Traversable (traverse)

type ResultMeta r = {start::Int, length::Int, available::Int | r}
newtype SearchResults a = SearchResults (ResultMeta (results::Array a))
newtype SearchResultsMeta = SearchResultsMeta (ResultMeta ())


derive instance srNT :: Newtype (SearchResults a) _

instance srmDecode :: DecodeJson SearchResultsMeta where
  decodeJson v = do
    o <- decodeJson v
    start <- o .? "start"
    length <- o .? "length"
    available <- o .? "available"
    pure $ SearchResultsMeta {start,length,available}

instance srDecode :: DecodeJson a => DecodeJson (SearchResults a) where
  decodeJson = decodeSearchResults decodeJson

decodeSearchResults :: forall a. (Json -> Either String a) -> Json -> Either String (SearchResults a)
decodeSearchResults f v = do
    o <- decodeJson v
    (SearchResultsMeta {start,length,available}) <- decodeJson v
    results <- o .? "results" >>= traverse f
    pure $ SearchResults {start,length,available,results}