module IndexPage where 

import Prelude

import Control.Monad.Eff (Eff)
import Control.Monad.Eff.Class (liftEff)
import Control.Monad.Eff.Console (CONSOLE)
import Control.Monad.Reader (ask)
import DOM (DOM)
import DOM.HTML (window)
import DOM.HTML.Location (pathname)
import DOM.HTML.Window (location)
import Data.Either (fromRight)
import Data.Lens ((^.))
import Data.Maybe (Maybe(..), fromJust, fromMaybe, maybe)
import Data.Nullable (toMaybe)
import Data.String (Pattern(Pattern), stripPrefix)
import Data.URI.AbsoluteURI (_path)
import Data.URI.Path (printPath)
import Data.URI.URI (_hierPart, parse)
import Dispatcher (DispatchEff(..), effEval, fromContext)
import Dispatcher.React (createLifecycleComponent, didMount, modifyState)
import EQUELLA.Environment (baseUrl)
import LegacyPage (legacy)
import Partial.Unsafe (unsafePartial)
import React (createFactory)
import React.DOM (div')
import Routes (Route(TestACLS, SchemaEdit, SchemasPage, CourseEdit, CoursesPage, SettingsPage, SearchPage), matchRoute, nav)
import Routing.PushState (matchesWith)
import SearchPage (searchPage)
import Security.ACLEditor (testEditor)
import SettingsPage (settingsPage)
import TSComponents (courseEdit, coursesPage, schemaEdit, schemasPage)
import Template (renderData, renderMain, renderReact)

data RouterCommand = Init | ChangeRoute Route
type State = {route::Maybe Route}

main :: forall eff. Eff (dom :: DOM, console::CONSOLE | eff) Unit
main = do
  let basePath = unsafePartial $ fromJust $ printPath <$> (fromRight (parse baseUrl) ^. (_hierPart <<< _path))
  w <- window
  l <- location w
  p <- pathname l
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
        CourseEdit cid -> courseEdit cid
        SchemasPage -> schemasPage
        SchemaEdit cid -> schemaEdit cid
        TestACLS -> testEditor
      render _ = maybe (div' []) legacy $ toMaybe renderData.html

      eval Init = do 
        (DispatchEff d) <- ask >>= fromContext eval
        _ <- liftEff $ matchesWith parseIt (\_ -> d ChangeRoute) nav
        pure unit
      eval (ChangeRoute r) = modifyState _ {route=Just r}

  if renderData.newUI 
    then renderMain renderRoot 
    else renderReact "settingsPage" $ settingsPage {legacyMode:true}
