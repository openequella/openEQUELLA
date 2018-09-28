module ExtUI.MaterialUIPicker.DatePicker where

import Prelude

import Data.JSDate (JSDate)
import Data.Nullable (Nullable)
import Data.TSCompat (OneOf, OptionRecord, StringConst)
import Data.TSCompat.Class (class IsTSEq)
import Data.TSCompat.React (ReactNode)
import Effect.Uncurried (EffectFn1)
import OEQ.Utils.Dates (LuxonDate)
import React (ReactClass, ReactElement, unsafeCreateLeafElement)

type DatePickerPropsO = (
  value :: Nullable JSDate {-Identifier:MaterialUiPickersDate-},
  label :: ReactNode,
  keyboard :: Boolean,
  disabled :: Boolean,
  margin :: OneOf (
    typed :: StringConst ("normal"),
    typed :: StringConst ("none"),
    typed :: StringConst ("dense")),
  minDate :: JSDate {-Identifier:DateType-},
  maxDate :: JSDate {-Identifier:DateType-},
  clearable :: Boolean,
  onChange :: EffectFn1 (Nullable LuxonDate) Unit {-unknownType:FunctionType-},
  disablePast :: Boolean,
  disableFuture :: Boolean, 
  required :: Boolean, 
  error :: Boolean, 
  helperText :: String,
  format :: String,
  fullWidth :: Boolean
) 

foreign import datePickerClass :: forall props. ReactClass props

datePicker :: forall a. IsTSEq (Record a) (OptionRecord DatePickerPropsO ()) => Record a -> ReactElement
datePicker = unsafeCreateLeafElement datePickerClass

