module ItemSummary.Api where 

import Prelude

import Data.Maybe (Maybe(..))
import Network.HTTP.ResponseHeader (ResponseHeader, responseHeaderName, responseHeaderValue)

itemApiPath :: String -> Int -> String
itemApiPath uuid version = "api/item/" <> uuid <> "/" <> show version

uuidHeader :: ResponseHeader -> Maybe String
uuidHeader r | responseHeaderName r == "x-uuid" = Just $ responseHeaderValue r
uuidHeader _ = Nothing