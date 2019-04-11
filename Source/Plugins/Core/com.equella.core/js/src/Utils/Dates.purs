module OEQ.Utils.Dates where 

import Prelude

import Data.DateTime (Date, canonicalDate, day, month, year)
import Data.Enum (class BoundedEnum, fromEnum, toEnum)
import Data.Int (floor, toNumber)
import Data.JSDate (JSDate, getDate, getFullYear, getMonth, jsdateLocal)
import Data.Maybe (fromJust)
import Effect.Unsafe (unsafePerformEffect)
import Partial.Unsafe (unsafePartial)

type LuxonDate = {year::Int, month::Int, day :: Int}

foreign import data LuxonFormat :: Type

foreign import luxonFormats :: {
  "DATE_SHORT" :: LuxonFormat, 
  "DATE_MED" :: LuxonFormat,
  "DATE_FULL" :: LuxonFormat,
  "DATE_HUGE" :: LuxonFormat
}

foreign import parseIsoToLuxon :: String -> LuxonDate 

foreign import luxonDateToIso :: LuxonDate -> String 

foreign import luxonFormat :: LuxonDate -> LuxonFormat -> String 

foreign import _dateToLuxon :: {y::Int, m::Int, d::Int} -> LuxonDate 

dateToLuxon :: Date -> LuxonDate 
dateToLuxon d = _dateToLuxon {y: fromEnum $ year d, m: fromEnum $ month d, d: fromEnum $ day d}

luxonToDate :: LuxonDate -> Date 
luxonToDate {year,month,day} = unsafePartial $ fromJust $ 
  canonicalDate <$> (toEnum year) <*> (toEnum month) <*> (toEnum day)

dateToLocalJSDate :: Date -> JSDate
dateToLocalJSDate d = 
  let 
  toNum :: forall a. BoundedEnum a => (Date -> a) -> Number
  toNum f = toNumber $ fromEnum $ f d
  in unsafePerformEffect $ jsdateLocal {
        year: toNum year
      , month: toNum month - 1.0
      , day: toNum day
      , hour:0.0, minute:0.0, second:0.0, millisecond:0.0}

localJSToDate :: JSDate -> Date
localJSToDate jsd = 
    let {y,m,d} = unsafePerformEffect do
            y <- getFullYear jsd
            m <- getMonth jsd 
            d <- getDate jsd
            pure {y,m,d}
    in unsafePartial $ fromJust $ canonicalDate <$> (toEnum $ floor y) <*> (toEnum $ floor m + 1) <*> (toEnum $ floor d)
