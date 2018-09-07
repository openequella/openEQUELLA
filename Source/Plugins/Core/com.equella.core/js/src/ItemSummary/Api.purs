module ItemSummary.Api where 

import Prelude

import AjaxRequests (ErrorResponse, getJson)
import Data.Either (Either)
import Data.Maybe (Maybe(..))
import Effect.Aff (Aff)
import ItemSummary (ItemSummary, decodeItemSummary)
import Network.HTTP.ResponseHeader (ResponseHeader, responseHeaderName, responseHeaderValue)

itemApiPath :: String -> Int -> String
itemApiPath uuid version = "api/item/" <> uuid <> "/" <> show version

uuidHeader :: ResponseHeader -> Maybe String
uuidHeader r | responseHeaderName r == "x-uuid" = Just $ responseHeaderValue r
uuidHeader _ = Nothing

loadItemSummary :: String -> Int -> Aff (Either ErrorResponse ItemSummary)
loadItemSummary uuid version = getJson (itemApiPath uuid version <> "/summary") decodeItemSummary
