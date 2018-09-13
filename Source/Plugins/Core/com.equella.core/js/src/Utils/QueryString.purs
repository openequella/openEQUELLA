module OEQ.Utils.QueryString where 

import Prelude

import Data.String (joinWith)
import Data.Tuple (Tuple(..), fst, snd)
import Foreign.Object (Object)
import Foreign.Object as Object
import Global.Unsafe (unsafeEncodeURIComponent)

queryString :: Array (Tuple String String) -> String 
queryString = joinWith "&" <<< map urlEncode
  where urlEncode (Tuple n v) = unsafeEncodeURIComponent n <> "=" <> unsafeEncodeURIComponent v

toTuples :: Object (Array String) -> Array (Tuple String String)
toTuples o = do 
  e <- Object.toUnfoldable o
  Tuple (fst e) <$> snd e 

queryStringObj :: Object (Array String) -> String
queryStringObj = queryString <<< toTuples 
