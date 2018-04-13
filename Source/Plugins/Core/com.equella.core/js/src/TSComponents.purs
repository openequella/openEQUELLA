module TSComponents where 

import Prelude

import Control.Monad.IOEffFn (IOFn1)
import Data.Maybe (Maybe(..))
import MaterialUI.Event (Event)
import React (ReactClass, ReactElement, createElement)
import Routes (Route)
import Template (template)

foreign import data Store :: Type
foreign import store :: Store
foreign import searchCourses :: forall a. ReactClass a
foreign import editCourse :: forall a. ReactClass a
foreign import searchSchemas :: forall a. ReactClass a
foreign import editSchema :: forall a. ReactClass a

coursesPage :: (Route -> {href::String, onClick::IOFn1 Event Unit}) -> ReactElement
coursesPage routes = template {mainContent:createElement searchCourses {store:store,routes} [],
    title: "Courses", titleExtra:Nothing}

courseEdit :: (Route -> {href::String, onClick::IOFn1 Event Unit}) -> String -> ReactElement
courseEdit routes cid = template {mainContent:createElement editCourse {store:store,routes,course:{uuid:cid}} [],
    title: "Course Edit", titleExtra:Nothing}

schemasPage :: (Route -> {href::String, onClick::IOFn1 Event Unit}) -> ReactElement
schemasPage routes = template {mainContent:createElement searchSchemas {store:store,routes} [],
    title: "Schemas", titleExtra:Nothing}

schemaEdit :: String -> ReactElement
schemaEdit cid = template {mainContent:createElement editSchema {store:store,schema:{uuid:cid}} [],
    title: "Schema Edit", titleExtra:Nothing}