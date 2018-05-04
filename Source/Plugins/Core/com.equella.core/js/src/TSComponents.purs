module TSComponents where 

import Prelude

import Bridge (tsBridge)
import Data.Maybe (Maybe(..))
import Data.Nullable (toNullable)
import React (ReactClass, ReactElement, createElement)
import Template (template)
 
foreign import data Store :: Type
foreign import store :: Store
foreign import searchCourses :: forall a. ReactClass a
foreign import searchCoursesTitleBar :: forall a. ReactClass a
foreign import editCourse :: forall a. ReactClass a
foreign import searchSchemas :: forall a. ReactClass a
foreign import editSchema :: forall a. ReactClass a

coursesPage :: ReactElement
coursesPage = template {mainContent:createElement searchCourses {store:store, bridge: tsBridge} [],
    title: "Courses", titleExtra:Just $ createElement searchCoursesTitleBar {store:store, bridge: tsBridge} []}

courseEdit :: String -> ReactElement
courseEdit cid = template {mainContent:createElement editCourse {store:store, bridge: tsBridge, 
    uuid: toNullable $ Just cid} [],
    title: "Course Edit", titleExtra:Nothing}

schemasPage :: ReactElement
schemasPage = template {mainContent:createElement searchSchemas {store:store,bridge: tsBridge} [],
    title: "Schemas", titleExtra:Nothing}

schemaEdit :: String -> ReactElement
schemaEdit cid = template {mainContent:createElement editSchema {store:store,schema:{uuid:cid}} [],
    title: "Schema Edit", titleExtra:Nothing}