module OEQ.Data.Error where 

import Prelude

import Data.Argonaut (Json, decodeJson, (.?), (.??))
import Data.Either (Either)
import Data.Maybe (Maybe)
import Effect.Unsafe (unsafePerformEffect)
import OEQ.Utils.UUID (newUUID)

type ErrorResponse = {code :: Int, error::String, description::Maybe String, id :: String}

decodeError :: Json -> Either String ErrorResponse
decodeError v = do 
  o <- decodeJson v 
  code <- o .? "code"
  error <- o .? "error"
  description <- o .?? "error_description"
  pure $ mkUniqueError code error description

mkUniqueError :: Int -> String -> Maybe String-> ErrorResponse
mkUniqueError code error description = {code,error,description, id: unsafePerformEffect newUUID}