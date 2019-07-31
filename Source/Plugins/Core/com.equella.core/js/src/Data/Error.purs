module OEQ.Data.Error where 

import Prelude

import Data.Argonaut (Json, decodeJson, (.?), (.??))
import Data.Either (Either)
import Data.Maybe (Maybe)
import Data.Nullable (Nullable, toNullable)
import Effect.Unsafe (unsafePerformEffect)
import OEQ.Utils.UUID (newUUID)

type ErrorResponse = {
  code :: Int, 
  error:: String, 
  error_description :: Nullable String, 
  id :: String
}

decodeError :: Json -> Either String ErrorResponse
decodeError v = do
  o <- decodeJson v 
  code <- o .? "code"
  error <- o .? "error"
  error_description <- o .?? "error_description"
  pure $ mkUniqueError code error error_description

mkUniqueError :: Int -> String -> Maybe String-> ErrorResponse
mkUniqueError code error desc = {code, error, error_description: toNullable desc, id: unsafePerformEffect newUUID}