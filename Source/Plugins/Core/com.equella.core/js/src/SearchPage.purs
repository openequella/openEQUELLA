module SearchPage where

import Prelude hiding (div)

import Common.CommonStrings (commonString)
import Common.Icons (userIcon, userIconName)
import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import Data.Argonaut (class DecodeJson, decodeJson, (.?), (.??))
import Data.Array (catMaybes, filter, find, findMap, fromFoldable, head, intercalate, length, mapMaybe, mapWithIndex)
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
import Data.Newtype (unwrap)
import Data.Nullable (toNullable)
import Data.Set (isEmpty)
import Data.Set as S
import Data.String (joinWith)
import Data.Symbol (SProxy(..))
import Data.Time.Duration (class Duration, Days(..), Milliseconds(..), fromDuration)
import Data.Tuple (Tuple(..), fst)
import DateUtils (localJSToDate)
import Dispatcher (affAction)
import Dispatcher.React (getState, modifyState, renderer)
import EQUELLA.Environment (baseUrl, prepLangStrings)
import Effect.Aff (Aff)
import Effect.Class (liftEffect)
import Effect.Class.Console (log)
import Effect.Now (now)
import Effect.Uncurried (mkEffectFn1)
import Effect.Unsafe (unsafePerformEffect)
import Facet (facetDisplay)
import Foreign.Object (lookup)
import Foreign.Object as SM
import MaterialUI.Button (button, raised)
import MaterialUI.Chip (chip)
import MaterialUI.CircularProgress (circularProgress)
import MaterialUI.Dialog (dialog)
import MaterialUI.DialogContent (dialogContent)
import MaterialUI.DialogTitle (dialogTitle_)
import MaterialUI.Divider (divider)
import MaterialUI.Drawer (open)
import MaterialUI.Fade (fade)
import MaterialUI.FormControl (formControl_)
import MaterialUI.Icon (icon, icon_)
import MaterialUI.InputLabel (inputLabel_)
import MaterialUI.List (disablePadding, list)
import MaterialUI.ListItem (button, divider) as LI
import MaterialUI.ListItem (disableGutters, listItem)
import MaterialUI.ListItemText (disableTypography, listItemText, primary, secondary)
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Paper (elevation, paper)
import MaterialUI.Properties (className, classes_, color, component, mkProp, onChange, onClick, onClose, onDelete, style, variant)
import MaterialUI.Select (select)
import MaterialUI.Styles (withStyles)
import MaterialUI.TextField (label, value)
import MaterialUI.TextStyle (title)
import MaterialUI.TextStyle as TS
import MaterialUI.Transition (in_, timeout)
import MaterialUI.Typography (textSecondary, typography)
import Network.HTTP.Affjax (get)
import Network.HTTP.Affjax.Response (json)
import Partial.Unsafe (unsafePartial)
import QueryString (queryString)
import React (ReactElement, unsafeCreateLeafElement)
import React as R
import React.DOM (em', div, text, div', img)
import React.DOM.Props as DP
import SearchFilters (filterSection)
import SearchResults (SearchResults(..))
import Settings.UISettings (FacetSetting(..), NewUISettings(..), UISettings(..))
import TSComponents (appBarQuery)
import Template (template', templateDefaults)
import TimeAgo (timeAgo)
import Unsafe.Coerce (unsafeCoerce)
import Users.SearchUser (UGREnabled(..), userSearch)
import Users.UserLookup (UserDetails(..), UserGroupRoles(..))
import Utils.UI (valueChange)
import Web.Event.Event (Event, EventType(..))
import Web.Event.EventTarget (addEventListener, eventListener)
import Web.HTML (window)
import Web.HTML.HTMLDocument (body)
import Web.HTML.HTMLElement (offsetHeight)
import Web.HTML.Window (document, innerHeight, scrollY)

newtype Attachment = Attachment {thumbnailHref::String}
newtype DisplayField = DisplayField {name :: String, html::String}
newtype Result = Result {name::String, description:: Maybe String, modifiedDate::String,
    displayFields :: Array DisplayField, thumbnail::String, uuid::String, version::Int, attachments::Array Attachment}

data Order = Relevance | DateModified | Name | Rating | DateCreated

type ItemSearchResults = SearchResults Result

type State = {
  searching :: Boolean,
  loadingNew :: Boolean,
  query :: String,
  facetSettings :: Array FacetSetting,
  facets :: SM.Object (S.Set String),
  searchResults :: Maybe ItemSearchResults, 
  modifiedLast :: Maybe Milliseconds,
  owner :: Maybe UserDetails,
  selectOwner :: Boolean, 
  order :: Maybe Order
}
type DateLens = Lens' State (Tuple Boolean Date)

data Command = InitSearch | Search | QueryUpdate String | ToggledTerm String String 
  | SetDate DateLens JSDate | ToggleDate DateLens | SetLast Milliseconds | SetOrder String
  | Scrolled Event | SelectOwner | OwnerSelected (Maybe UserDetails) | CloseOwner

initialState :: State
initialState = {
    searching:false
  , query:""
  , searchResults:Nothing
  , facets:SM.empty
  , modifiedLast: Nothing
  , facetSettings: []
  , loadingNew: false
  , selectOwner: false
  , owner: Nothing
  , order: Nothing
}

dateFormat :: Formatter
dateFormat = unsafePartial $ fromRight $ parseFormatString "YYYY-MM-DDTHH:mm:ss"

searchPage :: ReactElement
searchPage = flip unsafeCreateLeafElement {} $ withStyles styles $ R.component "SearchPage" $ \this -> do
  let
    d = eval >>> affAction this
    _id = prop (SProxy :: SProxy "id")
    _modifiedLast = prop (SProxy :: SProxy "modifiedLast")
    _searchResults = prop (SProxy :: SProxy "searchResults")
    _results = prop (SProxy :: SProxy "results")
    _length = prop (SProxy :: SProxy "length")

    string = prepLangStrings rawStrings
    coreString = prepLangStrings coreStrings

    ago :: forall a. Duration a => String -> a -> {name::String, emmed::Boolean, duration::Milliseconds}
    ago name d = {name, emmed:false, duration: fromDuration d}

    milliToAgo s = fromMaybe "" $ _.name <$> find (_.duration >>> eq s) agoEntries

    agoEntries = let s = string.filterLast in [
      (ago s.none (Milliseconds 0.0)) {emmed=true},
      ago s.day (Days 1.0), 
      ago s.week (Days 7.0),
      ago s.month (Days 28.0),
      ago s.year (Days 365.0),
      ago s.fiveyear (Days $ 365.0 * 5.0)
    ]

    render {state: 
      {modifiedLast,searchResults,query,facets,facetSettings,searching,loadingNew,selectOwner,owner,order}, props:{classes}} = 
        template' (templateDefaults coreString.title) 
          {titleExtra= toNullable $ Just $ appBarQuery {query, onChange: mkEffectFn1 $ d <<< QueryUpdate} } [ mainContent ]
      where

      facetMap = SM.fromFoldable $ (\fac@(FacetSetting {path}) -> Tuple path fac) <$> facetSettings

      queryWithout exclude = joinWith " AND " $ mapMaybe whereClause $ filter (fst >>> notEq exclude) $ SM.toUnfoldable facets

      mainContent = div [DP.className classes.layoutDiv] [
        paper [className classes.results, elevation 4] $ 
          renderResults searchResults <> progress,
        paper [className classes.refinements, elevation 4] $ 
          [ typography [className classes.filterTitle, variant title] [text string.refineTitle ] ] <>
          (intercalate [divider []] $ 
            (pure [ lastModifiedSelect ]) <> 
            (pure [ userFilter ]) <> 
            (pure <<< makeFacet <$> facetSettings))
      ]

      progress = [
        let pbar = circularProgress [className classes.progress]
        in fade [in_ $ searching || loadingNew, timeout $ if loadingNew then 0 else 800] [ pbar ]
      ]

      userFilter = filterSection {name:string.filterOwner.title, icon: userIcon} $ [
        button [ variant raised, onClick $ \_ -> d SelectOwner ] [ 
          icon [className classes.ownerIcon] [text userIconName],
          text commonString.action.select
        ],
        dialog [ open selectOwner, onClose $ \_ -> d CloseOwner] [
          dialogTitle_ [ text string.filterOwner.selectTitle ],
          dialogContent [className classes.ownerDialog  ] [
            userSearch {
              onSelect: mkEffectFn1 $ d <<< ownerSelected, 
              onCancel: d CloseOwner, 
              clickEntry: true,
              enabled: UGREnabled {users:true, groups:false, roles:false}
            }
          ]
        ]
      ]
        where 
        ownerSelected (UserGroupRoles {users}) = OwnerSelected $ head users

      lastModifiedSelect = filterSection {name:string.filterLast.name, icon: icon_ [text "calendar_today"]} [ 
        formControl_ [
          inputLabel_ [text string.filterLast.label],
          select [ className classes.selectFilter, 
            value $ maybe 0.0 unwrap modifiedLast, 
            onChange $ valueChange $ d <<< SetLast <<< Milliseconds
          ] $ (agoItem <$> agoEntries)
        ]
      ]
        where
        agoItem {name,emmed,duration:(Milliseconds ms)} = menuItem [mkProp "value" ms] $ 
          (if emmed then pure <<< em' else identity) [text name]

      facetChips = facetChip <$> (allVals =<< SM.toUnfoldable facets)
      allVals (Tuple node s) = {name:fromMaybe node $ unwrap >>> _.name <$> lookup node facetMap, node, value: _} <$> S.toUnfoldable s
      facetChip {name,node,value} = stdChip (name <> ": " <> value) $ ToggledTerm node value

      stdChip l c = chip [className classes.chip, label l, onDelete $ \_ -> d c]

      fullQuery = searchQuery {query,modifiedLast,owner}

      makeFacet details@(FacetSetting {path}) = facetDisplay {facet:details, 
        onClickTerm: d <<< ToggledTerm path,
        selectedTerms: fromMaybe S.empty $ SM.lookup path facets, 
        query: fullQuery <> [ Tuple "where" $ queryWithout path ] }

      renderResults (Just (SearchResults {results,available})) =
        let resultLen = length results
            orderItem o = menuItem [mkProp "value" $ orderValue o] [ text $ orderName o ]
        in [
          div [ DP.className classes.resultHeader ] [
            typography [className classes.available, component "div", variant TS.subheading] [ 
                text $ show available <> " " <> string.resultsAvailable ],
            select [ className classes.ordering, 
                    value $ orderValue $ fromMaybe Relevance order, 
                    onChange $ valueChange $ d <<< SetOrder
                  ] $ (orderItem <$> orderEntries)
          ],
          div [ DP.className classes.facetContainer ] $ 
            facetChips <> (catMaybes [
              (\(UserDetails {username}) -> stdChip (string.filterOwner.chip <> username) $ OwnerSelected Nothing) <$> owner,
              (\s -> stdChip (string.filterLast.chip <> milliToAgo s) $ SetLast $ Milliseconds 0.0) <$> modifiedLast
            ]),
          list [component "section"] (mapWithIndex (\i -> oneResult $ i /= (resultLen - 1)) results)
        ]
      renderResults Nothing = []


      oneResult showDivider (Result {name,description,displayFields,uuid,version,attachments,modifiedDate}) =
        let descMarkup descText = typography [] [ text descText ]
            titleLink = typography [variant TS.subheading, style {textDecoration:"none", color:"blue"},
                          component "a", mkProp "href" $ baseUrl <> "items/" <> uuid <> "/" <> show version <> "/"] [ text name ]
            attachThumb (Attachment {thumbnailHref}) = Just $ img [DP.aria {hidden:true}, DP.className classes.itemThumb, DP.src thumbnailHref]
            firstThumb = fromFoldable $ findMap attachThumb attachments
            extraDeets = [
              listItem [classes_ {default: classes.displayNode}, disableGutters true] [
                metaTitle string.modifiedDate,
                metaContent [
                  timeAgo modifiedDate []
                ]
              ]
            ]
            extraFields = (fieldDiv <$> displayFields) <> extraDeets
            itemContent = div [ DP.className classes.searchResultContent ] $ firstThumb <>
              [ div' $ fromFoldable (descMarkup <$> description) <> [ list [disablePadding true] extraFields ] ]
        in listItem [LI.button true, LI.divider showDivider] [
            listItemText [ disableTypography true, primary titleLink, secondary itemContent ]
        ]
        where
        metaTitle n = typography [variant TS.body1, className classes.metaLabel ] [ text n ]
        metaContent c = typography [component "div", color textSecondary] c 
        fieldDiv (DisplayField {name:n,html}) = listItem [classes_ {default: classes.displayNode}, disableGutters true] [
          metaTitle n,
          metaContent [ div [DP.dangerouslySetInnerHTML {__html: html}] [] ]
        ]

    modifySearchFlag searchFlag f = modifyState $ _{searching=searchFlag} <<< f
    searchMore = do 
      s <- getState
      case s of 
        {searching:false, searchResults:Just (SearchResults {start,length,available})} | start+length < available -> do 
          modifySearchFlag true identity
          sr <- lift $ callSearch (start+length) s
          let appendres (SearchResults newres) = 
                modifySearchFlag false $ over (_searchResults <<< _Just <<< _Newtype) 
                  ((appendOver _results newres.results) <<< (addOver _length newres.length))
          either (lift <<< log) appendres sr
        _ -> pure unit
      

    searchWith f = do
      s <- getState
      modifySearchFlag true f
      sr <- lift $ callSearch 0 (f s)
      either (lift <<< log) (modifySearchFlag false <<< setJust _searchResults) $ sr

    toggleFacet node term facMap = SM.insert node (toggle $ fromMaybe S.empty $ SM.lookup node facMap) facMap
      where
      toggle set = if S.member term set then S.delete term set else S.insert term set

    eval (SetOrder ov) = searchWith _{order = find (orderValue >>> eq ov) orderEntries}
    eval (CloseOwner) = modifyState _{selectOwner=false}
    eval (SelectOwner) = modifyState _{selectOwner=true}
    eval (OwnerSelected o) = do 
      searchWith _ {owner=o, selectOwner=false}
    eval InitSearch = do
      searchWith identity
      liftEffect $ do 
        w <- window
        l <- eventListener $ affAction this <<< eval <<< Scrolled
        addEventListener (EventType "scroll") l false (unsafeCoerce w)
      result <- lift $ get json $ baseUrl <> "api/settings/ui"
      either (lift <<< log) (\(UISettings {newUI:(NewUISettings {facets})}) -> 
        modifyState _ {facetSettings= facets}) $ decodeJson result.response

    eval (Scrolled e) = do
      shouldScroll <- liftEffect $ do 
        w <- window
        h <- innerHeight w
        sY <- scrollY w
        b <- document w >>= body
        oh <- unsafePartial $ offsetHeight $ fromJust b
        pure $ h + sY >= (floor oh - 500) 
      if shouldScroll then searchMore else pure unit

    eval (SetLast ms) = do 
      searchWith $ set (_modifiedLast) $ (guard $ unwrap ms > 0.0) $> ms

    eval (SetDate dl d) = do
      searchWith $ set (dl <<< _2) $ localJSToDate d

    eval Search = do
      searchWith identity

    eval (ToggledTerm node term) = do
      searchWith \s -> s {facets = toggleFacet node term s.facets }

    eval (QueryUpdate q) = do
      searchWith _ {query=q}
    
    eval (ToggleDate l) = do
      searchWith $ over (l <<< _1) not

    orderString = string.order 

    orderName = case _ of 
      Relevance -> orderString.relevance
      Name -> orderString.name
      DateModified -> orderString.datemodified
      DateCreated -> orderString.datecreated
      Rating -> orderString.rating
  pure {render: renderer render this, state:initialState, componentDidMount: d InitSearch}
  where 
  styles theme = {
    results: {
      flexBasis: "75%",
      display: "flex",
      flexDirection: "column",
      padding: 16
    },
    refinements: {
      flexBasis: "25%",
      marginLeft: 16, 
      display: "flex", 
      flexDirection: "column",
      padding: theme.spacing.unit * 2
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
      padding: theme.spacing.unit * 2,
      display: "flex",
      justifyContent: "space-around"
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
      width: "15em"
    }, 
    progress: {
      alignSelf: "center"
    }, 
    ownerDialog: {
      width: 600,
      height: 600
    }, 
    filterTitle: {
      alignSelf: "center",
      margin: theme.spacing.unit
    }, 
    ownerIcon: {
      marginRight: theme.spacing.unit
    }, 
    resultHeader: {
      display: "flex", 
      alignItems: "center"
    }, 
    ordering: {
      width: "10em"
    },
    available: {
      flexGrow: 1
    }, 
    metaLabel: {
      alignSelf: "flex-start", 
      marginRight: theme.spacing.unit
    }
  }

orderValue :: Order -> String
orderValue = case _ of 
  Relevance -> "relevance"
  Name -> "name"
  DateModified -> "modifed"
  DateCreated -> "created"
  Rating -> "rating"

orderEntries :: Array Order
orderEntries = [Relevance, Name, DateModified, DateCreated ]

beforeLast :: Milliseconds -> Tuple String String
beforeLast (Milliseconds ms) = 
  let nowInst = unsafePerformEffect now
  in Tuple "modifiedAfter" $ format dateFormat $ toDateTime $ fromMaybe nowInst $ instant $ 
    Milliseconds $ unwrap (unInstant nowInst) - ms

searchQuery :: { owner :: Maybe UserDetails
  , modifiedLast :: Maybe Milliseconds
  , query :: String}
  -> Array (Tuple String String)
searchQuery {query,modifiedLast,owner} = [
  Tuple "q" query 
] <> catMaybes [
  beforeLast <$> modifiedLast,
  (\(UserDetails {id}) -> Tuple "owner" id) <$> owner
]

callSearch :: Int -> State -> Aff (Either String ItemSearchResults)
callSearch offset {facets,query,modifiedLast,owner,order} = do
  let whereXpath = mapMaybe whereClause $ SM.toUnfoldable facets
      dateParam p (Tuple true d) = Just $ Tuple p $ format dateFormat (DateTime d bottom)
      dateParam _ _ = Nothing
  result <- get json $ baseUrl <> "api/search?" <> (queryString $ [
      Tuple "info" "basic,detail,attachment,display",
      Tuple "start" $ show offset,
      Tuple "where" $ joinWith " AND " whereXpath
    ] <> searchQuery {query,modifiedLast,owner} <> catMaybes [
      (orderValue >>> Tuple "order") <$> order
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

rawStrings = {prefix: "searchpage", 
  strings: {
    resultsAvailable: "results available",
    modifiedDate: "Modified",
    refineTitle: "Refine search",
    filterLast: {
      label: "Modified within last",
      chip: "Modified within: ",
      name: "Modification date",
      none: "\xa0-\xa0",
      month: "Month",
      year: "Year",
      fiveyear: "Five years",
      week: "Week",
      day: "Day"
    }, 
    filterOwner: {
      title: "Owner",
      chip: "Owner: ",
      selectTitle: "Select user to filter by"
    }, 
    order: {
      relevance: "Relevance",
      name: "Name",
      datemodified: "Date modifed",
      datecreated: "Date created",
      rating: "Rating"
    }
  }
}

coreStrings = {
  prefix: "com.equella.core.searching.search", 
  strings: {
    title: "Search"
  }
}
