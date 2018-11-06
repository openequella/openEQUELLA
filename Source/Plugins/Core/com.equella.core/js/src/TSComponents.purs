module TSComponents where 

import Prelude

import Bridge (tsBridge)
import Data.Maybe (Maybe)
import Data.Nullable (Nullable, toNullable)
import Data.TSCompat (OptionRecord)
import Data.TSCompat.Class (class IsTSEq)
import Effect.Uncurried (EffectFn1)
import MaterialUI.TextField (TextFieldPropsO, TextFieldPropsM)
import OEQ.Data.Course (CourseEntity)
import React (ReactClass, ReactElement, unsafeCreateLeafElement)
 
foreign import data Store :: Type
foreign import store :: Store
foreign import searchCourses :: forall a. ReactClass a
foreign import editCourse :: forall a. ReactClass a
foreign import appBarQueryClass ::  ReactClass {query :: String, onChange :: EffectFn1 String Unit}

foreign import courseSelectClass :: forall a. ReactClass a

foreign import themePageClass :: forall a. ReactClass a

coursesPage :: ReactElement
coursesPage = unsafeCreateLeafElement searchCourses {store:store, bridge: tsBridge}

courseEdit :: Maybe String -> ReactElement
courseEdit cid = unsafeCreateLeafElement editCourse {store:store, bridge: tsBridge, uuid: toNullable $ cid}

appBarQuery :: { query :: String, onChange :: EffectFn1 String Unit} -> ReactElement
appBarQuery = unsafeCreateLeafElement appBarQueryClass

type CourseSelectPropsO r = ("TextFieldProps" :: OptionRecord (TextFieldPropsO TextFieldPropsM) TextFieldPropsM | r)
type CourseSelectPropsM = (course :: Nullable CourseEntity, maxResults :: Int,
    title::String, onCourseSelect :: EffectFn1 (Nullable CourseEntity) Unit)

courseSelect :: forall a. IsTSEq (Record a) (OptionRecord (CourseSelectPropsO CourseSelectPropsM) CourseSelectPropsM) => Record a -> ReactElement
courseSelect = unsafeCreateLeafElement courseSelectClass
