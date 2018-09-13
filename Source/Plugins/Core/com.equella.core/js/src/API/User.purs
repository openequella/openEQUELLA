module OEQ.API.User where 

import Prelude

import Data.Argonaut (decodeJson, encodeJson)
import Data.Either (either)
import OEQ.Data.User (UGRDetail, UserGroupRoles)
import OEQ.Environment (baseUrl)
import Effect.Aff (Aff, error, throwError)
import Global.Unsafe (unsafeEncodeURIComponent)
import Network.HTTP.Affjax (get, post)
import Network.HTTP.Affjax.Request as Req
import Network.HTTP.Affjax.Response (json)

lookupUsers :: UserGroupRoles String String String -> Aff UGRDetail
lookupUsers r = do 
  resp <- post json (baseUrl <> "api/userquery/lookup") $ Req.json (encodeJson r)
  either (throwError <<< error) pure $ decodeJson resp.response

searchUGR :: String -> {users::Boolean, groups :: Boolean, roles :: Boolean} -> Aff UGRDetail
searchUGR q {users,groups,roles} =   do 
  let param t b = "&" <> t <> "=" <> show b
  resp <- get json $ baseUrl <> "api/userquery/search?q=" <> unsafeEncodeURIComponent q <> param "users" users 
              <> param "groups" groups <> param "roles" roles 
  either (throwError <<< error) pure $ decodeJson resp.response

listTokens :: Aff (Array String)
listTokens =  do 
  resp <- get json $ baseUrl <> "api/userquery/tokens"
  either (throwError <<< error) pure $ decodeJson resp.response
