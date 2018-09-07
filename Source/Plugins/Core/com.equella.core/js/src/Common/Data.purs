module Common.Data where 

import Prelude

import Data.Argonaut (class DecodeJson, decodeJson, (.?))

data ItemRef = ItemRef String Int 

instance irDec :: DecodeJson ItemRef where 
  decodeJson = decodeJson >=> (\o -> ItemRef <$> o .? "uuid" <*>  o .? "version")
