module OEQ.Data.Facet where 

import Prelude

import Data.Argonaut (class DecodeJson, Json, decodeJson, (.?))
import Data.Either (Either)

type FacetSetting = { title :: String, node :: String }

newtype FacetResult = FacetResult {term::String, count::Int}
newtype FacetResults = FacetResults (Array FacetResult)

instance frDecode :: DecodeJson FacetResult where
  decodeJson v = do
    o <- decodeJson v
    term <- o .? "term"
    count <- o .? "count"
    pure $ FacetResult {term,count}

instance frsDecode :: DecodeJson FacetResults where
  decodeJson v = do
    o <- decodeJson v
    results <- o .? "results"
    pure $ FacetResults results

decodeFacetSetting :: Json -> Either String FacetSetting
decodeFacetSetting v = do
    o <- decodeJson v
    title <- o .? "title"
    node <- o .? "node"
    pure $ {title, node}

