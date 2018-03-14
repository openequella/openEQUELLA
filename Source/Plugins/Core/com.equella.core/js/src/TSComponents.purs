module TSComponents where 

import Data.Maybe (Maybe(..))
import React (ReactClass, ReactElement, createElement)
import Template (template)

foreign import data CourseStore :: Type 
foreign import courseStore :: CourseStore
foreign import searchCourses :: forall a. ReactClass a
foreign import editCourse :: forall a. ReactClass a

coursesPage :: (String -> String) -> ReactElement
coursesPage editHref = template {mainContent:createElement searchCourses {store:courseStore,editHref} [], 
    title: "Courses", titleExtra:Nothing}

courseEdit :: String -> ReactElement
courseEdit cid = template {mainContent:createElement editCourse {store:courseStore,course:{uuid:cid, name:"Not", code:"Finished"}} [], 
    title: "Course Edit", titleExtra:Nothing}