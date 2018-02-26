module SearchPage where

import Prelude

import Control.Monad.Aff.Console (log)
import Control.Monad.Eff (Eff)
import Control.Monad.Eff.Console (CONSOLE)
import Control.Monad.Trans.Class (lift)
import DOM (DOM)
import Data.Argonaut (class DecodeJson, decodeJson, (.?), (.??))
import Data.Array (filter, findMap, fromFoldable, intercalate, length, mapMaybe, mapWithIndex, singleton)
import Data.Either (either)
import Data.Maybe (Maybe(Just, Nothing), fromMaybe)
import Data.Newtype (unwrap)
import Data.Set (isEmpty)
import Data.Set as S
import Data.StrMap (lookup)
import Data.StrMap as SM
import Data.String (joinWith)
import Data.Tuple (Tuple(..), fst)
import Dispatcher (DispatchEff(DispatchEff))
import Dispatcher.React (ReactProps(ReactProps), createLifecycleComponent, didMount, getState, modifyState)
import EQUELLA.Environment (baseUrl)
import Facet (facetDisplay)
import Global (encodeURIComponent)
import MaterialUI.Chip (chip, onDelete)
import MaterialUI.Colors (fade)
import MaterialUI.Divider as C
import MaterialUI.Icon (icon_)
import MaterialUI.List (disablePadding, list, list_)
import MaterialUI.ListItem (button, disableGutters, divider, listItem)
import MaterialUI.ListItemText (disableTypography, listItemText, primary, secondary)
import MaterialUI.Paper (elevation, paper)
import MaterialUI.PropTypes (handle)
import MaterialUI.Properties (className, classes_, color, component, mkProp, style, variant)
import MaterialUI.Styles (withStyles)
import MaterialUI.TextField (label)
import MaterialUI.TextStyle as TS
import MaterialUI.Typography (textSecondary, typography)
import Network.HTTP.Affjax (AJAX, get)
import React (ReactElement, createFactory)
import React.DOM as D
import React.DOM.Props as DP
import Settings.UISettings (FacetSetting(..), NewUISettings(..), UISettings(..))
import Template (renderMain, template)
import TimeAgo (timeAgo)
import Unsafe.Coerce (unsafeCoerce)

newtype Attachment = Attachment {thumbnailHref::String}
newtype DisplayField = DisplayField {name :: String, html::String}
newtype Result = Result {name::String, description:: Maybe String, modifiedDate::String,
    displayFields :: Array DisplayField, thumbnail::String, uuid::String, version::Int, attachments::Array Attachment}
newtype SearchResults = SearchResults {start::Int, length::Int, available::Int, results::Array Result}

instance attachDecode :: DecodeJson Attachment where
  decodeJson v = do
    o <- decodeJson v
    links <- o .? "links"
    thumbnailHref <- links .? "thumbnail"
    pure $ Attachment {thumbnailHref}


instance dfDecode :: DecodeJson DisplayField where
  decodeJson v = do
    o <- decodeJson v
    name <- o .? "name"
    html <- o .? "html"
    pure $ DisplayField {name, html}

instance rDecode :: DecodeJson Result where
  decodeJson v = do
    o <- decodeJson v
    nameO <- o .?? "name"
    description <- o .?? "description"
    uuid <- o .? "uuid"
    modifiedDate <- o .? "modifiedDate"
    thumbnail <- o .? "thumbnail"
    df <- o .?? "displayFields"
    version <- o .? "version"
    attachments <- o .? "attachments"
    pure $ Result {uuid,version,name:fromMaybe uuid nameO, description, thumbnail, modifiedDate, displayFields:fromMaybe [] df, attachments}

instance srDecode :: DecodeJson SearchResults where
  decodeJson v = do
    o <- decodeJson v
    start <- o .? "start"
    length <- o .? "length"
    available <- o .? "available"
    results <- o .? "results"
    pure $ SearchResults {start,length,available,results}

type State = {
  searching :: Boolean,
  query :: String,
  facetSettings :: Array FacetSetting,
  facets :: SM.StrMap (S.Set String),
  searchResults :: Maybe SearchResults
}

data Command = InitSearch | Search | QueryUpdate String | ToggledTerm String String

initialState :: State
initialState = {searching:false, query:"", searchResults:Nothing, facets:SM.empty, facetSettings: []}


searchPage :: ReactElement
searchPage = createFactory (withStyles styles $ createLifecycleComponent (didMount InitSearch) initialState render eval) {}
  where

  styles theme = {
    results: {
      flexGrow: 2,
      padding: 16
    },
    refinements: {
      width: "25%",
      maxWidth: "250px",
      marginLeft: 16,
      padding: 16
    },
    searchResultContent: {
      display: "flex",
      marginTop: "8px"
    },
    itemThumb: {
      maxWidth: "88px",
      maxHeight: "66px",
      marginRight: "12px"
    },
    displayNode: {
      padding: 0
    },
    layoutDiv: {
      display: "flex",
      justifyContent: "space-around"
    },
    searchPane: {
      width: "75%"
    },
    queryWrapper: {
      fontFamily: theme.typography.fontFamily,
      position: "relative",
      marginRight: theme.spacing.unit * 2,
      marginLeft: theme.spacing.unit * 2,
      borderRadius: 2,
      background: fade theme.palette.common.white 0.15,
      width: "400px"
    },
    queryIcon: {
      width: theme.spacing.unit * 9,
      height: "100%",
      position: "absolute",
      pointerEvents: "none",
      display: "flex",
      alignItems: "center",
      justifyContent: "center"
    },
    queryField: {
      font: "inherit",
      padding: show theme.spacing.unit <> "px " <> show theme.spacing.unit <> "px "
            <> show theme.spacing.unit <> "px " <> (show $ theme.spacing.unit * 9) <> "px",
      border: 0,
      display: "block",
      verticalAlign: "middle",
      whiteSpace: "normal",
      background: "none",
      margin: 0,
      color: "inherit",
      width: "100%"
    },
    facetContainer: {
      display: "flex",
      flexWrap: "wrap"
    },
    chip: {
      margin: theme.spacing.unit
    }
  }

  whereClause (Tuple node terms) | not isEmpty terms = Just $ "(" <> (joinWith " OR " $ clause <$> S.toUnfoldable terms) <> ")"
    where clause term = "/xml" <> node <> " = " <> "'" <> term <> "'"
  whereClause _ = Nothing

  render {searchResults,query,facets,facetSettings} (ReactProps {classes}) (DispatchEff d) = template {mainContent,titleExtra:Just searchBar}
    where

    facetMap = SM.fromFoldable $ (\fac@(FacetSetting {path}) -> Tuple path fac) <$> facetSettings

    queryWithout exclude = joinWith " AND " $ mapMaybe whereClause $ filter (fst >>> notEq exclude) $ SM.toUnfoldable facets

    mainContent = D.div [DP.className classes.layoutDiv] [
      paper [className classes.results, elevation 4] $ renderResults searchResults,
      paper [className classes.refinements, elevation 4] $ intercalate [C.divider []] $ (makeFacet >>> singleton) <$> facetSettings
    ]

    facetChips = facetChip <$> (allVals =<< SM.toUnfoldable facets)
    allVals (Tuple node s) = {name:fromMaybe node $ unwrap >>> _.name <$> lookup node facetMap, node, value: _} <$> S.toUnfoldable s
    facetChip {name,node,value} = chip [className classes.chip, label $ name <> ": " <> value,
                                          onDelete $ handle $ d \_ -> ToggledTerm node value]

    searchBar = D.div [DP.className classes.queryWrapper] [
      D.div [DP.className classes.queryIcon ] [ icon_ [ D.text "search" ] ],
      D.input [DP._type "text", DP.className classes.queryField, DP.value $ query, DP.onChange $ d \e -> QueryUpdate (unsafeCoerce e).target.value ] []
    ]

    makeFacet details@(FacetSetting {path}) = facetDisplay {facet:details, onClickTerm: d $ ToggledTerm path,
     selectedTerms: fromMaybe S.empty $ SM.lookup path facets, query:queryWithout path }

    renderResults (Just (SearchResults {results,available})) =
      let resultLen = length results
      in [
        typography [variant TS.subheading] [ D.text $ show available <> " results available"],
        D.div [ DP.className classes.facetContainer ] $ facetChips,
        list_ (mapWithIndex (\i -> oneResult $ i /= (resultLen - 1)) results)
      ]
    renderResults Nothing = []

    oneResult showDivider (Result {name,description,displayFields,uuid,version,attachments,modifiedDate}) =
      let descMarkup descText = typography [color textSecondary] [ D.text descText ]
          titleLink = typography [variant TS.title, style {textDecoration:"none", color:"blue"},
                        component "a", mkProp "href" $ baseUrl <> "items/" <> uuid <> "/" <> show version <> "/"] [ D.text name ]
          attachThumb (Attachment {thumbnailHref}) = Just $ D.img [DP.className classes.itemThumb, DP.src thumbnailHref] []
          firstThumb = fromFoldable $ findMap attachThumb attachments
          extraDeets = [
            listItem [classes_ {default: classes.displayNode}, disableGutters true] [
              typography [variant TS.body1] [ D.text "Modified" ],
              typography [component "div", color textSecondary] [
                D.text "\xa0-\xa0",
                timeAgo modifiedDate []
              ]
            ]
          ]
          extraFields = (fieldDiv <$> displayFields) <> extraDeets
          mainContent = D.div [ DP.className classes.searchResultContent ] $ firstThumb <>
            [ D.div' $ fromFoldable (descMarkup <$> description) <> [ list [disablePadding true] extraFields ] ]
      in listItem [button true, divider showDivider] [
          listItemText [ disableTypography true, primary titleLink, secondary mainContent ]
      ]
      where
      fieldDiv (DisplayField {name:n,html}) = listItem [classes_ {default: classes.displayNode}, disableGutters true] [
        typography [variant TS.body1] [ D.text n ],
        typography [component "div", color textSecondary] [ D.div [DP.dangerouslySetInnerHTML {__html: "\xa0-\xa0" <> html}] [] ]
      ]

  searchWith query facets = do
    modifyState _ {searching=true}
    let whereXpath = mapMaybe whereClause $ SM.toUnfoldable facets
    result <- lift $ get $ baseUrl <> "api/search?info=basic,detail,attachment,display&q=" <> encodeURIComponent query
      <> "&where=" <> (encodeURIComponent $ joinWith " AND " whereXpath)
    either (lift <<< log) (\r -> modifyState _ {searchResults=Just r}) $ decodeJson result.response

  toggleFacet node term facMap = SM.insert node (toggle $ fromMaybe S.empty $ SM.lookup node facMap) facMap
    where
    toggle set = if S.member term set then S.delete term set else S.insert term set

  eval InitSearch = do
    {query,facets} <- getState
    searchWith query facets
    result <- lift $ get $ baseUrl <> "api/settings/ui"
    either (lift <<< log) (\(UISettings {newUI:(NewUISettings {facets})}) -> modifyState _ {facetSettings= facets}) $ decodeJson result.response

  eval Search = do
    {query,facets} <- getState
    searchWith query facets

  eval (ToggledTerm node term) = do
    {query,facets} <- getState
    modifyState \s -> s {facets = toggleFacet node term s.facets }
    searchWith query $ toggleFacet node term facets

  eval (QueryUpdate q) = do
    modifyState _ {query=q}
    {facets} <- getState
    searchWith q facets

main :: forall eff. Eff (dom :: DOM, ajax :: AJAX, console::CONSOLE | eff) Unit
main = renderMain searchPage
