module SearchPage where

import Prelude hiding (div)

import Control.Monad.Trans.Class (lift)
import Data.Argonaut (decodeJson)
import Data.Array (catMaybes, filter, find, intercalate, length, mapMaybe, mapWithIndex, singleton)
import Data.Either (Either, either)
import Data.Int (floor)
import Data.Lens (_Just, addOver, appendOver, over, setJust)
import Data.Lens.Iso.Newtype (_Newtype)
import Data.Lens.Record (prop)
import Data.Maybe (Maybe(Nothing, Just), fromJust, fromMaybe)
import Data.Newtype (unwrap)
import Data.Nullable (toNullable)
import Data.Set (isEmpty)
import Data.Set as S
import Data.String (joinWith)
import Data.Symbol (SProxy(..))
import Data.Traversable (traverse)
import Data.Tuple (Tuple(..), fst)
import Dispatcher (affAction)
import Dispatcher.React (getState, modifyState, renderer, stateRenderer)
import EQUELLA.Environment (baseUrl, prepLangStrings)
import Effect.Aff (Aff)
import Effect.Class (liftEffect)
import Effect.Class.Console (log)
import Effect.Uncurried (mkEffectFn1)
import Facet (facetDisplay)
import Foreign.Object (lookup)
import Foreign.Object as SM
import MaterialUI.Chip (chip)
import MaterialUI.CircularProgress (circularProgress)
import MaterialUI.Divider (divider)
import MaterialUI.Fade (fade)
import MaterialUI.List (list)
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Paper (elevation, paper)
import MaterialUI.Properties (className, component, mkProp, onChange, onDelete, variant)
import MaterialUI.Select (select)
import MaterialUI.Styles (withStyles)
import MaterialUI.TextField (label, value)
import MaterialUI.TextStyle (title)
import MaterialUI.TextStyle as TS
import MaterialUI.Transition (in_, timeout)
import MaterialUI.Typography (typography)
import Network.HTTP.Affjax (get)
import Network.HTTP.Affjax.Response (json)
import Partial.Unsafe (unsafePartial)
import QueryString (queryString)
import React (ReactElement, unsafeCreateLeafElement)
import React as R
import React.DOM (div, text)
import React.DOM.Props as DP
import Routes (routeHref, viewItemRoute)
import Search.FacetControl (facetControl)
import Search.ItemResult (Result(..), itemResult, itemResultOptions)
import Search.OrderControl (Order(..), orderControl, orderEntries, orderName, orderValue)
import Search.OwnerControl (ownerControl)
import Search.ResultDisplay (renderResults)
import Search.SearchControl (Chip(..), Placement(..), SearchControl, placementMatch)
import Search.SearchLayout (searchLayout)
import Search.SearchQuery (Query, blankQuery, searchQueryParams)
import Search.WithinLastControl (withinLastControl)
import SearchResults (SearchResults(..))
import Settings.UISettings (FacetSetting(..), NewUISettings(..), UISettings(..))
import TSComponents (appBarQuery)
import Template (template', templateDefaults)
import Unsafe.Coerce (unsafeCoerce)
import Utils.UI (valueChange)
import Web.Event.Event (Event, EventType(..))
import Web.Event.EventTarget (addEventListener, eventListener)
import Web.HTML (window)
import Web.HTML.HTMLDocument (body)
import Web.HTML.HTMLElement (offsetHeight)
import Web.HTML.Window (document, innerHeight, scrollY)

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
    searchControls = [orderControl, oc, withinLastControl, renderResults \r@Result {uuid,version} -> 
      itemResultOptions (routeHref $ viewItemRoute uuid version) r]
    coreString = prepLangStrings coreStrings

    renderTemplate {queryBar,content} = template' (templateDefaults coreString.title) 
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
