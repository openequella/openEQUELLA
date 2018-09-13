module Search.ResultDisplay where 


import Prelude

import Data.Array (length, mapWithIndex)
import Data.Maybe (Maybe(..))
import Data.Tuple (Tuple(..))
import Effect (Effect)
import MaterialUI.List (list)
import MaterialUI.Properties (component)
import Search.ItemResult (ItemResultOptions, Result, itemResult)
import Search.SearchControl (Placement(..), SearchControl)
import OEQ.Data.SearchResults (SearchResults(..))

renderResults :: Effect (Result -> ItemResultOptions) -> SearchControl
renderResults ef = case _ of 
  {results:Just (SearchResults {results})} -> do 
      f <- ef
      let render = [ Tuple Results $ 
            let resultLen = length results
                oneResult i r = itemResult $ (f r) {showDivider = i /= (resultLen - 1)}
            in list [component "section"] $ mapWithIndex oneResult results
          ]
      pure {render, chips:[] }
  _ -> pure $ {render:[], chips:[]}
