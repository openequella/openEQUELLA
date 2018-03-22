module QueryString where 

import Prelude

import Data.String (joinWith)
import Data.Tuple (Tuple(..))
import Global (encodeURIComponent)

queryString :: Array (Tuple String String) -> String 
queryString = joinWith "&" <<< map urlEncode
  where urlEncode (Tuple n v) = encodeURIComponent n <> "=" <> encodeURIComponent v