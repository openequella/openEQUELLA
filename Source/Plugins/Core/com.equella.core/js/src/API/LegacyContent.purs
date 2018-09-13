module OEQ.API.LegacyContent where 

import Prelude

import Data.Argonaut (decodeJson, encodeJson)
import Data.Either (Either)
import Data.Maybe (maybe)
import Data.Nullable (toMaybe)
import OEQ.API.Requests (decodeResponse)
import OEQ.Data.Error (ErrorResponse)
import OEQ.Environment (baseUrl)
import OEQ.Data.LegacyContent (ContentResponse(..), SubmitOptions)
import Effect.Aff (Aff)
import Effect.Uncurried (runEffectFn1)
import Network.HTTP.Affjax as Ajax
import Network.HTTP.Affjax.Request as Req
import Network.HTTP.Affjax.Response as Resp

submitRequest :: String -> SubmitOptions -> Aff (Either ErrorResponse ContentResponse)
submitRequest path {vals,callback} = do 
    resp <- Ajax.post (Resp.json) (baseUrl <> "api/content/submit/" <> path)
                        (Req.json $ encodeJson vals) 
    let runcb cb json = pure $ Callback $ runEffectFn1 cb json
    pure $ decodeResponse (maybe decodeJson runcb $ toMaybe callback) resp 
