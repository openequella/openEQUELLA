module OEQ.MainUI.SearchPage where

import Prelude hiding (div)

import Control.Monad.Trans.Class (lift)
import Data.Argonaut (decodeJson)
import Data.Either (either)
import Data.Maybe (Maybe(..))
import Data.Nullable (toNullable)
import Data.Traversable (traverse)
import Dispatcher (affAction)
import Dispatcher.React (modifyState, stateRenderer)
import Effect.Class (liftEffect)
import Effect.Class.Console (log)
import MaterialUI.Styles (withStyles)
import Network.HTTP.Affjax (get)
import Network.HTTP.Affjax.Response (json)
import OEQ.Data.Settings (NewUISettings(..), UISettings(..))
import OEQ.Environment (baseUrl, prepLangStrings)
import OEQ.MainUI.Routes (routeHref, viewItemRoute)
import OEQ.MainUI.Template (template', templateDefaults)
import React (ReactElement, unsafeCreateLeafElement)
import React as R
import Search.FacetControl (facetControl)
import Search.ItemResult (Result(..), itemResultOptions)
import Search.OrderControl (orderControl)
import Search.OwnerControl (ownerControl)
import Search.ResultDisplay (renderResults)
import Search.SearchControl (SearchControl)
import Search.SearchLayout (searchLayout)
import Search.WithinLastControl (withinLastControl)

type State = {
  facets :: Array SearchControl
}
data Command = InitSearch 

initialState :: State
initialState = {
    facets: []
}

searchPage :: ReactElement
searchPage = flip unsafeCreateLeafElement {} $ withStyles styles $ R.component "SearchPage" $ \this -> do
  oc <- ownerControl
  let
    d = eval >>> affAction this
    searchControls = [orderControl, oc, withinLastControl, renderResults $ pure \r@Result {uuid,version} -> 
      itemResultOptions (routeHref $ viewItemRoute uuid version) r]
    coreString = prepLangStrings coreStrings

    renderTemplate {queryBar,content} = template' (templateDefaults "") 
             {titleExtra = toNullable $ Just $ queryBar } [ content ]
    render {facets} = searchLayout {searchControls: searchControls <> facets, strings:searchStrings, renderTemplate }
    eval = case _ of 
      InitSearch -> do
        result <- lift $ get json $ baseUrl <> "api/settings/ui"
        either (lift <<< log) (\(UISettings {newUI:(NewUISettings {facets})}) -> do 
          controls <- liftEffect $ traverse facetControl facets
          modifyState _ {facets = controls}) $ decodeJson result.response
  
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
