module DateUtils where 

import Prelude

import Control.Monad.Eff.Unsafe (unsafePerformEff)
import Data.DateTime (Date, canonicalDate, day, month, year)
import Data.Enum (class BoundedEnum, fromEnum, toEnum)
import Data.Int (floor, toNumber)
import Data.JSDate (JSDate, getDate, getFullYear, getMonth, jsdateLocal)
import Data.Maybe (fromJust)
import Partial.Unsafe (unsafePartial)

dateToLocalJSDate :: Date -> JSDate
dateToLocalJSDate d = 
  let 
  toNum :: forall a. BoundedEnum a => (Date -> a) -> Number
  toNum f = toNumber $ fromEnum $ f d
  in unsafePerformEff $ jsdateLocal {
        year: toNum year
      , month: toNum month - 1.0
      , day: toNum day
      , hour:0.0, minute:0.0, second:0.0, millisecond:0.0}

localJSToDate :: JSDate -> Date
localJSToDate jsd = 
    let {y,m,d} = unsafePerformEff do
            y <- getFullYear jsd
            m <- getMonth jsd 
            d <- getDate jsd
            pure {y,m,d}
    in unsafePartial $ fromJust $ canonicalDate <$> (toEnum $ floor y) <*> (toEnum $ floor m + 1) <*> (toEnum $ floor d)
