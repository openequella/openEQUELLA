module OEQ.API.Searches where 

import Prelude

import Data.Either (Either)
import Effect.Aff (Aff)
import Global.Unsafe (unsafeEncodeURIComponent)
import OEQ.API.Requests (getJson)
import OEQ.Data.Error (ErrorResponse)
import OEQ.Data.Searches (SearchConfig, decodeSearchConfig)

getPageConfig :: String -> Aff (Either ErrorResponse SearchConfig)
getPageConfig pagename = getJson ("api/searches/page/" <> unsafeEncodeURIComponent pagename <> "/resolve") decodeSearchConfig