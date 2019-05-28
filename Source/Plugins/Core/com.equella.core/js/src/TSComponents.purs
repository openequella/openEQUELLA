module TSComponents where 

import Prelude

import Data.Nullable (Nullable)
import Data.TSCompat (OptionRecord)
import Data.TSCompat.Class (class IsTSEq)
import Effect (Effect)
import Effect.Uncurried (EffectFn1)
import MaterialUI.TextField (TextFieldPropsO, TextFieldPropsM)
import OEQ.Data.Course (CourseEntity)
import React (ReactClass, ReactElement, unsafeCreateLeafElement)

foreign import startHeartbeat :: Effect Unit 
foreign import appBarQueryClass ::  ReactClass {query :: String, onChange :: EffectFn1 String Unit}

foreign import courseSelectClass :: forall a. ReactClass a

appBarQuery :: { query :: String, onChange :: EffectFn1 String Unit} -> ReactElement
appBarQuery = unsafeCreateLeafElement appBarQueryClass

type CourseSelectPropsO r = ("TextFieldProps" :: OptionRecord (TextFieldPropsO TextFieldPropsM) TextFieldPropsM | r)
type CourseSelectPropsM = (course :: Nullable CourseEntity, maxResults :: Int,
    title::String, onCourseSelect :: EffectFn1 (Nullable CourseEntity) Unit)

courseSelect :: forall a. IsTSEq (Record a) (OptionRecord (CourseSelectPropsO CourseSelectPropsM) CourseSelectPropsM) => Record a -> ReactElement
courseSelect = unsafeCreateLeafElement courseSelectClass

type JQueryDivProps r = { 
  html:: String, 
  script :: Nullable String,
  afterHtml :: Nullable (Effect Unit)  
  |r }

foreign import jqueryDivClass :: forall r. ReactClass (JQueryDivProps r)
