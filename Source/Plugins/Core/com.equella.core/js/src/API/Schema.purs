module OEQ.API.Schema where 

import Data.Argonaut (decodeJson)
import Data.Either (Either)
import Effect.Aff (Aff)
import OEQ.API.Requests (getJson)
import OEQ.Data.Error (ErrorResponse)

listAllCitations :: Aff (Either ErrorResponse (Array String))
listAllCitations = getJson "api/schema/citation" decodeJson