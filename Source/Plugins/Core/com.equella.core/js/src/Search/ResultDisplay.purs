module OEQ.Search.ResultDisplay where 


import Prelude

import Data.Array (length, mapWithIndex)
import Data.Maybe (Maybe(..))
import Data.Tuple (Tuple(..))
import Effect (Effect)
import MaterialUI.List (list)
import OEQ.Data.SearchResults (SearchResults(..))
import OEQ.Search.ItemResult (ItemResultOptions, Result, itemResult)
import OEQ.Search.SearchControl (Placement(..), SearchControlRender)

renderResults :: Effect (Result -> ItemResultOptions) -> SearchControlRender
renderResults ef = let 
  renderer {results:Just (SearchResults {results})} = do 
      f <- ef
      let render = [ Tuple Results $ 
            let resultLen = length results
                oneResult i r = itemResult $ (f r) {showDivider = i /= (resultLen - 1)}
            in list {component: "section"} $ mapWithIndex oneResult results
          ]
      pure {render, chips:[] }
  renderer _ = pure $ {render:[], chips:[]}
  in renderer
