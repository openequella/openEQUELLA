module Selection.ReturnResult where 

import Prelude

import Data.Argonaut (Json, fromArray, jsonEmptyObject, stringify, (.?), (.??), (:=), (~>))
import Data.Argonaut.Encode ((~>?))
import Data.Either (Either)
import Data.Maybe (Maybe(..), fromJust, fromMaybe)
import Data.Tuple (Tuple(..))
import Data.Unfoldable as U
import Debug.Trace (traceM)
import EQUELLA.Environment (baseUrl)
import Effect (Effect)
import Effect.Aff (Aff)
import Foreign.Object (Object)
import Global.Unsafe (unsafeEncodeURI, unsafeEncodeURIComponent)
import Network.HTTP.Affjax as Ajax
import Network.HTTP.Affjax.Request as Req
import Network.HTTP.Affjax.Response as Resp
import Partial.Unsafe (unsafeCrashWith, unsafePartial)
import QueryString (queryString)
import React.DOM.Dynamic (a, base)
import Search.ItemResult (ItemSelection, Result(..), SelectionType(..))
import Selection.Route (SessionParams(..))
import Text.Parsing.Indent ((<-/>))
import Unsafe.Coerce (unsafeCoerce)
import Web.DOM.Document (createElement)
import Web.DOM.Node (appendChild)
import Web.HTML (window)
import Web.HTML.HTMLDocument (body)
import Web.HTML.HTMLDocument as HTMLDoc
import Web.HTML.HTMLElement as Elem
import Web.HTML.HTMLFormElement (setAction, setMethod, submit)
import Web.HTML.HTMLFormElement as Form
import Web.HTML.HTMLInputElement as Input
import Web.HTML.Window (document)

-- {"url":"http://boorah:8080/fiveo/integ/gen/4653997f-47f4-ad05-9365-2fe3dc80dc0f/1/",
-- "name":"SearchFilters - Basic Item",
-- "description":"This is a basic item owned by DoNotUse",
-- "attachmentUuid":"",
-- "folder":"0",
-- "thumbnail":"http://boorah:8080/fiveo/thumbs/4653997f-47f4-ad05-9365-2fe3dc80dc0f/1/",
-- "uuid":"4653997f-47f4-ad05-9365-2fe3dc80dc0f",
-- "version":1,
-- "datecreated":1300943344489,
-- "datemodified":1532389656919,
-- "itemName":"SearchFilters - Basic Item",
-- "itemDescription":"This is a basic item owned by DoNotUse",
-- "owner":"A User [DoNotUse]" 
-- }

type ReturnData = {
  returnurl :: Maybe String,
  returnprefix:: Maybe String, 
  cancelurl:: Maybe String,
  forcePost:: Boolean, 
  cancelDisabled:: Boolean
}

decodeReturnData :: Object Json -> Object Json -> Either String ReturnData
decodeReturnData s o = do 
  returnurl <- o .?? "callbackURL"
  cancelurl <- o .?? "cancelURL"
  cancelDisabled <- s .? "cancelDisabled"
  forcePost <- s .? "forcePost"
  returnprefix <- o .?? "prefix"
  pure {returnurl, cancelurl, cancelDisabled, forcePost, returnprefix}

urlForSelection:: ItemSelection -> String 
urlForSelection {item:i, selected} = 
  let base = baseUrl <> "integ/gen/" <> i.uuid <> "/" <> show i.version <> "/"
  in case selected of 
    AttachmentUUID attuuid -> base <> "?attachment.uuid=" <> unsafeEncodeURI attuuid 
    Summary -> base
    Filepath fp -> baseUrl <> unsafeEncodeURIComponent fp

callReturn :: SessionParams -> Effect Unit
callReturn (Session sessid integid) = unsafePartial do 
  doc <- window >>= document
  let hdoc = HTMLDoc.toDocument doc
  bodyElem <- fromJust <$> (body doc)
  formElem <- fromJust <$> Form.fromElement <$> (createElement "form" hdoc)
  let formUrl = baseUrl <> "api/selection/" <> unsafeEncodeURIComponent sessid <> "/return?" <>
                  (queryString $ U.fromMaybe $ (Tuple "integid" <$> integid))
  _ <- setAction formUrl formElem
  _ <- setMethod "POST" formElem
  _ <- appendChild (Form.toNode formElem) (Elem.toNode bodyElem) 
  submit formElem

-- case class SelectionKey(uuid: String, version: Int, `type`: String,
--                         attachmentUuid: Option[String], folderId: Option[String], url: Option[String])

-- case class ResourceSelection(key: SelectionKey, title: String)  

encodeSelectionKey :: String -> ItemSelection -> Json 
encodeSelectionKey folderId {item,selected} = 
     "uuid" := item.uuid 
  ~> "version" := item.version
  ~> "type" := (case selected of 
    Summary -> "p"
    AttachmentUUID a -> "a"
    Filepath fp -> "p")
  ~> (case selected of 
    AttachmentUUID a -> Just $ "attachmentUuid" := a
    _ -> Nothing)
  ~>? "folderId" := folderId
  ~> jsonEmptyObject

encodeSelection :: String -> ItemSelection -> Json
encodeSelection folderId is@{name} = 
     "key" := encodeSelectionKey folderId is 
  ~> "title" := name
  ~> jsonEmptyObject

addSelection :: SessionParams -> String -> ItemSelection -> Aff Unit
addSelection (Session sessid _) folderId s = do 
  let addUrl = baseUrl <> "api/selection/" <> unsafeEncodeURIComponent sessid <> "/add"
  _ <- Ajax.post (Resp.string) addUrl (Req.json $ encodeSelection folderId s)
  pure unit
