module SearchPage where

import Prelude

import Control.Comonad (extract)
import Control.Monad.Aff (Aff)
import Control.Monad.Aff.Console (log)
import Control.Monad.Eff (Eff)
import Control.Monad.Eff.Class (liftEff)
import Control.Monad.Eff.Console (logShow)
import Control.Monad.Eff.Now (now, nowDate)
import Control.Monad.Eff.Unsafe (unsafePerformEff)
import Control.Monad.Reader (ask)
import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import DOM.Event.EventTarget (addEventListener, eventListener)
import DOM.Event.Types (Event, EventType(..))
import DOM.HTML (window)
import DOM.HTML.Document (body)
import DOM.HTML.HTMLElement (offsetHeight)
import DOM.HTML.Window (document, innerHeight, scrollY)
import Data.Argonaut (class DecodeJson, decodeJson, (.?), (.??))
import Data.Array (catMaybes, filter, findMap, fromFoldable, intercalate, length, mapMaybe, mapWithIndex)
import Data.DateTime (Date, DateTime(DateTime))
import Data.DateTime.Instant (instant, toDateTime, unInstant)
import Data.Either (Either, either, fromRight)
import Data.Formatter.DateTime (Formatter, format, parseFormatString)
import Data.Int (floor)
import Data.JSDate (JSDate)
import Data.Lens (Lens', _1, _2, _Just, addOver, appendOver, over, set, setJust)
import Data.Lens.Iso.Newtype (_Newtype)
import Data.Lens.Record (prop)
import Data.Maybe (Maybe(Just, Nothing), fromJust, fromMaybe, maybe)
import Data.Newtype (class Newtype, unwrap)
import Data.Set (isEmpty)
import Data.Set as S
import Data.StrMap (lookup)
import Data.StrMap as SM
import Data.String (joinWith)
import Data.Symbol (SProxy(..))
import Data.Time.Duration (class Duration, Days(..), Milliseconds(..), fromDuration)
import Data.Tuple (Tuple(..), fst)
import DateUtils (localJSToDate)
import Dispatcher (DispatchEff(DispatchEff), dispatch, fromContext)
import Dispatcher.React (ReactProps(ReactProps), createLifecycleComponent, didMount, getState, modifyState)
import EQUELLA.Environment (baseUrl, prepLangStrings)
import Facet (facetDisplay)
import MaterialUI.Chip (chip, onDelete)
import MaterialUI.Colors (fade)
import MaterialUI.Divider as C
import MaterialUI.Icon (icon_)
import MaterialUI.List (disablePadding, list, list_)
import MaterialUI.ListItem (button, disableGutters, divider, listItem)
import MaterialUI.ListItemText (disableTypography, listItemText, primary, secondary)
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Paper (elevation, paper)
import MaterialUI.PropTypes (handle)
import MaterialUI.Properties (className, classes_, color, component, mkProp, style, variant)
import MaterialUI.Select (select)
import MaterialUI.Styles (withStyles)
import MaterialUI.TextField (label, value)
import MaterialUI.TextStyle as TS
import MaterialUI.Typography (textSecondary, typography)
import MaterialUIPicker.DatePicker (onChange)
import Network.HTTP.Affjax (AJAX, get)
import Partial.Unsafe (unsafePartial)
import QueryString (queryString)
import React (ReactElement, createFactory)
import React.DOM as D
import React.DOM.Dynamic (em')
import React.DOM.Props as DP
import SearchFilters (filterSection)
import Settings.UISettings (FacetSetting(..), NewUISettings(..), UISettings(..))
import Template (template)
import TimeAgo (timeAgo)
import Unsafe.Coerce (unsafeCoerce)

newtype Attachment = Attachment {thumbnailHref::String}
newtype DisplayField = DisplayField {name :: String, html::String}
newtype Result = Result {name::String, description:: Maybe String, modifiedDate::String,
    displayFields :: Array DisplayField, thumbnail::String, uuid::String, version::Int, attachments::Array Attachment}
newtype SearchResults = SearchResults {start::Int, length::Int, available::Int, results::Array Result}

derive instance srNT :: Newtype SearchResults _

type State = {
  searching :: Boolean,
  query :: String,
  facetSettings :: Array FacetSetting,
  facets :: SM.StrMap (S.Set String),
  searchResults :: Maybe SearchResults, 
  modifiedLast :: Maybe Milliseconds,
  after :: Tuple Boolean Date,
  before :: Tuple Boolean Date
}
type DateLens = Lens' State (Tuple Boolean Date)

data Command = InitSearch | Search | QueryUpdate String | ToggledTerm String String 
  | SetDate DateLens JSDate | ToggleDate DateLens | SetLast Milliseconds
  | Scrolled Event

initialState :: State
initialState = {searching:false, query:""
  , searchResults:Nothing
  , facets:SM.empty
  , modifiedLast: Nothing
  , after:Tuple false currentDate
  , before:Tuple false currentDate
  , facetSettings: []}

rawStrings = Tuple "searchpage" {
  resultsAvailable: "results available",
  modifiedDate: "Modified",
  filterLast: {
    name: "Resources modified within last:",
    none: "None",
    month: "Month",
    year: "Year",
    fiveyear: "Five years",
    week: "Week",
    day: "Day"
  }
}
coreStrings = Tuple "com.equella.core.searching.search" {
  title: "Search"
}

currentDate :: Date
currentDate = unsafePerformEff $ extract <$> nowDate

dateFormat :: Formatter
dateFormat = unsafePartial $ fromRight $ parseFormatString "YYYY-MM-DDTHH:mm:ss"

searchPage :: ReactElement
searchPage = createFactory (withStyles styles $ createLifecycleComponent (didMount InitSearch) initialState render eval) {}
  where
  _before :: DateLens
  _before = prop (SProxy :: SProxy "before")
  _after :: DateLens
  _after = prop (SProxy :: SProxy "after")
  _modifiedLast = prop (SProxy :: SProxy "modifiedLast")
  _searchResults = prop (SProxy :: SProxy "searchResults")
  _results = prop (SProxy :: SProxy "results")
  _length = prop (SProxy :: SProxy "length")

  string = prepLangStrings rawStrings
  coreString = prepLangStrings coreStrings

  ago :: forall a. Duration a => String -> a -> {name::String, emmed::Boolean, duration::Milliseconds}
  ago name d = {name, emmed:false, duration: fromDuration d}

  agoEntries = let s = string.filterLast in [
    (ago s.none (Milliseconds 0.0)) {emmed=true},
    ago s.day (Days 1.0), 
    ago s.week (Days 7.0),
    ago s.month (Days 28.0),
    ago s.year (Days 365.0),
    ago s.fiveyear (Days $ 365.0 * 5.0)
  ]

  styles theme = {
    results: {
      flexBasis: "75%",
      padding: 16
    },
    refinements: {
      flexBasis: "25%",
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
    },
    dateContainer: {
      margin: theme.spacing.unit
    },
    selectFilter: {
      width: 150
    }
  }

  render s@{searchResults,query,facets,facetSettings} (ReactProps {classes}) (DispatchEff d) = 
      template {mainContent,titleExtra:Just searchBar, title: coreString.title}
    where

    facetMap = SM.fromFoldable $ (\fac@(FacetSetting {path}) -> Tuple path fac) <$> facetSettings

    queryWithout exclude = joinWith " AND " $ mapMaybe whereClause $ filter (fst >>> notEq exclude) $ SM.toUnfoldable facets

    mainContent = D.div [DP.className classes.layoutDiv] [
      paper [className classes.results, elevation 4] $ 
        renderResults searchResults,
      paper [className classes.refinements, elevation 4] $ 
        intercalate [C.divider []] $ 
          (pure [ lastModifiedSelect ]) <> 
          (pure <<< makeFacet <$> facetSettings)
    ]

    lastModifiedSelect = filterSection {name:string.filterLast.name} [ 
      select [ className classes.selectFilter,
        value $ maybe 0.0 unwrap s.modifiedLast, 
        onChange $ handle $ d \e -> SetLast $ Milliseconds $ e.target.value
      ] $ (agoItem <$> agoEntries)
    ]
      where
      agoItem {name,emmed,duration:(Milliseconds ms)} = menuItem [mkProp "value" ms] $ 
        (if emmed then pure <<< em' else id) [D.text name]

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
        typography [variant TS.subheading] [ D.text $ show available <> " " <> string.resultsAvailable],
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
              typography [variant TS.body1] [ D.text string.modifiedDate ],
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

  searchMore = do 
    s <- getState
    case s of 
      {searching:false, searchResults:Just (SearchResults {start,length,available})} | start+length < available -> do 
        modifyState _ {searching=true}
        sr <- lift $ query (start+length) s
        let appendres (SearchResults sr) = 
              modifyState $ _ {searching=false} >>> over (_searchResults <<< _Just <<< _Newtype) 
                ((appendOver _results sr.results) <<< (addOver _length sr.length))
        either (lift <<< log) appendres sr
      _ -> pure unit
    

  searchWith f = do
    s <- getState
    modifyState $ (_ {searching=true} >>> f)
    sr <- lift $ query 0 (f s)
    either (lift <<< log) (\sr -> modifyState $ setJust _searchResults sr <<< _ {searching=false}) $ sr

  toggleFacet node term facMap = SM.insert node (toggle $ fromMaybe S.empty $ SM.lookup node facMap) facMap
    where
    toggle set = if S.member term set then S.delete term set else S.insert term set

  eval InitSearch = do
    searchWith id
    (DispatchEff d) <- ask >>= fromContext eval
    liftEff $ do 
      w <- window
      addEventListener (EventType "scroll") (eventListener $ d \e -> Scrolled e) false (unsafeCoerce w)
    result <- lift $ get $ baseUrl <> "api/settings/ui"
    either (lift <<< log) (\(UISettings {newUI:(NewUISettings {facets})}) -> 
      modifyState _ {facetSettings= facets}) $ decodeJson result.response
      
  eval (Scrolled e) = do
    shouldScroll <- liftEff $ do 
      w <- window
      h <- innerHeight w
      sY <- scrollY w
      d <- document w
      b <- body d
      oh <- unsafePartial $ offsetHeight $ fromJust b
      pure $ h + sY >= (floor oh - 500) 
    if shouldScroll then searchMore else pure unit

  eval (SetLast ms) = do 
    searchWith $ set (_modifiedLast) $ (guard $ unwrap ms > 0.0) $> ms

  eval (SetDate dl d) = do
    searchWith $ set (dl <<< _2) $ localJSToDate d

  eval Search = do
    searchWith id

  eval (ToggledTerm node term) = do
    searchWith \s -> s {facets = toggleFacet node term s.facets }

  eval (QueryUpdate q) = do
    searchWith _ {query=q}
  
  eval (ToggleDate l) = do
    searchWith $ over (l <<< _1) not

query :: forall e. Int -> State -> Aff (ajax :: AJAX |e) (Either String SearchResults)
query offset {facets,query,before,after,modifiedLast} = do
  let
    whereXpath = mapMaybe whereClause $ SM.toUnfoldable facets
    beforeLast ms = 
      let nowInst = unsafePerformEff now
      in Tuple "modifiedAfter" $ format dateFormat $ toDateTime $ fromMaybe nowInst $ instant $ (unInstant nowInst) - ms
    dateParam p (Tuple true d) = Just $ Tuple p $ format dateFormat (DateTime d bottom)
    dateParam _ _ = Nothing
  result <- get $ baseUrl <> "api/search?" <> (queryString $ [
      Tuple "info" "basic,detail,attachment,display",
      Tuple "q" query,        
      Tuple "start" $ show offset,
      Tuple "where" $ joinWith " AND " whereXpath
    ] <> catMaybes [
      beforeLast <$> modifiedLast,
      dateParam "modifiedBefore" before,
      dateParam "modifiedAfter" after
    ]
  )
  pure $ decodeJson result.response

whereClause :: Tuple String (S.Set String) -> Maybe String
whereClause (Tuple node terms) | not isEmpty terms = Just $ "(" <> (joinWith " OR " $ clause <$> S.toUnfoldable terms) <> ")"
  where clause term = "/xml" <> node <> " = " <> "'" <> term <> "'"
whereClause _ = Nothing

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
