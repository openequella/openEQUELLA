module TSComponents where 

import Prelude

import Bridge (tsBridge)
import Data.Maybe (Maybe)
import Data.Nullable (Nullable, toNullable)
import Effect (Effect)
import Effect.Uncurried (EffectFn1)
import MaterialUI.Properties (Enum)
import React (ReactClass, ReactElement, unsafeCreateElement, unsafeCreateLeafElement)
 
foreign import data Store :: Type
foreign import store :: Store
foreign import searchCourses :: forall a. ReactClass a
foreign import editCourse :: forall a. ReactClass a
foreign import appBarQueryClass ::  ReactClass {query :: String, onChange :: EffectFn1 String Unit}
foreign import messageInfoClass :: forall a. ReactClass a

coursesPage :: ReactElement
coursesPage = unsafeCreateLeafElement searchCourses {store:store, bridge: tsBridge}

courseEdit :: Maybe String -> ReactElement
courseEdit cid = unsafeCreateLeafElement editCourse {store:store, bridge: tsBridge, uuid: toNullable $ cid}

appBarQuery :: { query :: String, onChange :: EffectFn1 String Unit} -> ReactElement
appBarQuery = unsafeCreateLeafElement appBarQueryClass


messageInfo :: {
    open :: Boolean,
    onClose :: Effect Unit,
    title :: String,
    code :: Nullable Int,
    variant :: Enum (success ::String, warning :: String, error::String, info::String)
} -> ReactElement
messageInfo = unsafeCreateLeafElement messageInfoClass
