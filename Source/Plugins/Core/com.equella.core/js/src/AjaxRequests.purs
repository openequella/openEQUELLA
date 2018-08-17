module AjaxRequests where 


import Prelude

import Data.Argonaut (Json, decodeJson, (.?), (.??))
import Data.Bifunctor (lmap)
import Data.Either (Either(..))
import Data.Maybe (Maybe(..), maybe')
import EQUELLA.Environment (baseUrl)
import Effect.Aff (Aff)
import Network.HTTP.Affjax (AffjaxResponse)
import Network.HTTP.Affjax as Ajax
import Network.HTTP.Affjax.Response as Resp
import Network.HTTP.StatusCode (StatusCode(..))

decodeError :: Json -> Either String ErrorResponse
decodeError v = do 
  o <- decodeJson v 
  code <- o .? "code"
  error <- o .? "error"
  description <- o .?? "error_description"
  pure {code,error,description}

getJson :: forall a. (Json -> Either String a) -> String -> Aff (Either ErrorResponse a)
getJson decode path = do 
    response <- Ajax.get Resp.json (baseUrl <> path)
    pure $ decodeResponse decode response

decodeResponse :: forall a. (Json -> Either String a) -> AffjaxResponse Json -> Either ErrorResponse a
decodeResponse decode resp = case resp.status of 
    (StatusCode 200) -> lmap (\d -> { code:500, error: "Invalid JSON response", description: Just d }) 
            $ decode resp.response
    (StatusCode code) -> Left $ case decodeError resp.response of 
        Left _ -> {code, error: titleForCode code, description:Nothing}
        Right err -> err

titleForCode :: Int -> String
titleForCode 404 = "Page not found"
titleForCode _ = "Server error"

type ErrorResponse = {code :: Int, error::String, description::Maybe String}
