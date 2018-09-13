module OEQ.Data.Activation where 

import Prelude
import Data.Argonaut (Json, jsonEmptyObject, (:=), (~>))
import Data.Date (Date)
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

type ActivateReqest = Record (ActivationBase ())

encodeActivateRequset :: ActivateReqest -> Json
encodeActivateRequset ar@{item, attachmentUuid, courseUuid, from, until} = 
    "type" := ar."type" ~> 
    "item" := item ~>
    "attachment" := attachmentUuid ~>
    "course" := ("uuid" := courseUuid ~> jsonEmptyObject) ~>
    "from" := (luxonDateToIso $ dateToLuxon from) ~>
    "until" := (luxonDateToIso $ dateToLuxon until) ~>
    jsonEmptyObject