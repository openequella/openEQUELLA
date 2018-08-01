module Selection.Main where

import Prelude

import Control.Monad.Reader (lift, runReaderT)
import Course.Structure (CourseStructure, courseStructure, decodeStructure)
import Data.Argonaut (Json, decodeJson, jsonParser, (.?), (.??))
import Data.Array (catMaybes, length, mapMaybe, mapWithIndex)
import Data.Either (Either, either)
import Data.Function (apply)
import Data.Lens (over)
import Data.Lens.At (at)
import Data.Lens.Record (prop)
import Data.Map (Map, empty)
import Data.Map as Map
import Data.Maybe (Maybe(..))
import Data.Symbol (SProxy(..))
import Data.Traversable (traverse)
import Data.Tuple (Tuple(..), snd)
import Dispatcher (affAction)
import Dispatcher.React (ReactReaderT, getProps, getState, modifyState, renderer)
import EQUELLA.Environment (baseUrl)
import Effect (Effect)
import Effect.Aff (Aff)
import Effect.Class (liftEffect)
import Foreign.Object (Object)
import MaterialUI.Button as MUI
import MaterialUI.Chip (chip, label)
import MaterialUI.List (list)
import MaterialUI.Properties (className, onClick, onDelete)
import MaterialUI.Properties as MUIC
import MaterialUI.Styles (withStyles)
import Network.HTTP.Affjax as Ajax
import Network.HTTP.Affjax.Response as Resp
import Partial (crashWith)
import Partial.Unsafe (unsafeCrashWith)
import Polyfills (polyfill)
import QueryString (queryString)
import React (ReactElement, component, modifyStateWithCallback, unsafeCreateLeafElement)
import React as R
import React.DOM (div', text)
import Search.FacetControl (facetControl)
import Search.ItemResult (Result(..), ItemSelection, itemResult, itemResultOptions)
import Search.OrderControl (orderControl)
import Search.OwnerControl (ownerControl)
import Search.SearchControl (Chip(..), Placement(..), SearchControl)
import Search.SearchQuery (Query, blankQuery, searchQueryParams)
import Search.WithinLastControl (withinLastControl)
import SearchPage (ItemSearchResults)
import SearchResults (SearchResults(..))
import Selection.ReturnResult (ReturnData, decodeReturnData, executeReturn)
import Settings.UISettings (FacetSetting(..))
import Template (renderMain)

foreign import selectionJson :: String

type CourseData = {
  courseId :: Maybe String, 
  courseCode :: Maybe String,
  structure :: Maybe CourseStructure
}

type SelectionData = {
  courseData :: CourseData,
  returnData :: ReturnData
}

decodeCourseData :: Object Json -> Either String CourseData
decodeCourseData o = do 
  courseId <- o .?? "courseId"
  courseCode <- o .?? "courseCode"
  structure <- o .?? "structure" >>= traverse decodeStructure
  pure {courseId, courseCode, structure}

decodeSelection :: Json -> Either String SelectionData 
decodeSelection v = do 
  o <- decodeJson v 
  courseData <- o .? "courseData" >>= decodeCourseData
  returnData <- o .? "returnData" >>= decodeReturnData
  pure {courseData,returnData}

data Command = SelectFolder String | SelectionMade ItemSelection | ReturnSelections | ResetSearch | UpdateQuery (Query -> Query)

type State = {
  query :: Query, 
  selectedFolder :: String, 
  selections :: Map String (Array ItemSelection), 
  searchResults :: Maybe ItemSearchResults
}

selectSearch :: {selection::SelectionData} -> ReactElement
selectSearch = unsafeCreateLeafElement $ withStyles styles $ component "SelectSearch" $ \this -> do 
  oc <- ownerControl
  fc <- facetControl $ FacetSetting {name:"Name", path:"/item/name"}
  let 
    d = eval >>> affAction this
    _selections = prop (SProxy :: SProxy "selections")
    controls :: Array SearchControl
    controls = [orderControl, oc, fc, withinLastControl]
    render {props:{classes, selection}, state:{selectedFolder,selections,query,searchResults}} = do 
      controlsRendered <- traverse (\f -> f {updateQuery: d <<< UpdateQuery, results:searchResults, query}) controls
      let 
        placementFilter (f :: Placement) = mapMaybe (\(Tuple p e) -> if p == f then Just e else Nothing)
        stdChip (Chip c) = chip [className classes.chip, label c.label, onDelete $ \_ -> c.onDelete]
        renderResults (SearchResults {results,available}) = 
            list [MUIC.component "section"] $ mapWithIndex oneResult results
          where 
            lastResult = length results - 1
            oneResult i r = itemResult $ (itemResultOptions r) {showDivider = i /= lastResult, onSelect = Just $ d <<< SelectionMade}
      pure $ div' $ (placementFilter Filters <<< _.render =<< controlsRendered) <> catMaybes [ 
        Just $ div' $ (map stdChip <<< _.chips =<< controlsRendered),
        Just $ MUI.button [onClick $ \_ -> d ReturnSelections] [ text "Return" ],
        searchResults <#> renderResults,
        -- Just $ itemResult $ (itemResultOptions sampleResult) {showDivider = true, onSelect = Just $ d <<< SelectionMade}, 
        (courseStructure <<< {selectedFolder, selections, onSelectFolder: d <<< SelectFolder, structure: _}) <$> selection.courseData.structure
      ]

    eval = case _ of 
      SelectFolder f -> modifyState _ {selectedFolder = f}
      SelectionMade sel -> let 
        addToFolder (Just f) = Just $ (f <> [sel])
        addToFolder Nothing = Just [sel]
        in modifyState $ \s@{selectedFolder} -> over (_selections <<< at selectedFolder) addToFolder s
      ReturnSelections -> do
        {selections} <- getState 
        {selection} <- getProps
        liftEffect $ executeReturn selection.returnData (Map.toUnfoldable selections)
      ResetSearch -> do 
        {query} <- getState 
        result <- lift $ Ajax.get Resp.json $ baseUrl <> "api/search?" <> (queryString $ [
                    Tuple "info" "basic,detail,attachment,display",
                    Tuple "start" "0"
                  ] <> searchQueryParams query)
        either unsafeCrashWith (\sr -> modifyState _ { searchResults = Just sr}) $ decodeJson result.response
      UpdateQuery f -> do
        liftEffect $ modifyStateWithCallback this (\s -> s {query = f s.query}) (affAction this (eval ResetSearch))

  pure {render: do 
          props <- R.getProps this
          state <- R.getState this
          render {props,state}
        , 
        componentDidMount: d ResetSearch,
        state: {
          selectedFolder:"", 
          selections:empty, 
          query: blankQuery, 
          searchResults: Nothing 
        } :: State}
  where 
  styles theme = {
    chip: {
      margin: theme.spacing.unit
    }
  }

main :: Effect Unit
main = do
  polyfill
  let js  = jsonParser selectionJson
      s = either unsafeCrashWith identity $ js >>= decodeSelection
  renderMain $ selectSearch {selection:s}