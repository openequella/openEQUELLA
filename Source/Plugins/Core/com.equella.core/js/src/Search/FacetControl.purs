module Search.FacetControl where

import Prelude

import CheckList (checkList)
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
import EQUELLA.Environment (baseUrl)
import Effect (Effect)
import Effect.Class.Console (log)
import Effect.Uncurried (mkEffectFn2)
import Foreign.Object (Object)
import Foreign.Object as Object
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
import Search.SearchControl (Chip(..), ControlParams, Placement(..), SearchControl)
import Search.SearchQuery (Query, QueryParam(..), searchQueryParams)
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

    render {state: {searchResults}, props:{facet:(FacetSetting {name,path}), updateQuery, query}} =
      let 
        selectedTerms = selections path query
        renderResults (Just (FacetResults results)) = checkList {entries: result <$> results}
            where
            result (FacetResult {term,count}) = {
              checkProps : [checked $ Object.member term selectedTerms, onChange $ (mkEffectFn2 \e c -> updateQuery $ updateValue c path term)],
              textProps : [primary term, secondary $ show count]
            }
        renderResults _ = D.div' []
      in filterSection {name, icon: icon_ [text "view_list"] } [
          renderResults searchResults
      ]

    searchWith query = do
      modifyState _ {searching=true}
      {facet:(FacetSetting {path})} <- getProps
      result <- lift $ get json $ baseUrl <> "api/search/facet?" <> (queryString $ [Tuple "nodes" path] <> searchQueryParams query)
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

facetControl :: FacetSetting -> Effect SearchControl
facetControl setting@(FacetSetting {name,path}) = do 
  pure $ \{query,updateQuery,results} -> do 
    let mkChip value = Chip {
          label: name <> ": " <> value, 
          onDelete: updateQuery $ updateValue false path value
        }
    pure {
      render: [Tuple Filters $ facetDisplay {facet:setting, query,updateQuery,results}], 
      chips: mkChip <$> (Object.keys $ selections path query)
    }