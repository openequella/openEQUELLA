module IndexPage where 

import Prelude

import Control.Monad.Reader (runReaderT)
import Data.Array (mapMaybe)
import Data.Either (Either, either)
import Data.Int (floor)
import Data.Lens (view)
import Data.Maybe (Maybe(..), fromMaybe, maybe)
import Data.Newtype (unwrap)
import Data.Nullable (toMaybe)
import Data.String (Pattern(Pattern), drop, indexOf, stripPrefix)
import Data.Time.Duration (Minutes(..), fromDuration)
import Data.Tuple (Tuple(..))
import Debug.Trace (spy)
import Dispatcher.React (modifyState, stateRenderer)
import EQUELLA.Environment (baseUrl)
import Effect (Effect)
import Effect.Aff (runAff_)
import Effect.Class (liftEffect)
import Effect.Class.Console (log)
import Effect.Exception (throw)
import Effect.Timer (setInterval)
import LegacyPage (legacy)
import Network.HTTP.Affjax (get)
import Network.HTTP.Affjax.Response (string)
import React (component, unsafeCreateLeafElement)
import React.DOM (div')
import Routes (LegacyURI(..), Route(..), matchRoute, nav)
import Routing.PushState (matchesWith)
import SearchPage (searchPage)
import SettingsPage (settingsPage)
import TSComponents (courseEdit, coursesPage)
import Template (renderData, renderMain, renderReact)
import Text.Parsing.Parser (ParseError, runParser)
import URI (AbsoluteURI, HierPath, Host, Path, Port, Query, UserInfo)
import URI.AbsoluteURI (_hierPart, _path, parser)
import URI.Extra.QueryPairs (Key, QueryPairs(..), Value, keyToString, valueToString)
import URI.Extra.QueryPairs as QueryPairs
import URI.HostPortPair (HostPortPair)
import URI.HostPortPair as HPP
import URI.Path (print)
import URI.Query as Query
import Web.HTML (window)
import Web.HTML.Location (pathname, search)
import Web.HTML.Window (location)

data RouterCommand = Init | ChangeRoute Route
type State = {route::Maybe Route}

foreign import polyfill :: Effect Unit

parseURI :: String -> Either ParseError (AbsoluteURI UserInfo (HostPortPair Host Port) Path HierPath Query)
parseURI = flip runParser $ parser {
  parseUserInfo: pure, 
  parseHosts: HPP.parser pure pure, 
  parsePath: pure, 
  parseHierPath: pure, 
  parseQuery: pure
}

main :: Effect Unit
main = do
  polyfill
  basePath <- either (throw <<< show) (pure <<< print) $ 
    view (_hierPart <<< _path) <$> parseURI baseUrl
  w <- window
  l <- location w
  path <- pathname l
  qs <- spy "query" <$> search l
  _ <- setInterval (floor $ unwrap $ fromDuration $ Minutes 2.0) $ runAff_ (\_ -> pure unit) $ void $ do 
    {response} <- get string $ baseUrl <> "api/status/heartbeat"
    if response == "OK" then pure unit else log response
  let 
    onlyVals (Tuple n (Just v)) = Just $ Tuple n v
    onlyVals _ = Nothing 
    params = either (\_ -> []) (\(QueryPairs p) -> mapMaybe onlyVals p) $ QueryPairs.parse (pure <<< keyToString) (pure <<< valueToString) $ Query.unsafeFromString $ drop 1 qs
    baseStripped p = fromMaybe p $ stripPrefix (Pattern basePath) p
    pagePath = baseStripped path
    parseIt m = matchRoute $ spy "path" $ baseStripped m
    
    initialRoute = case pagePath of 
        "access/settings.do" -> Just SettingsPage
        a -> matchRoute a

    renderRoot = flip unsafeCreateLeafElement {} $ 
          component "IndexPage" $ \this -> do
      let
        d = eval >>> flip runReaderT this
        render {route:Just r} = case r of 
          SearchPage -> searchPage
          SettingsPage -> settingsPage {legacyMode:false}
          CoursesPage -> coursesPage
          NewCourse -> courseEdit Nothing
          CourseEdit cid -> courseEdit $ Just cid
          LegacyPage page -> legacy {page}
        render _ = legacy {page: LegacyURI pagePath params}

        eval Init =  
          void $ liftEffect $ matchesWith parseIt (\_ -> flip runReaderT this <<< eval <<< ChangeRoute) nav
        eval (ChangeRoute r) = modifyState _ {route=Just r}
      pure {render: stateRenderer render this, componentDidMount: d Init, state: {route:initialRoute}}

  if renderData.newUI 
    then renderMain renderRoot 
    else renderReact "settingsPage" $ settingsPage {legacyMode:true}
