module OEQ.Data.Activation where 

import Prelude

import Data.Argonaut (Json, jsonEmptyObject, (:=), (~>))
import Data.Argonaut.Encode ((:=?), (~>?))
import Data.Date (Date)
import Data.Maybe (Maybe)
import OEQ.Data.Item (ItemRef)
import OEQ.Utils.Dates (dateToLuxon, luxonDateToIso)

type ActivationBase r = (
    "type" :: String,
    item :: ItemRef, 
    attachmentUuid :: String, 
    from :: Date,
    until :: Date,
    courseUuid :: String 
    | r
)

type ActivateReqest = Record (ActivationBase (citation :: Maybe String))

encodeActivateRequset :: ActivateReqest -> Json
encodeActivateRequset ar = 
    "type" := ar."type" ~> 
    "item" := ar.item ~>
    "attachment" := ar.attachmentUuid ~>
    "course" := ("uuid" := ar.courseUuid ~> jsonEmptyObject) ~>
    "from" := (luxonDateToIso $ dateToLuxon ar.from) ~>
    "until" := (luxonDateToIso $ dateToLuxon ar.until) ~>
    "citation" :=? ar.citation ~>?
    jsonEmptyObject