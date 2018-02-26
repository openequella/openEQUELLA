module TimeAgo where

import Prelude

import MaterialUI.Properties (IProp, PropValue, mkPropF, mkPropRecord)
import React (ReactClass, ReactElement, createElement)
import Unsafe.Coerce (unsafeCoerce)

foreign import timeAgoClass :: forall p. ReactClass p

class IsTimeAgoDate a where
  toProp :: a -> PropValue

instance stringDate :: IsTimeAgoDate String where
  toProp = unsafeCoerce


type TimeAgoProps = (
  live :: Boolean,
  className :: String,
  locale :: String
)

timeAgo :: forall a. IsTimeAgoDate a => a -> Array (IProp TimeAgoProps) -> ReactElement
timeAgo dt p = createElement timeAgoClass (mkPropRecord $ [mkPropF "datetime" $ toProp dt] <> p) []
