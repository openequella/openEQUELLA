module OEQ.Data.Error where 

import Prelude

import Data.Argonaut (Json, decodeJson, (.?), (.??))
import Data.Either (Either)
import Data.Maybe (Maybe)

type ErrorResponse = {code :: Int, error::String, description::Maybe String}

decodeError :: Json -> Either String ErrorResponse
decodeError v = do 
  o <- decodeJson v 
  code <- o .? "code"
  error <- o .? "error"
  description <- o .?? "error_description"
  pure {code,error,description}
