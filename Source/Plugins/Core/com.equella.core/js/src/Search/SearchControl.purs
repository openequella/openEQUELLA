module OEQ.Search.SearchControl where 

import Prelude

import Data.Maybe (Maybe(..))
import Data.Tuple (Tuple, fst, snd)
import Effect (Effect)
import OEQ.Data.SearchResults (SearchResults)
import React (ReactElement)
import OEQ.Search.ItemResult (Result)
import OEQ.Search.SearchQuery (Query)

data Placement = Filters | ResultHeader | Results | Selections
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

type SearchControlRender = {|ControlParams} -> ControlRender

type SearchControl = {
    initQuery :: Query -> Query,
    renderer :: SearchControlRender
}

placementMatch :: Placement -> Tuple Placement ReactElement -> Maybe ReactElement 
placementMatch p t | fst t == p = Just $ snd t
placementMatch _ _ = Nothing
