module OEQ.SelectionUI.ReturnResult where 

import Prelude

import Data.Argonaut (Json, jsonEmptyObject, (:=), (~>))
import Data.Argonaut.Encode ((~>?))
import Data.Either (Either)
import Data.Maybe (Maybe(..), fromJust)
import Data.Tuple (Tuple(..))
import Data.Unfoldable as U
import OEQ.API.Requests (postJson)
import OEQ.Data.Error (ErrorResponse)
import OEQ.Environment (baseUrl)
import Effect (Effect)
import Effect.Aff (Aff)
import Global.Unsafe (unsafeEncodeURI, unsafeEncodeURIComponent)
import Partial.Unsafe (unsafePartial)
import OEQ.Utils.QueryString (queryString)
import OEQ.Search.ItemResult (ItemSelection, SelectionType(..))
import OEQ.SelectionUI.Routes (SessionParams(..))
import Web.DOM.Document (createElement)
import Web.DOM.Node (appendChild)
import Web.HTML (window)
import Web.HTML.HTMLDocument (body)
import Web.HTML.HTMLDocument as HTMLDoc
import Web.HTML.HTMLElement as Elem
import Web.HTML.HTMLFormElement (setAction, setMethod, submit)
import Web.HTML.HTMLFormElement as Form
import Web.HTML.Window (document)

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

addSelection :: SessionParams -> String -> ItemSelection -> Aff (Either ErrorResponse Unit)
addSelection (Session sessid _) folderId s = do 
  let addUrl = "api/selection/" <> unsafeEncodeURIComponent sessid <> "/add"
  void <$> postJson addUrl (encodeSelection folderId s)

removeSelection :: SessionParams -> String -> ItemSelection -> Aff (Either ErrorResponse Unit)
removeSelection (Session sessid _) folderId s = do 
  let remUrl = "api/selection/" <> unsafeEncodeURIComponent sessid <> "/remove"
  void <$> postJson remUrl (encodeSelectionKey folderId s)
