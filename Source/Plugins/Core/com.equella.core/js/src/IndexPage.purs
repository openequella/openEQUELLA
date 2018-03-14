module IndexPage where 

import Prelude

import Control.Monad.Eff (Eff)
import Control.Monad.Eff.Class (liftEff)
import Control.Monad.Eff.Console (CONSOLE)
import Control.Monad.Reader (ask, runReaderT)
import DOM (DOM)
import Data.Maybe (Maybe(..), maybe)
import Data.Nullable (toMaybe)
import Dispatcher (effEval)
import Dispatcher.React (createLifecycleComponent, didMount, modifyState)
import LegacyPage (legacy)
import React (createFactory)
import React.DOM (div')
import Routes (Route(CourseEdit, CoursesPage, SettingsPage, SearchPage), routeHref, routeMatch)
import Routing.Hash (matches)
import SearchPage (searchPage)
import SettingsPage (settingsPage)
import TSComponents (courseEdit, coursesPage)
import Template (renderData, renderMain, renderReact)

data RouterCommand = Init | ChangeRoute Route
type State = {route::Maybe Route}

main :: forall eff. Eff (dom :: DOM, console::CONSOLE | eff) Unit
main = 
  if renderData.newUI 
  then renderMain renderRoot
  else renderReact "settingsPage" settingsPage
  where
  renderRoot = createFactory
          (createLifecycleComponent (didMount Init) {route:Nothing} render (effEval eval) ) {}
    where 
    render {route:Just r} = case r of 
      SearchPage -> searchPage
      SettingsPage -> settingsPage
      CoursesPage -> coursesPage (routeHref <<< CourseEdit)
      CourseEdit cid -> courseEdit cid
    render _ = maybe (div' []) legacy $ toMaybe renderData.html
    eval Init = do 
      this <- ask
      _ <- liftEff $ matches routeMatch (\_ r -> runReaderT (eval $ ChangeRoute r) this)
      pure unit
    eval (ChangeRoute r) = modifyState _ {route=Just r}