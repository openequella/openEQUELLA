module IndexPage where 

import Prelude

import Control.Monad.Aff (runAff_)
import Control.Monad.Aff.Console (log)
import Control.Monad.Eff (Eff)
import Control.Monad.Eff.Class (liftEff)
import Control.Monad.Eff.Console (CONSOLE)
import Control.Monad.Eff.Timer (TIMER, setInterval)
import Control.Monad.Reader (ask)
import DOM (DOM)
import DOM.HTML (window)
import DOM.HTML.Location (pathname)
import DOM.HTML.Window (location)
import Data.Either (fromRight)
import Data.Int (floor)
import Data.Lens ((^.))
import Data.Maybe (Maybe(..), fromJust, fromMaybe, maybe)
import Data.Newtype (unwrap)
import Data.Nullable (toMaybe)
import Data.String (Pattern(Pattern), stripPrefix)
import Data.Time.Duration (Minutes(..), fromDuration)
import Data.URI.AbsoluteURI (_path)
import Data.URI.Path (printPath)
import Data.URI.URI (_hierPart, parse)
import Dispatcher (DispatchEff(..), effEval, fromContext)
import Dispatcher.React (createLifecycleComponent, didMount, modifyState)
import EQUELLA.Environment (baseUrl)
import LegacyPage (legacy)
import Network.HTTP.Affjax (AJAX, get)
import Partial.Unsafe (unsafePartial)
import React (createFactory)
import React.DOM (div')
import Routes (Route(..), matchRoute, nav)
import Routing.PushState (matchesWith)
import SearchPage (searchPage)
import SettingsPage (settingsPage)
import TSComponents (courseEdit, coursesPage)
import Template (renderData, renderMain, renderReact)

data RouterCommand = Init | ChangeRoute Route
type State = {route::Maybe Route}

foreign import polyfill :: forall e. Eff e Unit

main :: forall eff. Eff (dom :: DOM, timer::TIMER, ajax::AJAX, console::CONSOLE | eff) Unit
main = do
  polyfill
  let basePath = unsafePartial $ fromJust $ printPath <$> (fromRight (parse baseUrl) ^. (_hierPart <<< _path))
  w <- window
  l <- location w
  p <- pathname l
  _ <- setInterval (floor $ unwrap $ fromDuration $ Minutes 2.0) $ runAff_ (\_ -> pure unit) $ void $ do 
    {response} <- get $ baseUrl <> "api/status/heartbeat"
    if response == "OK" then pure unit else log response
  let 
    pagePath = fromMaybe "" $ stripPrefix (Pattern basePath) p
    parseIt m = stripPrefix (Pattern $ basePath <> "page/") m >>= matchRoute
    
    initialRoute = case pagePath of 
        "access/settings.do" -> Just SettingsPage        
        _ -> Nothing

    renderRoot = createFactory
          (createLifecycleComponent (didMount Init) {route:initialRoute} render (effEval eval) ) {}
      where 
      
      render {route:Just r} = case r of 
        SearchPage -> searchPage
        SettingsPage -> settingsPage {legacyMode:false}
        CoursesPage -> coursesPage
        NewCourse -> courseEdit Nothing
        CourseEdit cid -> courseEdit $ Just cid
      render _ = maybe (div' []) legacy $ toMaybe renderData.html

      eval Init = do 
        (DispatchEff d) <- ask >>= fromContext eval
        _ <- liftEff $ matchesWith parseIt (\_ -> d ChangeRoute) nav
        pure unit
      eval (ChangeRoute r) = modifyState _ {route=Just r}

  if renderData.newUI 
    then renderMain renderRoot 
    else renderReact "settingsPage" $ settingsPage {legacyMode:true}
