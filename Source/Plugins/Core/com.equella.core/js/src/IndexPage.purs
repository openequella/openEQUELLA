module IndexPage where 

import Prelude

import Control.Monad.Reader (runReaderT)
import Data.Either (either)
import Data.Maybe (Maybe(..), fromMaybe)
import Data.String (Pattern(Pattern), stripPrefix)
import Dispatcher.React (modifyState, stateRenderer)
import EQUELLA.Environment (basePath, startHearbeat)
import Effect (Effect)
import Effect.Class (liftEffect)
import Effect.Exception (throw)
import LegacyPage (legacy)
import Polyfills (polyfill)
import React (component, unsafeCreateLeafElement)
import React.DOM (div')
import Routes (Route(..), globalNav, matchRoute)
import Routing.PushState (matchesWith)
import SearchPage (searchPage)
import SettingsPage (settingsPage)
import TSComponents (courseEdit, coursesPage)
import Template (renderData, renderMain, renderReact)
import Web.HTML (window)
import Web.HTML.Location (pathname)
import Web.HTML.Window (location)

data RouterCommand = Init | ChangeRoute Route
type State = {route::Maybe Route}

main :: Effect Unit
main = do
  polyfill
  startHearbeat
  bp <- either (throw <<< show) pure basePath
  w <- window
  l <- location w
  path <- pathname l
  let 
    baseStripped p = fromMaybe p $ stripPrefix (Pattern bp) p
    pagePath = baseStripped path
    parseIt = matchRoute <<< baseStripped
    
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
