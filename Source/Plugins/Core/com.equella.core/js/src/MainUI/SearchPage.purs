module OEQ.MainUI.SearchPage where

import Prelude hiding (div)

import Common.Strings (languageStrings)
import Control.Monad.Trans.Class (lift)
import Data.Array (mapMaybe)
import Data.Either (either)
import Data.Foldable (fold)
import Data.Function.Uncurried (runFn2)
import Data.Maybe (Maybe(..))
import Data.Nullable (toNullable)
import Data.Traversable (foldr, sequence)
import Data.Tuple (Tuple(..))
import Debug.Trace (traceM)
import Dispatcher (affAction)
import Dispatcher.React (modifyState, stateRenderer)
import Effect.Class (liftEffect)
import Effect.Uncurried (mkEffectFn1)
import Foreign.Object (toArrayWithKey)
import Foreign.Object as Object
import MaterialUI.Styles (withStyles)
import OEQ.API.Searches (getPageConfig)
import OEQ.MainUI.TSRoutes (TemplateUpdateCB, routes, runTemplateUpdate)
import OEQ.MainUI.Template (templateDefaults)
import OEQ.Search.Controls (controlFromConfig, queryFromConfig)
import OEQ.Search.ItemResult (Result(..), itemResultOptions)
import OEQ.Search.ResultDisplay (renderResults)
import OEQ.Search.SearchControl (SearchControl, placementFromString)
import OEQ.Search.SearchLayout (searchLayout)
import OEQ.Search.SearchQuery (Query, blankQuery)
import React (ReactClass, ReactElement)
import React as R
import React.DOM (div')

type State = {
  config :: Maybe (Tuple Query (Array SearchControl))
}

data Command = InitSearch | UpdateQueryBar ReactElement

initialState :: State
initialState = {
    config: Nothing
}

searchPageClass :: ReactClass {updateTemplate :: TemplateUpdateCB }
searchPageClass = withStyles styles $ R.component "SearchPage" $ \this -> do
  let
    searchStrings = languageStrings.searchpage
    d = eval >>> affAction this

    renderTemplate {content} = content
    render {config: Just (Tuple query searchControls)} = searchLayout 
      {searchControls, initialQuery: query, updateQueryBar: mkEffectFn1 $ d <<< UpdateQueryBar,
        strings:searchStrings, renderTemplate }
    render _ = div' [ ]
    eval = case _ of 
      UpdateQueryBar e -> runTemplateUpdate _ {titleExtra = toNullable $ Just e}
      InitSearch -> do
        runTemplateUpdate $ \t -> templateDefaults "" 
        sc <- lift $ getPageConfig "search"
        sc # either traceM \{sections} -> do 
            let query = foldr queryFromConfig blankQuery $ join $ Object.values sections
                controlsForSection p configs = sequence $ mapMaybe (controlFromConfig $ placementFromString p) configs
            configedControls <- liftEffect $ fold $ toArrayWithKey controlsForSection sections
            modifyState _ {config = Just $ Tuple query $ configedControls <> [
              renderResults $ pure \r@Result {uuid,version} -> itemResultOptions (runFn2 routes."ViewItem".to uuid version) r
            ]}
  
  pure {render: stateRenderer render this, state:initialState, componentDidMount: d InitSearch}
  where 
  styles theme = {
  }
