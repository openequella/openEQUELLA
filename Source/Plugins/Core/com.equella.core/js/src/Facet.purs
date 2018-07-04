module Facet where

import Prelude

import CheckList (checkList)
import Control.Monad.Trans.Class (lift)
import Data.Argonaut (class DecodeJson, decodeJson, (.?))
import Data.Either (either)
import Data.Maybe (Maybe(..))
import Data.Set (Set, member)
import Data.Tuple (Tuple(..))
import Dispatcher (affAction)
import Dispatcher.React (getProps, modifyState, renderer)
import EQUELLA.Environment (baseUrl)
import Effect (Effect)
import Effect.Class (liftEffect)
import Effect.Class.Console (log)
import Effect.Uncurried (mkEffectFn2)
import MaterialUI.Icon (icon_)
import MaterialUI.ListItemText (primary, secondary)
import MaterialUI.Properties (onChange)
import MaterialUI.SwitchBase (checked)
import Network.HTTP.Affjax (get)
import Network.HTTP.Affjax.Response (json)
import QueryString (queryString)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (text)
import React.DOM as D
import SearchFilters (filterSection)
import Settings.UISettings (FacetSetting(..))

newtype FacetResult = FacetResult {term::String, count::Int}
newtype FacetResults = FacetResults (Array FacetResult)

instance frDecode :: DecodeJson FacetResult where
  decodeJson v = do
    o <- decodeJson v
    term <- o .? "term"
    count <- o .? "count"
    pure $ FacetResult {term,count}

instance frsDecode :: DecodeJson FacetResults where
  decodeJson v = do
    o <- decodeJson v
    results <- o .? "results"
    pure $ FacetResults results

type Props = {
  facet :: FacetSetting,
  onClickTerm :: String -> Effect Unit,
  selectedTerms :: Set String,
  query :: Array (Tuple String String)
}

type State = {
  searching :: Boolean,
  searchResults :: Maybe FacetResults
}

data Command = Search | UpdatedProps (Array (Tuple String String))

initialState :: State
initialState = {searching:false, searchResults:Nothing}

facetDisplay :: Props -> ReactElement
facetDisplay = unsafeCreateLeafElement $ component "FacetDisplay" $ \this -> do
  let
    d = eval >>> affAction this
    componentDidUpdate {query} _ _ = d $ UpdatedProps query
    componentDidMount = d Search
    render {state: {searchResults}, props:{facet:(FacetSetting {name}), onClickTerm,selectedTerms}} = 
      filterSection {name, icon: icon_ [text "view_list"] } [
        renderResults searchResults
      ]
      where
      renderResults (Just (FacetResults results)) = checkList {entries: result <$> results}
        where 
        result (FacetResult {term,count}) = {
          checkProps : [checked $ member term selectedTerms, onChange $ (mkEffectFn2 \e c -> onClickTerm term)],
          textProps : [primary term, secondary $ show count]
        }
      renderResults _ = D.div' []

    searchWith query = do
      modifyState _ {searching=true}
      {facet:(FacetSetting {path})} <- getProps
      result <- lift $ get json $ baseUrl <> "api/search/facet?" <> (queryString $ [Tuple "nodes" path] <> query)
      either (lift <<< liftEffect <<< log) (\r -> modifyState _ {searchResults=Just r}) $ decodeJson result.response

    eval Search = do
      {query} <- getProps
      searchWith query
    eval (UpdatedProps oldQuery) = do
      {query} <- getProps
      if oldQuery /= query then searchWith query else pure unit
  pure {state:initialState, componentDidMount, componentDidUpdate, render: renderer render this}
