module OEQ.Search.SearchQuery where 

import Prelude

import Control.Bind (bindFlipped)
import Control.MonadZero (guard)
import Data.Argonaut (class EncodeJson, Json, encodeJson)
import Data.Array as Array
import Data.Lens (Lens')
import Data.Lens.Record (prop)
import Data.Map (Map)
import Data.Map as Map
import Data.Maybe (Maybe)
import Data.String (joinWith)
import Data.Symbol (SProxy(..))
import Data.Tuple (Tuple(..))

data QueryParam = Param String String | XPath (Array String) | Params (Array (Tuple String String))
type ParamData = {data::Json, value::QueryParam}
derive instance eqQP :: Eq QueryParam

type Query = {
    query :: String,
    params :: Map String ParamData
}

type ParamDataLens = Lens' (Map String ParamData) (Maybe ParamData)

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

singleParam :: forall a. EncodeJson a => a -> String -> String -> ParamData
singleParam a n v = {data: encodeJson a, value:Param n v}

emptyQueryParam :: forall a. EncodeJson a => a -> ParamData 
emptyQueryParam a = {data: encodeJson a, value:Params []}

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
        