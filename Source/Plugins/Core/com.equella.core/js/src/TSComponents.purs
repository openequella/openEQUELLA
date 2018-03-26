module TSComponents where 

import Prelude

import Control.Monad.IOEffFn (IOFn1)
import Data.Maybe (Maybe(..))
import MaterialUI.Event (Event)
import React (ReactClass, ReactElement, createElement)
import Routes (Route)
import Template (template)

foreign import data CourseStore :: Type 
foreign import courseStore :: CourseStore
foreign import searchCourses :: forall a. ReactClass a
foreign import editCourse :: forall a. ReactClass a

coursesPage :: (Route -> {href::String, onClick::IOFn1 Event Unit}) -> ReactElement
coursesPage routes = template {mainContent:createElement searchCourses {store:courseStore,routes} [], 
    title: "Courses", titleExtra:Nothing}

courseEdit :: String -> ReactElement
courseEdit cid = template {mainContent:createElement editCourse {store:courseStore,course:{uuid:cid, name:"Not", code:"Finished"}} [], 
    title: "Course Edit", titleExtra:Nothing}