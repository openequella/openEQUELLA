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
import Data.Nullable (toNullable)
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
import Search.SearchControl (Chip(..), Placement(..), SearchControl, placementMatch)
import Search.SearchLayout (ItemSearchResults, searchLayout)
import Search.SearchQuery (Query, blankQuery, searchQueryParams)
import Search.WithinLastControl (withinLastControl)
import SearchPage (searchStrings)
import SearchResults (SearchResults(..))
import Selection.ReturnResult (ReturnData, decodeReturnData, executeReturn)
import Settings.UISettings (FacetSetting(..))
import Template (renderMain, template', templateDefaults)

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

data Command = SelectFolder String | SelectionMade ItemSelection | ReturnSelections 

type State = {
  selectedFolder :: String, 
  selections :: Map String (Array ItemSelection)
}

selectSearch :: {selection::SelectionData} -> ReactElement
selectSearch = unsafeCreateLeafElement $ withStyles styles $ component "SelectSearch" $ \this -> do 
  oc <- ownerControl
  fc <- facetControl $ FacetSetting {name:"Name", path:"/item/name"}
  let 
    d = eval >>> affAction this
    _selections = prop (SProxy :: SProxy "selections")
    searchControls = [orderControl, oc, fc, withinLastControl]


        -- (courseStructure <<< {selectedFolder, selections, onSelectFolder: d <<< SelectFolder, structure: _}) <$> selection.courseData.structure

    renderTemplate {queryBar,content} = template' (templateDefaults "Selection") 
             {titleExtra = toNullable $ Just $ queryBar } [ content ]

    render {props:{classes, selection}, state:{selectedFolder,selections}} = 
      searchLayout {searchControls, strings: searchStrings, renderTemplate}
      -- let 
      --   stdChip (Chip c) = chip [className classes.chip, label c.label, onDelete $ \_ -> c.onDelete]
      --   renderResults (SearchResults {results,available}) = 
      --       list [MUIC.component "section"] $ mapWithIndex oneResult results
      --     where 
      --       lastResult = length results - 1
      --       oneResult i r = itemResult $ (itemResultOptions r) {showDivider = i /= lastResult, onSelect = Just $ d <<< SelectionMade}
      -- pure $ div' $ (mapMaybe (placementMatch Filters) <<< _.render =<< controlsRendered) <> catMaybes [ 
      --   Just $ div' $ (map stdChip <<< _.chips =<< controlsRendered),
      --   Just $ MUI.button [onClick $ \_ -> d ReturnSelections] [ text "Return" ],
      --   searchResults <#> renderResults,
      --   -- Just $ itemResult $ (itemResultOptions sampleResult) {showDivider = true, onSelect = Just $ d <<< SelectionMade}, 
      -- ]

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

  pure {render: renderer render this, 
        state: {
          selectedFolder:"", 
          selections:empty
        } :: State}
  where 
  styles theme = {
    
  }

main :: Effect Unit
main = do
  polyfill
  let js  = jsonParser selectionJson
      s = either unsafeCrashWith identity $ js >>= decodeSelection
  renderMain $ selectSearch {selection:s}