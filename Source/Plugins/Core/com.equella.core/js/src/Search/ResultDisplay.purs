module Search.ResultDisplay where 


import Prelude

import Data.Array (length, mapWithIndex)
import Data.Maybe (Maybe(..))
import Data.Tuple (Tuple(..))
import MaterialUI.List (list)
import MaterialUI.Properties (component)
import Search.ItemResult (ItemResultOptions, Result(..), itemResult, itemResultOptions)
import Search.SearchControl (Placement(..), SearchControl)
import SearchResults (SearchResults(..))

renderResults :: (Result -> ItemResultOptions) -> SearchControl
renderResults f = case _ of 
  {results:Just (SearchResults {results})} -> pure {
    render: [Tuple Results $ 
      let resultLen = length results
          oneResult i r = itemResult $ (f r) {showDivider = i /= (resultLen - 1)}
      in list [component "section"] $ mapWithIndex oneResult results
      ], chips:[]
  }
  _ -> pure $ {render:[], chips:[]}
