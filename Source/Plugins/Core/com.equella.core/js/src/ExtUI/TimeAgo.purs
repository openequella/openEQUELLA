module ExtUI.TimeAgo where


import Data.TSCompat (OptionRecord)
import Data.TSCompat.Class (class IsTSEq)
import React (ReactClass, ReactElement, unsafeCreateLeafElement)

foreign import timeAgoClass :: forall p. ReactClass p

type TimeAgoProps = (
  live :: Boolean,
  className :: String,
  locale :: String,
  datetime :: String
)

timeAgo :: forall a. IsTSEq (Record a) (OptionRecord TimeAgoProps ()) => Record a -> ReactElement
timeAgo = unsafeCreateLeafElement timeAgoClass
