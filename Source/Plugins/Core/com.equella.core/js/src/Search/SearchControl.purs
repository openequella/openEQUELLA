module Search.SearchControl where 

import Prelude

import Data.Maybe (Maybe)
import Data.Tuple (Tuple)
import Effect (Effect)
import React (ReactElement)
import Search.ItemResult (Result)
import Search.SearchQuery (Query)
import SearchResults (SearchResults)

data Placement = Filters
derive instance eqP :: Eq Placement 

newtype Chip = Chip {label::String, onDelete::Effect Unit}

type ControlRender = Effect { 
    render :: Array (Tuple Placement ReactElement), 
    chips :: Array Chip
}

type ControlParams = (
    updateQuery :: (Query -> Query) -> Effect Unit, 
    results :: Maybe (SearchResults Result),
    query :: Query
)

type SearchControl = {|ControlParams} -> ControlRender
