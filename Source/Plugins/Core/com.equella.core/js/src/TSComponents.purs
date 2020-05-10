module TSComponents where 

import Prelude

import Data.Nullable (Nullable)
import Data.TSCompat (OptionRecord)
import Data.TSCompat.Class (class IsTSEq)
import Effect (Effect)
import Effect.Uncurried (EffectFn1)
import MaterialUI.TextField (TextFieldPropsO, TextFieldPropsM)
import React (ReactClass, ReactElement, unsafeCreateLeafElement)

foreign import startHeartbeat :: Effect Unit 
foreign import appBarQueryClass ::  ReactClass {query :: String, onChange :: EffectFn1 String Unit}

foreign import adminDownloadDialogClass :: forall a. ReactClass a

appBarQuery :: { query :: String, onChange :: EffectFn1 String Unit} -> ReactElement
appBarQuery = unsafeCreateLeafElement appBarQueryClass

type JQueryDivProps r = { 
  html:: String, 
  script :: Nullable String,
  afterHtml :: Nullable (Effect Unit)  
  |r }

foreign import jqueryDivClass :: forall r. ReactClass (JQueryDivProps r)
