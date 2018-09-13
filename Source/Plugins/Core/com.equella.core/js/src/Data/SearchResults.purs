module OEQ.Data.SearchResults where 

import Prelude

import Data.Argonaut (class DecodeJson, decodeJson, getField, (.?))
import Data.Newtype (class Newtype)

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
  decodeJson v = do
    o <- decodeJson v
    (SearchResultsMeta {start,length,available}) <- decodeJson o
    results <- decodeJson o >>= flip getField "results"
    pure $ SearchResults {start,length,available,results}
