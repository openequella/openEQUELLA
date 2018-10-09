module OEQ.MainUI.SearchPage where

import Prelude hiding (div)

import Control.Monad.Trans.Class (lift)
import Data.Array (mapMaybe)
import Data.Either (either)
import Data.Foldable (fold)
import Data.Maybe (Maybe(..))
import Data.Nullable (toNullable)
import Data.Traversable (foldr, sequence, traverse)
import Data.Tuple (Tuple(..))
import Debug.Trace (traceM)
import Dispatcher (affAction)
import Dispatcher.React (modifyState, stateRenderer)
import Effect.Class (liftEffect)
import Foreign.Object (toArrayWithKey)
import Foreign.Object as Object
import MaterialUI.Styles (withStyles)
import OEQ.API.Searches (getPageConfig)
import OEQ.Environment (prepLangStrings)
import OEQ.MainUI.Routes (routeHref, oldViewItemRoute)
import OEQ.MainUI.Template (template', templateDefaults)
import OEQ.Search.Controls (controlFromConfig, queryFromConfig)
import OEQ.Search.ItemResult (Result(..), itemResultOptions)
import OEQ.Search.ResultDisplay (renderResults)
import OEQ.Search.SearchControl (SearchControl, placementFromString)
import OEQ.Search.SearchLayout (searchLayout)
import OEQ.Search.SearchQuery (Query, blankQuery)
import React (ReactElement, unsafeCreateLeafElement)
import React as R

type State = {
  config :: Maybe (Tuple Query (Array SearchControl))
}

data Command = InitSearch 

initialState :: State
initialState = {
    config: Nothing
}

searchPage :: ReactElement
searchPage = flip unsafeCreateLeafElement {} $ withStyles styles $ R.component "SearchPage" $ \this -> do
  let
    coreString = prepLangStrings coreStrings
    d = eval >>> affAction this

    renderTemplate {queryBar,content} = template' (templateDefaults "") 
             {titleExtra = toNullable $ Just $ queryBar } [ content ]
    render {config: Just (Tuple query searchControls)} = searchLayout {searchControls, initialQuery: query, strings:searchStrings, renderTemplate }
    render _ = template' (templateDefaults "") [ ]
    eval = case _ of 
      InitSearch -> do
        sc <- lift $ getPageConfig "search"
        sc # either traceM \{sections} -> do 
            let query = foldr queryFromConfig blankQuery $ join $ Object.values sections
                controlsForSection p configs = sequence $ mapMaybe (controlFromConfig $ placementFromString p) configs
            configedControls <- liftEffect $ fold $ toArrayWithKey controlsForSection sections
            modifyState _ {config = Just $ Tuple query $ configedControls <> [
              renderResults $ pure \r@Result {uuid,version} -> itemResultOptions (routeHref $ oldViewItemRoute uuid version) r
            ]}
  
  pure {render: stateRenderer render this, state:initialState, componentDidMount: d InitSearch}
  where 
  styles theme = {
  }

searchStrings = prepLangStrings rawStrings

rawStrings :: { prefix :: String
, strings :: { resultsAvailable :: String
             , refineTitle :: String
             }
}
rawStrings = {prefix: "searchpage", 
  strings: {
    resultsAvailable: "results available",
    refineTitle: "Refine search"
  }
}

coreStrings :: { prefix :: String
, strings :: { title :: String
             }
}
coreStrings = {
  prefix: "com.equella.core.searching.search", 
  strings: {
    title: "Search"
  }
}
