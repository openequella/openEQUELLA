module Facet where

import Prelude

import Control.Monad.Eff (Eff)
import Control.Monad.Eff.Class (liftEff)
import Control.Monad.Eff.Console (log)
import Control.Monad.Eff.Uncurried (mkEffFn2)
import Control.Monad.State (modify)
import Control.Monad.Trans.Class (lift)
import Data.Argonaut (class DecodeJson, decodeJson, (.?))
import Data.Either (either)
import Data.Maybe (Maybe(..))
import Data.Set (Set, member)
import Dispatcher (dispatch)
import Dispatcher.React (ReactProps(ReactProps), createLifecycleComponent, didMount, getProps, modifyState)
import EQUELLA.Environment (baseUrl)
import Global (encodeURIComponent)
import MaterialUI.Checkbox (checkbox)
import MaterialUI.List (disablePadding, list)
import MaterialUI.ListItem (disableGutters, listItem)
import MaterialUI.ListItemText (listItemText, primary, secondary)
import MaterialUI.Properties (className, classes_, variant)
import MaterialUI.Styles (withStyles)
import MaterialUI.SwitchBase (checked)
import MaterialUI.TextField (onChange)
import MaterialUI.TextStyle (subheading)
import MaterialUI.Typography (typography)
import Network.HTTP.Affjax (get)
import React (ReactElement, createFactory)
import React.DOM as D
import React.DOM.Props as DP
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

type Props eff = {
  facet :: FacetSetting,
  onClickTerm :: String -> Eff eff Unit,
  selectedTerms :: Set String,
  query :: String
}

type State = {
  searching :: Boolean,
  searchResults :: Maybe FacetResults
}

data Command = Search | UpdatedProps String

initialState :: State
initialState = {searching:false, searchResults:Nothing}

facetDisplay :: forall eff. Props eff -> ReactElement
facetDisplay = createFactory (withStyles styles $ createLifecycleComponent (do
    didMount Search
    modify _ { componentWillReceiveProps = \c {query} -> dispatch eval c (UpdatedProps query) }
    ) initialState render eval)
  where

  styles theme = {
    container: {
      padding: theme.spacing.unit
    },
    reallyDense: {
      padding: 0
    },
    smallerCheckbox: {
      height: theme.spacing.unit * 4
    },
    facetText: {
      display: "flex",
      justifyContent: "space-between"
    }
  }

  render {searchResults} (ReactProps {facet:(FacetSetting {name}), classes,onClickTerm,selectedTerms}) = D.div [DP.className classes.container] [
    typography [variant subheading] [ D.text name ],
    renderResults searchResults
  ]
    where
    renderResults (Just (FacetResults results)) = list [disablePadding true] $ result <$> results
      where result (FacetResult {term,count}) = listItem [classes_ {default:classes.reallyDense}, disableGutters true] [
        checkbox [classes_ {default:classes.smallerCheckbox}, checked $ member term selectedTerms, onChange $ (mkEffFn2 \e c -> onClickTerm term)],
        listItemText [className classes.facetText, primary term, secondary $ show count]
      ]
    renderResults _ = D.div' []

  searchWith query = do
    modifyState _ {searching=true}
    {facet:(FacetSetting {path})} <- getProps
    result <- lift $ get $ baseUrl <> "api/search/facet?where=" <> encodeURIComponent query <> "&nodes=" <> encodeURIComponent path
    either (lift <<< liftEff <<< log) (\r -> modifyState _ {searchResults=Just r}) $ decodeJson result.response

  eval Search = do
    {query} <- getProps
    searchWith query
  eval (UpdatedProps newQuery) = do
    {query} <- getProps
    if newQuery /= query then searchWith newQuery else pure unit
