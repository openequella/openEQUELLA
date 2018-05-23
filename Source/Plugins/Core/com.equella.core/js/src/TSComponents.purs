module TSComponents where 

import Prelude

import Bridge (tsBridge)
import Control.Monad.IOEffFn (IOFn1)
import Data.Maybe (Maybe)
import Data.Nullable (toNullable)
import React (ReactClass, ReactElement, createElement, createFactory)
 
foreign import data Store :: Type
foreign import store :: Store
foreign import searchCourses :: forall a. ReactClass a
foreign import editCourse :: forall a. ReactClass a
foreign import appBarQueryClass ::  ReactClass {query :: String, onChange :: IOFn1 String Unit}

coursesPage :: ReactElement
coursesPage = createElement searchCourses {store:store, bridge: tsBridge} []

courseEdit :: Maybe String -> ReactElement
courseEdit cid = createElement editCourse {store:store, bridge: tsBridge, uuid: toNullable $ cid} []

appBarQuery :: { query :: String, onChange :: IOFn1 String Unit} -> ReactElement
appBarQuery = createFactory appBarQueryClass