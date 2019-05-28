module OEQ.SelectionUI.Main where

import Prelude

import Common.Strings (languageStrings)
import Control.Monad.Except (except, runExceptT)
import Control.Monad.Trans.Class (lift)
import Data.Array as Array
import Data.Either (either)
import Data.Lens (over)
import Data.Lens.At (at)
import Data.Lens.Record (prop)
import Data.Map (Map, empty)
import Data.Maybe (Maybe(..), fromMaybe)
import Data.Nullable (Nullable)
import Data.Symbol (SProxy(..))
import Data.Tuple (Tuple(..))
import Dispatcher (affAction)
import Dispatcher.React (getProps, getState, modifyState, renderer)
import Effect (Effect)
import Effect.Class (liftEffect)
import Effect.Uncurried (mkEffectFn1)
import MaterialUI.AppBar (appBar)
import MaterialUI.Button (button)
import MaterialUI.Enums (headline, inherit, sticky)
import MaterialUI.Enums as Enum
import MaterialUI.Styles (withStyles)
import MaterialUI.Toolbar (toolbar_)
import MaterialUI.Typography (typography)
import OEQ.Data.Error (ErrorResponse)
import OEQ.Data.Selection (SelectionData, findDefaultFolder)
import OEQ.MainUI.TSRoutes (toLocation)
import OEQ.Search.ItemResult (ItemSelection, Result(..), itemResultOptions)
import OEQ.Search.OrderControl (orderControl)
import OEQ.Search.OwnerControl (ownerControl)
import OEQ.Search.ResultDisplay (renderResults)
import OEQ.Search.SearchControl (Placement(..), SearchControl)
import OEQ.Search.SearchLayout (searchLayout)
import OEQ.Search.SearchQuery (blankQuery)
import OEQ.Search.WithinLastControl (withinLastControl)
import OEQ.SelectionUI.CourseStructure (courseStructure)
import OEQ.SelectionUI.ReturnResult (addSelection, callReturn, removeSelection)
import OEQ.SelectionUI.Routes (SelectionPage(..), SelectionRoute(..), SessionParams, pushSelectionRoute, selectionPageMatch)
import OEQ.UI.Common (rootTag)
import OEQ.UI.ItemSummary.ViewItem (viewItem)
import OEQ.UI.Layout (dualPane)
import OEQ.UI.MessageInfo (messageInfo)
import Partial.Unsafe (unsafeCrashWith)
import React (ReactElement, component, unsafeCreateLeafElement)
import React as R
import React.DOM (text)
import React.DOM as RD
import React.DOM.Dynamic (div')
import React.DOM.Props (key)
import Routing (match)

foreign import selectionJson :: {selection::String, integration::Nullable String}

data Command = Init 
  | ChangeRoute SelectionRoute 
  | SelectFolder String 
  | SelectionMade ItemSelection 
  | RemoveSelection String ItemSelection
  | ReturnSelections 
  | UpdateTitle String
  | Errored ErrorResponse
  | Redirected {href::String, external::Boolean}
  | CloseError

type State = {
  selectedFolder :: String, 
  selections :: Map String (Array ItemSelection), 
  route :: Maybe SelectionRoute,
  title :: Maybe String, 
  error :: Maybe ErrorResponse, 
  errorOpen :: Boolean
}

selectSearch :: {sessionParams :: SessionParams, selection::SelectionData} -> ReactElement
selectSearch = unsafeCreateLeafElement $ withStyles styles $ component "SelectSearch" $ \this -> do 
  oc <- ownerControl Filters
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
        onRemove: \fid -> d <<< RemoveSelection fid,
        structure: fromMaybe blankStructure selection.courseData.structure}, 
      button {onClick: d ReturnSelections} [ text "Return" ]
    ]

    courseControl :: SearchControl
    courseControl {} = do 
      {selection} <- R.getProps this
      {selectedFolder,selections} <- R.getState this
      pure { chips:[], render: [Tuple Selections $ renderStructure selection {selectedFolder,selections}] }

    selectionClicker :: SelectionRoute -> {href::String, onClick:: Effect Unit}
    selectionClicker = unsafeCrashWith "TODO"

    searchControls = [orderControl Filters, oc, withinLastControl Filters, 
      renderResults $ do 
        {sessionParams} <- R.getProps this
        pure $ \r@Result {uuid,version} -> 
          let {href,onClick} = selectionClicker $ Route sessionParams (ViewItem uuid version)
          in (itemResultOptions (toLocation href) r) {onSelect = Just $ d <<< SelectionMade}, 
      courseControl]


    renderError {error:Just {error:title}, errorOpen} = [ messageInfo {open:errorOpen, variant: Enum.error,
      onClose: d CloseError, title  } ]
    renderError _ = []


    render {props:{classes,selection,sessionParams:sp}, 
            state:s@{title, 
              route: Just (Route params r), 
              selectedFolder,selections}
            } = case r of 
      Search -> let 
        renderTemplate {content} = rootTag classes.root $ [ 
          appBar {position: sticky} [ 
            toolbar_ [
              -- TODO
            ]
          ],
          content 
        ] <> renderError s
        in searchLayout {searchControls, initialQuery:blankQuery, strings: languageStrings.searchpage, renderTemplate, 
            updateQueryBar: mkEffectFn1 \_ -> pure unit}
      LegacySelectionPage page -> 
        dualPane {
            left: [
              -- legacyContent {
              --   page, 
              --   contentUpdated: mkEffectFn1 \_ -> pure unit, 
              --   userUpdated: pure unit,
              --   redirected: mkEffectFn1 $ d <<< Redirected,
              --   onError: mkEffectFn1 $ d <<< Errored <<< _.error
              -- } 
            ],  
            right:[
              renderStructure selection {selectedFolder, selections}
            ]
        }
      ViewItem uuid version -> rootTag classes.root $ [
          appBar {position: sticky} [
            toolbar_ [
              typography {variant: headline, color: inherit} [text $ fromMaybe "" title]
            ]
          ],
          dualPane {
            left: [
              viewItem {uuid,version, 
                onError: mkEffectFn1 $ d <<< Errored,
                onSelect: Just $ d <<< SelectionMade,
                courseCode: selection.integration >>= _.courseInfoCode}
            ],  
            right:[
              renderStructure selection {selectedFolder, selections}
            ]
          }
      ] <> renderError s
    render _ = div' [text "NOTHING"]

    eval = case _ of 
      UpdateTitle t -> modifyState _ {title = Just t}
      SelectFolder f -> modifyState _ {selectedFolder = f}
      CloseError -> modifyState _ {errorOpen=false}
      ChangeRoute r -> modifyState _ {route=Just r}

      RemoveSelection folderId is -> do
        {sessionParams} <- getProps
        modifyState $ over (_selections <<< at folderId) (map $ Array.delete is)
        maybeError =<< (lift $ removeSelection sessionParams folderId is)

      SelectionMade sel -> do 
        let 
          addToFolder (Just f) = Just $ (f <> [sel])
          addToFolder Nothing = Just [sel]
        {selectedFolder} <- getState
        modifyState $ over (_selections <<< at selectedFolder) addToFolder
        {sessionParams} <- getProps
        maybeError =<< (lift $ addSelection sessionParams selectedFolder sel)

      Redirected {href} -> do 
        {sessionParams} <- getProps
        void $ runExceptT $ do 
          page <- except $ match selectionPageMatch href
          liftEffect $ pushSelectionRoute $ Route sessionParams page

      Errored error -> modifyState _ {error = Just error}

      ReturnSelections -> do
        {sessionParams} <- getProps
        liftEffect $ callReturn sessionParams

      Init -> do 
        selectDefaultFolder
        -- liftEffect $ do
        --   bp <- either (throw <<< show) pure basePath
          -- let baseStripped p = fromMaybe p $ stripPrefix (Pattern $ bp) p
          -- void $ matchesWith (matchSelection <<< baseStripped) (\_ -> affAction this <<< eval <<< ChangeRoute) globalNav
       
      where 
        maybeError = either (\error -> modifyState _ {error = Just error}) pure

    selectDefaultFolder = do 
      {selection} <- getProps 
      case selection of 
        {courseData:{structure: Just s}} | Just defaultFolder <- findDefaultFolder s -> eval $ SelectFolder defaultFolder 
        _ -> pure unit 

  pure {
    render: renderer render this, 
    state: { 
      selectedFolder:"", 
      selections:empty, 
      route: Nothing, 
      title: Nothing, 
      error: Nothing, 
      errorOpen: false
    } :: State,  
    componentDidMount: d Init
  }

  where 
  styles theme = {
    root: {}
  }

main :: Effect Unit
main = do
  pure unit
  -- polyfill
  -- startHeartbeat
  -- bp <- either (throw <<< show) pure basePath
  -- loc <- window >>= location
  -- pagePath <- pathname loc
  -- let baseStripped p = fromMaybe p $ stripPrefix (Pattern $ bp) p
  --     (Route sp _) = unsafePartial $ fromJust $ matchSelection (baseStripped pagePath)
  -- let decoded = do 
  --       sjs <- jsonParser selectionJson.selection
  --       ijs <- traverse jsonParser (toMaybe selectionJson.integration)
  --       decodeSelection sjs ijs
  -- either unsafeCrashWith (\s -> renderMain $ selectSearch {selection:s, sessionParams: sp}) decoded
