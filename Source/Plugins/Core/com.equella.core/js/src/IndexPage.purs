module IndexPage where 

import Prelude

import Control.Monad.Reader (runReaderT)
import Data.Either (Either, either)
import Data.Int (floor)
import Data.Lens (view)
import Data.Maybe (Maybe(..), fromMaybe)
import Data.Newtype (unwrap)
import Data.String (Pattern(Pattern), stripPrefix)
import Data.Time.Duration (Minutes(..), fromDuration)
import Data.Tuple (Tuple(..))
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
import Polyfills (polyfill)
import React (component, unsafeCreateLeafElement)
import React.DOM (div')
import Routes (Route(..), globalNav, matchRoute)
import Routing.PushState (matchesWith)
import SearchPage (searchPage)
import SettingsPage (settingsPage)
import TSComponents (courseEdit, coursesPage)
import Template (renderData, renderMain, renderReact)
import Text.Parsing.Parser (ParseError, runParser)
import URI (AbsoluteURI, HierPath, Host, Path, Port, Query, UserInfo)
import URI.AbsoluteURI (_hierPart, _path, parser)
import URI.HostPortPair (HostPortPair)
import URI.HostPortPair as HPP
import URI.Path (print)
import Web.HTML (window)
import Web.HTML.Location (pathname)
import Web.HTML.Window (location)

data RouterCommand = Init | ChangeRoute Route
type State = {route::Maybe Route}

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
  _ <- setInterval (floor $ unwrap $ fromDuration $ Minutes 2.0) $ runAff_ (\_ -> pure unit) $ void $ do 
    {response} <- get string $ baseUrl <> "api/status/heartbeat"
    if response == "OK" then pure unit else log response
  let 
    onlyVals (Tuple n (Just v)) = Just $ Tuple n v
    onlyVals _ = Nothing 
    baseStripped p = fromMaybe p $ stripPrefix (Pattern basePath) p
    pagePath = baseStripped path
    parseIt m = matchRoute $ baseStripped m
    
    initialRoute = case pagePath of 
        "access/settings.do" -> Just SettingsPage
        a -> Nothing

    renderRoot = flip unsafeCreateLeafElement {} $ 
          component "IndexPage" $ \this -> do
      let
        effAction = flip runReaderT this
        d = eval >>> effAction
        render {route:Just r} = case r of 
          SearchPage -> searchPage
          SettingsPage -> settingsPage {legacyMode:false}
          CoursesPage -> coursesPage
          NewCourse -> courseEdit Nothing
          CourseEdit cid -> courseEdit $ Just cid
          LegacyPage page -> legacy {page}
        render _ = div' []

        eval Init =  
          void $ liftEffect $ matchesWith parseIt (\_ -> effAction <<< eval <<< ChangeRoute) globalNav
        eval (ChangeRoute r) = modifyState _ {route=Just r}
      pure {render: stateRenderer render this, componentDidMount: d Init, state: {route:initialRoute}}

  if renderData.newUI 
    then renderMain renderRoot 
    else renderReact "settingsPage" $ settingsPage {legacyMode:true}
