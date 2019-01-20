module OEQ.MainUI.Main where 

import Prelude

import Control.Monad.Reader (runReaderT)
import Data.Either (either)
import Data.Maybe (Maybe(..), fromMaybe)
import Data.String (Pattern(Pattern), stripPrefix)
import Dispatcher.React (modifyState, stateRenderer)
import Effect (Effect)
import Effect.Class (liftEffect)
import Effect.Exception (throw)
import OEQ.Data.Item (ItemRef(..))
import OEQ.Environment (basePath, startHearbeat)
import OEQ.MainUI.LegacyPage (legacy)
import OEQ.MainUI.Routes (Route(..), globalNav, matchRoute)
import OEQ.MainUI.SearchPage (searchPage)
import OEQ.MainUI.SettingsPage (settingsPage)
import OEQ.MainUI.Template (renderData)
import OEQ.MainUI.ViewItem (viewItemPage)
import OEQ.UI.Common (renderMain, renderReact)
import OEQ.Utils.Polyfills (polyfill)
import React (component, unsafeCreateLeafElement)
import React.DOM (div')
import Routing.PushState (matchesWith)
import TSComponents (courseEdit, coursesPage, themePageClass, loginNoticeConfigPageClass)
import Web.HTML (window)
import Web.HTML.Location (pathname)
import Web.HTML.Window (location)
import Bridge (tsBridge)
import Effect.Console (log)
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
    baseStripped p = fromMaybe "" $ stripPrefix (Pattern bp) p
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
          ThemePage -> unsafeCreateLeafElement themePageClass {bridge:tsBridge}
          LoginNoticeConfigPage -> unsafeCreateLeafElement loginNoticeConfigPageClass {bridge:tsBridge}
          CourseEdit cid -> courseEdit $ Just cid
          ViewItemPage (ItemRef uuid version) -> viewItemPage {uuid,version}
          LegacyPage page -> legacy {page} 
        render _ = div' []

        eval Init =  
          void $ liftEffect $ matchesWith parseIt (\_ -> effAction <<< eval <<< ChangeRoute) globalNav
        eval (ChangeRoute r) = modifyState _ {route=Just r}
      pure {render: stateRenderer render this, componentDidMount: d Init, state: {route:initialRoute}}

  if renderData.newUI 
    then renderMain renderRoot 
    else renderReact "settingsPage" $ settingsPage {legacyMode:true}
