module Selection.Main where

import Prelude

import Course.Structure (CourseStructure, courseStructure, decodeStructure)
import Data.Argonaut (Json, decodeJson, jsonParser, (.?), (.??))
import Data.Array as Array
import Data.Either (Either, either)
import Data.Lens (over)
import Data.Lens.At (at)
import Data.Lens.Record (prop)
import Data.Map (Map, empty)
import Data.Map as Map
import Data.Maybe (Maybe(..))
import Data.Nullable (toNullable)
import Data.Symbol (SProxy(..))
import Data.Traversable (traverse)
import Data.Tuple (Tuple(..))
import Dispatcher (affAction)
import Dispatcher.React (getProps, getState, modifyState, renderer)
import Effect (Effect)
import Effect.Class (liftEffect)
import Foreign.Object (Object)
import MaterialUI.Styles (withStyles)
import Partial.Unsafe (unsafeCrashWith)
import Polyfills (polyfill)
import React (ReactElement, component, unsafeCreateLeafElement)
import React as R
import Search.ItemResult (ItemSelection)
import Search.OrderControl (orderControl)
import Search.OwnerControl (ownerControl)
import Search.ResultDisplay (renderResults)
import Search.SearchControl (Placement(..), SearchControl)
import Search.SearchLayout (searchLayout)
import Search.WithinLastControl (withinLastControl)
import SearchPage (searchStrings)
import Selection.ReturnResult (ReturnData, decodeReturnData, executeReturn)
import Template (renderMain, rootTag, template', templateDefaults)

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
  -- fc <- facetControl $ FacetSetting {name:"Name", path:"/item/name"}
  let 
    d = eval >>> affAction this
    _selections = prop (SProxy :: SProxy "selections")

    courseControl :: SearchControl
    courseControl {} = do 
      {selection} <- R.getProps this
      {selectedFolder,selections} <- R.getState this
      let rendered = (courseStructure <<< {selectedFolder, 
            selections, 
            onSelectFolder: d <<< SelectFolder, structure: _}) <$> selection.courseData.structure
      pure { chips:[], render: Array.fromFoldable $ Tuple Selections <$> rendered }

    searchControls = [orderControl, oc,  withinLastControl, renderResults _ {onSelect = Just $ d <<< SelectionMade}, courseControl]


    render {props:{classes}} = 
      let renderTemplate {queryBar,content} = rootTag classes.root [ content ]
      in searchLayout {searchControls, strings: searchStrings, renderTemplate}

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
    root: {}
  }

main :: Effect Unit
main = do
  polyfill
  let js  = jsonParser selectionJson
      s = either unsafeCrashWith identity $ js >>= decodeSelection
  renderMain $ selectSearch {selection:s}