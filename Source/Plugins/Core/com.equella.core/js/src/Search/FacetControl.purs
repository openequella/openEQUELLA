module OEQ.Search.FacetControl where

import Prelude

import Control.Monad.Trans.Class (lift)
import Data.Argonaut (class DecodeJson, Json, decodeJson, fromBoolean, fromObject, toObject, (.?))
import Data.Either (either)
import Data.Lens (set, view)
import Data.Lens.At (at)
import Data.Maybe (Maybe(..), fromMaybe)
import Data.String (joinWith)
import Data.Tuple (Tuple(..))
import Data.Unfoldable as Array
import Dispatcher (affAction)
import Dispatcher.React (getProps, modifyState, renderer)
import Effect (Effect)
import Effect.Class.Console (log)
import Effect.Uncurried (mkEffectFn2)
import Foreign.Object (Object)
import Foreign.Object as Object
import MaterialUI.Checkbox (checkbox')
import MaterialUI.Icon (icon_)
import MaterialUI.ListItemText (listItemText')
import Network.HTTP.Affjax (get)
import Network.HTTP.Affjax.Response (json)
import OEQ.Data.Facet (FacetSetting)
import OEQ.Environment (baseUrl)
import OEQ.Search.SearchControl (Chip(..), ControlParams, Placement(..), SearchControl)
import OEQ.Search.SearchQuery (Query, QueryParam(..), _params, searchQueryParams)
import OEQ.UI.CheckList (checkList)
import OEQ.UI.SearchFilters (filterSection)
import OEQ.Utils.QueryString (queryString)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (text)
import React.DOM as D

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
  facet :: FacetSetting
  | ControlParams
}

type State = {
  searching :: Boolean,
  searchResults :: Maybe FacetResults
}

data Command = Search | UpdatedProps Query

initialState :: State
initialState = {searching:false, searchResults:Nothing}

selections :: String -> Query -> Object Json
selections path q = fromMaybe Object.empty $ do 
  pathData <- view (at path) q.params
  toObject pathData.data

clausesFor :: String -> Object Json -> Maybe String
clausesFor path vals | not Object.isEmpty vals = Just $ "(" <> (joinWith " OR " $ clause <$> Object.keys vals) <> ")"
  where clause term = "/xml" <> path <> " = " <> "'" <> term <> "'"
clausesFor _ _ = Nothing

updateValue :: Boolean -> String -> String -> Query -> Query
updateValue c path term q = 
  let _sel = selections path q 
      sel = (if c then flip Object.insert (fromBoolean true) else Object.delete) term _sel
  in q {params = set (at path) (Just {data:fromObject sel, value: XPath $ Array.fromMaybe $ clausesFor path sel}) q.params }

facetDisplay :: Props -> ReactElement
facetDisplay = unsafeCreateLeafElement $ component "FacetDisplay" $ \this -> do
  let
    d = eval >>> affAction this

    componentDidUpdate {query} _ _ = d $ UpdatedProps query
    componentDidMount = d Search

    render {state: {searchResults}, props:{facet:{title,node}, updateQuery, query}} =
      let 
        selectedTerms = selections node query
        renderResults (Just (FacetResults results)) = checkList {entries: result <$> results}
            where
            result (FacetResult {term,count}) = {
              checkbox: \{classes} -> checkbox' {classes, checked: Object.member term selectedTerms, 
                onChange: mkEffectFn2 \e c -> updateQuery $ updateValue c node term},
              text: \{className} -> listItemText' {className, primary: term, secondary: show count}
            }
        renderResults _ = D.div' []
      in filterSection {name:title, icon: icon_ [text "view_list"] } [
          renderResults searchResults
      ]

    searchWith query = do
      modifyState _ {searching=true}
      {facet:{node}} <- getProps
      let withoutOurs = set (_params <<< at node) Nothing query
      result <- lift $ get json $ baseUrl <> "api/search/facet?" <> 
        (queryString $ [Tuple "nodes" node] <> 
        searchQueryParams withoutOurs)
      either log (\r -> modifyState _ {searchResults=Just r}) $ decodeJson result.response

    eval Search = do
      {query} <- getProps
      searchWith query
    eval (UpdatedProps oldQuery) = do
      {query} <- getProps
      if oldQuery /= query then searchWith query else pure unit

  pure {
    state:initialState, 
    componentDidMount, 
    componentDidUpdate, 
    render: renderer render this
  }

facetControl :: FacetSetting -> Placement  -> SearchControl
facetControl setting@{title,node} placement = 
  let renderer {query,updateQuery,results} = do 
        let mkChip value = Chip {
              label: title <> ": " <> value, 
              onDelete: updateQuery $ updateValue false node value
            }
        pure {
          render: [Tuple placement $ facetDisplay {facet:setting, query,updateQuery,results}], 
          chips: mkChip <$> (Object.keys $ selections node query)
        }
  in renderer