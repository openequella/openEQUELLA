module Selection.ReturnResult where 

import Prelude

import Data.Argonaut (Json, fromArray, stringify, (.?), (.??))
import Data.Either (Either)
import Data.Maybe (Maybe(..), fromJust, fromMaybe)
import Data.Tuple (Tuple(..))
import Debug.Trace (traceM)
import EQUELLA.Environment (baseUrl)
import Effect (Effect)
import Foreign.Object (Object)
import Partial.Unsafe (unsafeCrashWith, unsafePartial)
import Search.ItemResult (ItemSelection, Result(..), SelectionType(..))
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

decodeReturnData :: Object Json -> Either String ReturnData
decodeReturnData o = do 
  returnurl <- o .?? "returnurl"
  cancelurl <- o .?? "cancelurl"
  cancelDisabled <- o .? "cancelDisabled"
  forcePost <- o .? "forcePost"
  returnprefix <- o .?? "returnprefix"
  pure {returnurl, cancelurl, cancelDisabled, forcePost, returnprefix}

selectionNameDesc :: SelectionType -> Result -> {name::String, description::String, thumbnail::String}
selectionNameDesc = case _ of 
  Summary -> \(Result i@{name,thumbnail}) -> {name,description: fromMaybe "" i.description, thumbnail}
  _ -> unsafeCrashWith "Unimplemented"

selectionToJson :: String -> ItemSelection -> Json
selectionToJson folder {item:item@Result i, selected} = 
  let {name,description,thumbnail} = selectionNameDesc selected item
  in unsafeCoerce {
      url: baseUrl <> "integ/gen/" <> i.uuid <> "/" <> show i.version <> "/",
      uuid: i.uuid, 
      name, 
      description,
      thumbnail,
      folder,
      version: i.version,
      itemName: i.name, 
      itemDescription: i.description
  }

executeReturn :: ReturnData -> Array (Tuple String (Array ItemSelection)) -> Effect Unit 
executeReturn rd@{returnurl:Just returnurl} sel = unsafePartial $ do 
  let prefix = fromMaybe "" rd.returnprefix
  doc <- window >>= document
  let hdoc = HTMLDoc.toDocument doc
  bodyElem <- fromJust <$> (body doc)
  formElem <- fromJust <$> Form.fromElement <$> (createElement "form" hdoc)
  _ <- setAction returnurl formElem
  _ <- setMethod "POST" formElem
  _ <- appendChild (Form.toNode formElem) (Elem.toNode bodyElem) 
  linkField <- fromJust <$> Input.fromElement <$> createElement "input" hdoc
  Input.setName (prefix <> "links") linkField 
  Input.setType "hidden" linkField
  Input.setValue (stringify $ fromArray $ (\(Tuple folder m) -> selectionToJson folder <$> m) =<< sel) linkField
  _ <- appendChild (Input.toNode linkField) (Form.toNode formElem) 
  submit formElem
executeReturn _ _ = pure unit