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
import Dispatcher.React (getState, modifyState, renderer)
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
import Search.FacetControl (facetControl)
import Search.ItemResult (Result, itemResult, itemResultOptions)
import Search.OrderControl (Order(..), orderControl, orderEntries, orderName, orderValue)
import Search.OwnerControl (ownerControl)
import Search.SearchControl (Chip(..), Placement(..), SearchControl, placementMatch)
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

type ItemSearchResults = SearchResults Result

type State = {
  searching :: Boolean,
  loadingNew :: Boolean,
  query :: Query,
  facets :: Array SearchControl,
  searchResults :: Maybe ItemSearchResults
}
data Command = InitSearch | Search | QueryUpdate String
  | Scrolled Event | UpdateQuery (Query -> Query)

initialState :: State
initialState = {
    searching:false
  , query: blankQuery
  , searchResults:Nothing
  , facets: []
  , loadingNew: false
}

searchPage :: ReactElement
searchPage = flip unsafeCreateLeafElement {} $ withStyles styles $ R.component "SearchPage" $ \this -> do
  oc <- ownerControl
  let
    searchControls = [orderControl, oc, withinLastControl]
    d = eval >>> affAction this
    _id = prop (SProxy :: SProxy "id")
    _searchResults = prop (SProxy :: SProxy "searchResults")
    _results = prop (SProxy :: SProxy "results")
    _length = prop (SProxy :: SProxy "length")

    string = prepLangStrings rawStrings
    coreString = prepLangStrings coreStrings

    render = do 
      {searchResults,query,facets,searching,loadingNew} <- R.getState this
      {classes} <- R.getProps this
      controlsRendered <- traverse (\f -> f {updateQuery: d <<< UpdateQuery, results:searchResults, query}) $ searchControls <> facets

      let 
        mainContent = div [DP.className classes.layoutDiv] [
          paper [className classes.results, elevation 4] $ 
            renderResults searchResults <> progress,
          paper [className classes.refinements, elevation 4] $ 
            [ typography [className classes.filterTitle, variant title] [text string.refineTitle ] ] <>
            (intercalate [divider []] $ (mapMaybe (map singleton <<< placementMatch Filters) <<< _.render =<< controlsRendered))
        ]

        progress = [
          let pbar = circularProgress [className classes.progress]
          in fade [in_ $ searching || loadingNew, timeout $ if loadingNew then 0 else 800] [ pbar ]
        ]
        stdChip (Chip c) = chip [className classes.chip, label c.label, onDelete $ \_ -> c.onDelete]

        renderResults (Just (SearchResults {results,available})) =
          let resultLen = length results
              orderItem o = menuItem [mkProp "value" $ orderValue o] [ text $ orderName o ]
              oneResult i r = itemResult $ (itemResultOptions r) {showDivider = i /= (resultLen - 1)}
          in [
            div [ DP.className classes.resultHeader ] $ [
              typography [className classes.available, component "div", variant TS.subheading] [ 
                  text $ show available <> " " <> string.resultsAvailable ] 
              ] <> 
              (mapMaybe (placementMatch ResultHeader) <<< _.render =<< controlsRendered)
            ,
            div [ DP.className classes.facetContainer ] $ 
              (map stdChip <<< _.chips =<< controlsRendered),
            list [component "section"] $ mapWithIndex oneResult results 
          ]
        renderResults Nothing = []
      pure $ template' (templateDefaults coreString.title) 
            {titleExtra = toNullable $ Just $ appBarQuery {query: query.query, onChange: mkEffectFn1 $ d <<< QueryUpdate} } [ 
            mainContent 
          ]

    modifySearchFlag searchFlag f = modifyState $ _{searching=searchFlag} <<< f

    searchMore = do 
      s <- getState
      case s of 
        {searching:false, searchResults:Just (SearchResults {start,length,available})} | start+length < available -> do 
          modifySearchFlag true identity
          sr <- lift $ callSearch (start+length) s
          let appendres (SearchResults newres) = 
                modifySearchFlag false $ over (_searchResults <<< _Just <<< _Newtype) 
                  ((appendOver _results newres.results) <<< (addOver _length newres.length))
          either (lift <<< log) appendres sr
        _ -> pure unit
      

    searchWith f = do
      s <- getState
      modifySearchFlag true f
      sr <- lift $ callSearch 0 (f s)
      either (lift <<< log) (modifySearchFlag false <<< setJust _searchResults) $ sr

    eval = case _ of 
      InitSearch -> do
        searchWith identity
        liftEffect $ do 
          w <- window
          l <- eventListener $ affAction this <<< eval <<< Scrolled
          addEventListener (EventType "scroll") l false (unsafeCoerce w)
        result <- lift $ get json $ baseUrl <> "api/settings/ui"
        either (lift <<< log) (\(UISettings {newUI:(NewUISettings {facets})}) -> do 
          controls <- liftEffect $ traverse facetControl facets
          modifyState _ {facets = controls}) $ decodeJson result.response

      Scrolled e -> do
        shouldScroll <- liftEffect $ do 
          w <- window
          h <- innerHeight w
          sY <- scrollY w
          b <- document w >>= body
          oh <- unsafePartial $ offsetHeight $ fromJust b
          pure $ h + sY >= (floor oh - 500) 
        if shouldScroll then searchMore else pure unit

      Search -> searchWith identity
      QueryUpdate q -> searchWith \s -> s {query = s.query {query = q} }
      UpdateQuery f -> searchWith \s -> s {query = f s.query}
  
  pure {render, state:initialState, componentDidMount: d InitSearch}
  where 
  styles theme = {
    results: {
      flexBasis: "75%",
      display: "flex",
      flexDirection: "column",
      padding: 16
    },
    refinements: {
      flexBasis: "25%",
      marginLeft: 16, 
      display: "flex", 
      flexDirection: "column",
      padding: theme.spacing.unit * 2
    },
    layoutDiv: {
      padding: theme.spacing.unit * 2,
      display: "flex",
      justifyContent: "space-around"
    },
    facetContainer: {
      display: "flex",
      flexWrap: "wrap"
    },
    chip: {
      margin: theme.spacing.unit
    },
    dateContainer: {
      margin: theme.spacing.unit
    },
    progress: {
      alignSelf: "center"
    }, 
    filterTitle: {
      alignSelf: "center",
      margin: theme.spacing.unit
    }, 
    resultHeader: {
      display: "flex", 
      alignItems: "center"
    }, 
    ordering: {
      width: "10em"
    },
    available: {
      flexGrow: 1
    }
}

callSearch :: Int -> State -> Aff (Either String ItemSearchResults)
callSearch offset {query} = do
  result <- get json $ baseUrl <> "api/search?" <> (queryString $ [
      Tuple "info" "basic,detail,attachment,display",
      Tuple "start" $ show offset
    ] <> searchQueryParams query
  )
  pure $ decodeJson result.response

whereClause :: Tuple String (S.Set String) -> Maybe String
whereClause (Tuple node terms) | not isEmpty terms = Just $ "(" <> (joinWith " OR " $ clause <$> S.toUnfoldable terms) <> ")"
  where clause term = "/xml" <> node <> " = " <> "'" <> term <> "'"
whereClause _ = Nothing

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
