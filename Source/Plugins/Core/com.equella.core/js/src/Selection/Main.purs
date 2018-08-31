module Selection.Main where

import Prelude

import Control.Monad.Trans.Class (lift)
import Course.Structure (CourseStructure, courseStructure, decodeStructure, findDefaultFolder)
import Data.Argonaut (Json, decodeJson, jsonParser, (.??))
import Data.Either (Either, either)
import Data.Lens (over)
import Data.Lens.At (at)
import Data.Lens.Record (prop)
import Data.Map (Map, empty)
import Data.Map as Map
import Data.Maybe (Maybe(..), fromJust, fromMaybe, maybe)
import Data.Nullable (Nullable, toMaybe)
import Data.String (Pattern(..), stripPrefix)
import Data.Symbol (SProxy(..))
import Data.Traversable (traverse)
import Data.Tuple (Tuple(..))
import Debug.Trace (traceM)
import Dispatcher (affAction)
import Dispatcher.React (getProps, getState, modifyState, renderer)
import EQUELLA.Environment (basePath, baseUrl, startHearbeat)
import Effect (Effect)
import Effect.Class (liftEffect)
import Effect.Exception (throw)
import Effect.Uncurried (mkEffectFn1)
import Foreign.Object (Object)
import Global.Unsafe (unsafeEncodeURIComponent)
import ItemSummary.ViewItem (viewItem)
import MaterialUI.AppBar (appBar, position, sticky)
import MaterialUI.Button (button)
import MaterialUI.CircularProgress (inherit)
import MaterialUI.Properties (color, onClick, variant)
import MaterialUI.Styles (withStyles)
import MaterialUI.TextStyle (headline)
import MaterialUI.Toolbar (toolbar)
import MaterialUI.Typography (typography)
import Network.HTTP.Affjax as Ajax
import Network.HTTP.Affjax.Response as Resp
import Partial.Unsafe (unsafeCrashWith, unsafePartial)
import Polyfills (polyfill)
import React (ReactElement, component, unsafeCreateLeafElement)
import React as R
import React.DOM (text)
import React.DOM as RD
import React.DOM.Dynamic (div')
import React.DOM.Props (key)
import Routes (globalNav)
import Routing.PushState (matchesWith)
import Search.ItemResult (ItemSelection, Result(..), itemResultOptions)
import Search.OrderControl (orderControl)
import Search.OwnerControl (ownerControl)
import Search.ResultDisplay (renderResults)
import Search.SearchControl (Placement(..), SearchControl)
import Search.SearchLayout (searchLayout)
import Search.WithinLastControl (withinLastControl)
import SearchPage (searchStrings)
import Selection.ReturnResult (ReturnData, addSelection, callReturn, decodeReturnData)
import Selection.Route (SelectionPage(..), SelectionRoute(..), SessionParams(..), matchSelection, selectionClicker, withPage)
import Template (renderMain, rootTag)
import UIComp.DualPane (dualPane)
import Web.HTML (window)
import Web.HTML.Location (pathname)
import Web.HTML.Window (location)

foreign import selectionJson :: {selection::String, integration::Nullable String}

type CourseData = {
  structure :: Maybe CourseStructure
}

type IntegrationData = {

}

type SelectionData = {
  courseData :: CourseData,
  returnData :: Maybe ReturnData
}

decodeCourseData :: Object Json -> Either String CourseData
decodeCourseData o = do 
  structure <- o .?? "structure" >>= traverse decodeStructure
  pure {structure}

decodeSelection :: Json -> Maybe Json -> Either String SelectionData 
decodeSelection s i = do 
  os <- decodeJson s 
  courseData <- decodeCourseData os
  oi <- traverse decodeJson i
  returnData <- traverse (decodeReturnData os) oi
  pure {courseData,returnData}

data Command = Init 
  | ChangeRoute SelectionRoute 
  | SelectFolder String 
  | SelectionMade ItemSelection 
  | ReturnSelections 
  | UpdateTitle String

type State = {
  selectedFolder :: String, 
  selections :: Map String (Array ItemSelection), 
  route :: Maybe SelectionRoute, 
  title :: Maybe String
}

selectSearch :: {sessionParams :: SessionParams, selection::SelectionData} -> ReactElement
selectSearch = unsafeCreateLeafElement $ withStyles styles $ component "SelectSearch" $ \this -> do 
  oc <- ownerControl
  -- fc <- facetControl $ FacetSetting {name:"Name", path:"/item/name"}
  let 
    d = eval >>> affAction this
    _selections = prop (SProxy :: SProxy "selections")

    blankStructure = {name: "Selections", folders:[]}

    renderStructure selection {selectedFolder, selections} = RD.div [key "courseStructure"] [ 
      courseStructure $ {
        selectedFolder, 
        selections,  
        onSelectFolder: d <<< SelectFolder, 
        structure: fromMaybe blankStructure selection.courseData.structure}, 
      button [ onClick $ \_ -> d ReturnSelections] [ text "Return" ]
    ]

    courseControl :: SearchControl
    courseControl {} = do 
      {selection} <- R.getProps this
      {selectedFolder,selections} <- R.getState this
      pure { chips:[], render: [Tuple Selections $ renderStructure selection {selectedFolder,selections}] }

    searchControls = [orderControl, oc, withinLastControl, 
      renderResults $ do 
        {route} <- R.getState this
        pure $ \r@Result {uuid,version} -> 
          let {href,onClick} = selectionClicker $ withPage route (ViewItem uuid version)
          in (itemResultOptions {href, onClick} r) {onSelect = Just $ d <<< SelectionMade}, 
      courseControl]


    render {props:{classes,selection}, state:{title, route: Just (Route params r), selectedFolder,selections}} = case r of 
      Search ->
        let renderTemplate {queryBar,content} = rootTag classes.root [ 
          appBar [position sticky] [ 
            toolbar [] [
              queryBar
            ]
          ],
          content
        ]
        in searchLayout {searchControls, strings: searchStrings, renderTemplate}
      ViewItem uuid version -> rootTag classes.root [ 
          appBar [position sticky] [
            toolbar [] [
              typography [variant headline, color inherit] [text $ fromMaybe "" title]
            ]
          ],
          dualPane {
            left: [viewItem {uuid,version, 
                titleUpdate: mkEffectFn1 $ d <<< UpdateTitle, 
                onError: mkEffectFn1 traceM,
                onSelect: Just $ d <<< SelectionMade }],  
            right:[
              renderStructure selection {selectedFolder, selections}
            ]
          }
      ]
    render _ = div' [text "NOTHING"]

    eval = case _ of 
      UpdateTitle t -> modifyState _ {title = Just t}
      SelectFolder f -> modifyState _ {selectedFolder = f}
      SelectionMade sel -> do 
        let 
          addToFolder (Just f) = Just $ (f <> [sel])
          addToFolder Nothing = Just [sel]
        {selectedFolder} <- getState
        modifyState $over (_selections <<< at selectedFolder) addToFolder
        {sessionParams} <- getProps
        lift $ addSelection sessionParams selectedFolder sel

      ReturnSelections -> do
        {sessionParams} <- getProps
        liftEffect $ callReturn sessionParams
      Init -> do 
        selectDefaultFolder
        liftEffect $ do
          bp <- either (throw <<< show) pure basePath
          let baseStripped p = fromMaybe p $ stripPrefix (Pattern $ bp) p
          void $ matchesWith (matchSelection <<< baseStripped) (\_ -> affAction this <<< eval <<< ChangeRoute) globalNav
      ChangeRoute r -> modifyState _ {route=Just r}

    selectDefaultFolder = do 
      {selection} <- getProps 
      case selection of 
        {courseData:{structure: Just s}} | Just defaultFolder <- findDefaultFolder s -> eval $ SelectFolder defaultFolder 
        _ -> pure unit 

  pure {render: renderer render this, 
        state: { 
          selectedFolder:"", 
          selections:empty, 
          route: Nothing, 
          title: Nothing
        } :: State,  componentDidMount: d Init}

  where 
  styles theme = {
    root: {}
  }

main :: Effect Unit
main = do
  polyfill
  startHearbeat
  bp <- either (throw <<< show) pure basePath
  loc <- window >>= location
  pagePath <- pathname loc
  let baseStripped p = fromMaybe p $ stripPrefix (Pattern $ bp) p
      (Route sp _) = unsafePartial $ fromJust $ matchSelection (baseStripped pagePath)
  let decoded = do 
        sjs <- jsonParser selectionJson.selection
        ijs <- traverse jsonParser (toMaybe selectionJson.integration)
        decodeSelection sjs ijs
  either unsafeCrashWith (\s -> renderMain $ selectSearch {selection:s, sessionParams: sp}) decoded
