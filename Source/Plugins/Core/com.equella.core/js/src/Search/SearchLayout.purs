module Search.SearchLayout where 

import Prelude hiding (div)

import Control.Monad.Trans.Class (lift)
import Data.Argonaut (decodeJson)
import Data.Array (mapMaybe, singleton)
import Data.Either (Either, either)
import Data.Foldable (intercalate)
import Data.Int (floor)
import Data.Lens (_Just, addOver, appendOver, over, setJust)
import Data.Lens.Iso.Newtype (_Newtype)
import Data.Lens.Record (prop)
import Data.Maybe (Maybe(..), fromJust)
import Data.Symbol (SProxy(..))
import Data.Traversable (traverse)
import Data.Tuple (Tuple(..))
import Dispatcher (affAction)
import Dispatcher.React (getState, modifyState)
import EQUELLA.Environment (baseUrl)
import Effect.Aff (Aff)
import Effect.Class (liftEffect)
import Effect.Class.Console (log)
import Effect.Uncurried (mkEffectFn1)
import MaterialUI.Chip (chip, label)
import MaterialUI.CircularProgress (circularProgress)
import MaterialUI.Divider (divider)
import MaterialUI.Fade (fade)
import MaterialUI.Paper (elevation, paper)
import MaterialUI.Properties (className, component, onDelete, variant)
import MaterialUI.Styles (withStyles)
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
import Search.ItemResult (Result)
import Search.SearchControl (Chip(..), Placement(..), SearchControl, placementMatch)
import Search.SearchQuery (Query, blankQuery, searchQueryParams)
import SearchResults (SearchResults(..))
import TSComponents (appBarQuery)
import UIComp.DualPane (dualPane)
import Unsafe.Coerce (unsafeCoerce)
import Web.Event.Event (EventType(..))
import Web.Event.EventTarget (addEventListener, eventListener)
import Web.Event.Internal.Types (Event)
import Web.HTML (window)
import Web.HTML.HTMLDocument (body)
import Web.HTML.HTMLElement (offsetHeight)
import Web.HTML.Window (document, innerHeight, scrollY)

type ItemSearchResults = SearchResults Result

type State = {
  searching :: Boolean,
  loadingNew :: Boolean,
  query :: Query,
  searchResults :: Maybe ItemSearchResults
}
data Command = InitSearch | Search | QueryUpdate String
  | Scrolled Event | UpdateQuery (Query -> Query)

initialState :: State
initialState = {
    searching:false
  , query: blankQuery
  , searchResults:Nothing
  , loadingNew: false
}
type SearchStrings = { resultsAvailable :: String, refineTitle :: String }

searchLayout :: { 
    searchControls::Array SearchControl, 
    strings :: SearchStrings, 
    renderTemplate :: {queryBar :: ReactElement, content :: ReactElement } -> ReactElement 
    } -> ReactElement
searchLayout = unsafeCreateLeafElement $ withStyles styles $ R.component "SearchLayout" $ \this -> do
  let
    d = eval >>> affAction this
    _searchResults = prop (SProxy :: SProxy "searchResults")
    _results = prop (SProxy :: SProxy "results")
    _length = prop (SProxy :: SProxy "length")

    render = do 
      {searchResults,query,searching,loadingNew} <- R.getState this
      {classes,searchControls,strings,renderTemplate} <- R.getProps this
      controlOutputs <- traverse (\f -> f {updateQuery: d <<< UpdateQuery, results:searchResults, query}) $ searchControls
      
      let 
        controlsRendered = controlOutputs >>= _.render
        mainContent = dualPane { 
          left: renderResults searchResults <> progress, 
          right: (mapMaybe (placementMatch Selections) controlsRendered) <>
            [ typography [className classes.filterTitle, variant title] [text strings.refineTitle ] ] <>
            (intercalate [divider []] $ (mapMaybe (map singleton <<< placementMatch Filters) controlsRendered))
        }

        progress = [
          let pbar = circularProgress [className classes.progress]
          in fade [in_ $ searching || loadingNew, timeout $ if loadingNew then 0 else 800] [ pbar ]
        ]
        stdChip (Chip c) = chip [className classes.chip, label c.label, onDelete $ \_ -> c.onDelete]

        renderResults (Just (SearchResults {results,available})) = [
          div [ DP.className classes.resultHeader ] $ [
            typography [className classes.available, component "div", variant TS.subheading] [ 
                text $ show available <> " " <> strings.resultsAvailable ] 
          ] <> 
          (mapMaybe (placementMatch ResultHeader) controlsRendered)
          ,
          div [ DP.className classes.chipContainer ] $ 
            (map stdChip <<< _.chips =<< controlOutputs)
        ] <> (mapMaybe (placementMatch Results) controlsRendered) 
        renderResults Nothing = []
        
      pure $  renderTemplate { 
          queryBar: appBarQuery {query: query.query, onChange: mkEffectFn1 $ d <<< QueryUpdate}, 
          content: mainContent 
        } 

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
    chipContainer: {
      display: "flex",
      flexWrap: "wrap"
    },
    chip: {
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
