module Search.SearchQuery where 

import Prelude

import Control.Bind (bindFlipped)
import Control.MonadZero (guard)
import Data.Argonaut (Json, jsonNull)
import Data.Array as Array
import Data.Lens (Lens', Prism', prism')
import Data.Lens.Record (prop)
import Data.Map (Map)
import Data.Map as Map
import Data.Maybe (Maybe(..))
import Data.String (joinWith)
import Data.Symbol (SProxy(..))
import Data.Tuple (Tuple(..))

data QueryParam = Param String String | XPath (Array String) | Params (Array (Tuple String String))

derive instance eqQP :: Eq QueryParam

type Query = {
    query :: String,
    params :: Map String {data::Json, value::QueryParam}
}

blankQuery :: Query
blankQuery = {
    query: "", 
    params: Map.empty
}

_params :: forall r a. Lens' {params::a|r} a
_params = prop (SProxy :: SProxy "params")

_value :: forall r a. Lens' {value::a|r} a
_value = prop (SProxy :: SProxy "value")

_data :: forall r a. Lens' {data::a|r} a
_data = prop (SProxy :: SProxy "data")

singleParam :: String -> String -> {data::Json, value::QueryParam}
singleParam n v = {data:jsonNull, value:Param n v}

_Param :: Prism' QueryParam (Tuple String String)
_Param = prism' (\(Tuple n v) -> Param n v) case _ of 
    Param n v -> Just $ Tuple n v
    _ -> Nothing

searchQueryParams :: Query -> Array (Tuple String String)
searchQueryParams {query,params} = let 
  paramOnly (Param n v) = [Tuple n v]
  paramOnly (Params a) = a
  paramOnly _ = []
  clauses (XPath v) = v 
  clauses _ = []
  valsOnly = Array.fromFoldable $ (_.value <$> Map.values params)
  clausesOnly = clauses =<< valsOnly
  in [(Tuple "q" query)] <> 
    (bindFlipped paramOnly valsOnly) <> 
    (guard (not $ Array.null clausesOnly) $> 
        (Tuple "where" $ joinWith " AND " clausesOnly))
        