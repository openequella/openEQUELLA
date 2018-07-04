module TSComponents where 

import Prelude

import Bridge (tsBridge)
import Data.Maybe (Maybe)
import Data.Nullable (toNullable)
import Effect.Uncurried (EffectFn1)
import React (ReactClass, ReactElement, unsafeCreateLeafElement)
 
foreign import data Store :: Type
foreign import store :: Store
foreign import searchCourses :: forall a. ReactClass a
foreign import editCourse :: forall a. ReactClass a
foreign import appBarQueryClass ::  ReactClass {query :: String, onChange :: EffectFn1 String Unit}

coursesPage :: ReactElement
coursesPage = unsafeCreateLeafElement searchCourses {store:store, bridge: tsBridge}

courseEdit :: Maybe String -> ReactElement
courseEdit cid = unsafeCreateLeafElement editCourse {store:store, bridge: tsBridge, uuid: toNullable $ cid}

appBarQuery :: { query :: String, onChange :: EffectFn1 String Unit} -> ReactElement
appBarQuery = unsafeCreateLeafElement appBarQueryClass