module EQUELLA.Environment where

import Data.Tuple (Tuple)

foreign import baseUrl :: String

foreign import prepLangStrings :: forall r. Tuple String (Record r) -> Record r
