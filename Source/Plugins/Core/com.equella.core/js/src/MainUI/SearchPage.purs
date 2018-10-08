module OEQ.MainUI.SearchPage where

import Prelude hiding (div)

import Control.Bind (bindFlipped)
import Control.Monad.Trans.Class (lift)
import Data.Argonaut (decodeJson)
import Data.Array as Arrays
import Data.Either (either)
import Data.Map as Map
import Data.Maybe (Maybe(..))
import Data.Nullable (toNullable)
import Data.Traversable (sequence, traverse)
import Debug.Trace (traceM)
import Dispatcher (affAction)
import Dispatcher.React (modifyState, stateRenderer)
import Effect.Class (liftEffect)
import Effect.Class.Console (log)
import Foreign.Object as Object
import MaterialUI.Styles (withStyles)
import Network.HTTP.Affjax (get)
import Network.HTTP.Affjax.Response (json)
import OEQ.API.Searches (getPageConfig)
import OEQ.Data.Settings (NewUISettings(..), UISettings(..))
import OEQ.Environment (baseUrl, prepLangStrings)
import OEQ.MainUI.Routes (routeHref, oldViewItemRoute)
import OEQ.MainUI.Template (template', templateDefaults)
import OEQ.Search.Controls (controlFromConfig)
import OEQ.Search.FacetControl (facetControl)
import OEQ.Search.ItemResult (Result(..), itemResultOptions)
import OEQ.Search.OrderControl (orderControl)
import OEQ.Search.OwnerControl (ownerControl)
import OEQ.Search.ResultDisplay (renderResults)
import OEQ.Search.SearchControl (SearchControl, SearchControlRender)
import OEQ.Search.SearchLayout (searchLayout)
import OEQ.Search.SearchQuery (Query, blankQuery)
import OEQ.Search.WithinLastControl (withinLastControl)
import React (ReactElement, unsafeCreateLeafElement)
import React as R

type State = {
  searchControls :: Array SearchControlRender,
  query :: Query
}
data Command = InitSearch 

initialState :: State
initialState = {
    searchControls: [],
    query: blankQuery
}

searchPage :: ReactElement
searchPage = flip unsafeCreateLeafElement {} $ withStyles styles $ R.component "SearchPage" $ \this -> do
  oc <- ownerControl
  let
    coreString = prepLangStrings coreStrings
    d = eval >>> affAction this

    renderTemplate {queryBar,content} = template' (templateDefaults "") 
             {titleExtra = toNullable $ Just $ queryBar } [ content ]
    render {searchControls, query} = searchLayout {searchControls, initialQuery: query, strings:searchStrings, renderTemplate }
    eval = case _ of 
      InitSearch -> do
        sc <- lift $ getPageConfig "search"
        sc # either traceM \{sections} -> do 
            configedControls <- liftEffect $ sequence $ Object.values sections >>= map controlFromConfig 
            modifyState _ {searchControls = (_.renderer <$> configedControls) <> [
              renderResults $ pure \r@Result {uuid,version} -> itemResultOptions (routeHref $ oldViewItemRoute uuid version) r
            ]}
        -- result <- lift $ get json $ baseUrl <> "api/settings/ui"
        -- either (lift <<< log) (\(UISettings {newUI:(NewUISettings {facets})}) -> do 
        --   controls <- liftEffect $ traverse facetControl facets
        --   modifyState _ {facets = controls}) $ decodeJson result.response
  
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
