module Uploads.UploadModel where 

import Effect.Aff.Compat (EffectFnAff, fromEffectFnAff)
import Prelude

import Control.Alt ((<|>))
import Control.Monad.Except (lift, throwError)
import Control.Monad.Reader (ask, runReaderT)
import Control.Parallel (parTraverse_)
import Data.Argonaut (class DecodeJson, class EncodeJson, Json, decodeJson, encodeJson, jsonParser, (.?))
import Data.Array (alterAt, any, find, findIndex, mapMaybe, snoc)
import Data.Either (Either(..), either)
import Data.Lens (Lens', Traversal', _2, _Just, _Right, lens, over, preview, set)
import Data.Lens.Record (prop)
import Data.Maybe (Maybe(..), fromMaybe, maybe)
import Data.Newtype (class Newtype, unwrap)
import Data.Symbol (SProxy(..))
import Data.Tuple (Tuple(..), fst)
import Dispatcher.React (ReactReaderT, getState, modifyState)
import Effect (Effect)
import Effect.Aff (Aff, Fiber, error, forkAff, killFiber, runAff_)
import Effect.Class (liftEffect)
import Effect.Console (logShow)
import Effect.Console as Eff
import Effect.Uncurried (EffectFn1, mkEffectFn1)
import Network.HTTP.Affjax (AffjaxResponse, post)
import Network.HTTP.Affjax.Request as AXReq
import Network.HTTP.Affjax.Response as AXResp
import Unsafe.Coerce (unsafeCoerce)
import Web.File.File (File, name, size)

type EntryId = String 

data UploadCommand = NewUpload String Number | Delete EntryId
data UploadResponse = NewUploadResponse {uploadUrl:: String, id:: EntryId, name:: String}
  | AddEntries (Array FileElement) | UploadFailed String | RemoveEntries (Array EntryId)
  | UpdateEntry FileElement

type CurrentUpload = {id::String, name::String, length:: Number, finished:: Number, fiber :: Fiber Unit }
newtype FileElement = FileElement {id::EntryId, name::String, link::String, preview::Boolean, editable::Boolean, children::Array FileElement }

derive instance feNT :: Newtype FileElement _ 

type Progress = {loaded::Number, total::Number}

data Command = UploadFiles (Array File) | Progress String {length::Number, finished::Number} | DeleteFile EntryId | CancelUpload String

type Entry = Tuple String (Either FileElement CurrentUpload)

type State = {
  entries :: Array Entry,
  error :: Maybe String
}

fileToEntry :: FileElement -> Entry 
fileToEntry fe = Tuple ((unwrap fe).id) $ Left fe

uploadToEntry :: CurrentUpload -> Entry 
uploadToEntry u = Tuple u.id $ Right u

foreign import postFile_ :: {file :: File, url::String, progress :: EffectFn1 Progress Unit} -> 
  (EffectFnAff (AffjaxResponse String))

postFile :: File -> String -> EffectFn1 Progress Unit -> Aff (AffjaxResponse Json)
postFile file url progress = do
  res <- fromEffectFnAff $ postFile_ {url, file, progress}
  either (error >>> throwError) (pure <<< res { response = _}) $ jsonParser res.response

instance encodeUC :: EncodeJson UploadCommand where
  encodeJson (NewUpload filename size) = unsafeCoerce $ {
    command : "newupload",
    size,
    filename
  }
  encodeJson (Delete id) = unsafeCoerce $ {
    command : "delete",
    id
  }
instance decodeFE :: DecodeJson FileElement where
  decodeJson v = do
    o <- decodeJson v
    id <- o .? "id"
    name <- o .? "name"
    link <- o .? "link"
    editable <- o .? "editable"
    children <- o .? "children"
    preview <- o .? "preview"
    pure $ FileElement {id,name,link,preview,editable,children}

instance decodeUR :: DecodeJson UploadResponse where
  decodeJson v = do
    o <- decodeJson v
    response <- o .? "response"
    case response of
      "newuploadresponse" -> do
        id <- o .? "id"
        uploadUrl <- o .? "uploadUrl"
        name <- o .? "name"
        pure $ NewUploadResponse {id, uploadUrl, name}
      "addentries" -> AddEntries <$> o .? "entries"
      "removeentries" -> RemoveEntries <$> o .? "ids"
      "updateentry" -> UpdateEntry <$> o .? "entry"
      "uploadfailed" -> do
        reason <- o .? "reason"
        pure $ UploadFailed reason
      _ -> Left $ "Unknown response type: " <> response



commandEval :: forall p. { commandUrl :: String, updateUI :: Maybe (Effect Unit) } -> Command -> ReactReaderT p State Aff Unit 
commandEval {commandUrl,updateUI} = eval 
  where 
  responseJson :: Aff (AffjaxResponse Json) -> Aff UploadResponse
  responseJson a = (a >>= \res -> either (error >>> throwError) pure $ decodeJson res.response) 
    <|> (pure $ UploadFailed "FAILED")
  
  runUpdate = liftEffect $ fromMaybe (pure unit) updateUI

  runEval this c = runAff_ (either logShow pure) $ runReaderT (eval c) this

  _entries = prop (SProxy :: SProxy "entries")
  _fiber = prop (SProxy :: SProxy "fiber")

  _entryForId :: String -> Lens' State (Maybe Entry)
  _entryForId uploadId = _entries <<< arrEq (\t -> fst t == uploadId)

  _currentUpload :: String -> Traversal' State CurrentUpload
  _currentUpload id = _entryForId id <<< _Just <<< _2 <<< _Right

  eval (CancelUpload uploadId) = do
    let _upload = _currentUpload uploadId
    maybeF <- preview (_upload <<< _fiber) <$> getState
    _ <- lift $ maybe (pure unit) (killFiber $ error "Cancelled") maybeF
    modifyState $ set (_entryForId uploadId) Nothing

  eval (Progress uploadId {length,finished}) = do
    modifyState $ over (_currentUpload uploadId) _ {length = length,finished = finished}

  eval (UploadFiles files) = do
    this <- ask
    lift $ parTraverse_ (uploadFile this) files
    where 
    removeOne entryId = set (_entryForId entryId) Nothing
    uploadFile this f = flip runReaderT this do 
      r <- lift $ responseJson $ post AXResp.json commandUrl $ AXReq.json $ encodeJson $ NewUpload (name f) (size f)
      case r of
        (NewUploadResponse {uploadUrl,id,name}) -> do
          fiber <- lift $ forkAff $ flip runReaderT this $ do
            postr <- lift $ responseJson $ postFile f uploadUrl 
                    (mkEffectFn1 \e -> do runEval this (Progress id {length:e.total, finished:e.loaded}))
            case postr of
              (AddEntries entries) -> do 
                modifyState $ removeOne id <<< \s -> s {entries = s.entries <> (fileToEntry <$> entries), error=Nothing}
              (UpdateEntry entry) -> do
                modifyState $ (set (_entryForId id) $ Just $ fileToEntry entry) >>> _ {error=Nothing}
              o -> do 
                modifyState $ removeOne id
                errorResponse o
            runUpdate

          let newUpload = {id, name, length:size f, finished:0.0, fiber}
          modifyState (\s -> s {entries = snoc s.entries $ uploadToEntry newUpload})
        o -> errorResponse o

  eval (DeleteFile fileid) = do 
    r <- lift $ responseJson $ post AXResp.json commandUrl $ AXReq.json $ encodeJson $ Delete fileid
    case r of 
      (RemoveEntries removed) -> do
        let remEntry (Tuple id o) | any (eq id) removed = Nothing
            remEntry (Tuple id (Left fe@(FileElement _))) = fileToEntry <$> remRecurse fe
            remEntry o = Just o
            remRecurse (FileElement {id}) | any (eq id) removed = Nothing
            remRecurse (FileElement fe@{children}) = Just $ FileElement fe {children = mapMaybe remRecurse children}
        modifyState \s -> s {entries = mapMaybe remEntry s.entries }
      o -> errorResponse o
    runUpdate

  errorResponse (UploadFailed reason) = modifyState _ {error = Just reason}
  errorResponse o = liftEffect $ Eff.error $ "Unexpected response"

arrEq :: forall a. (a -> Boolean) -> Lens' (Array a) (Maybe a)
arrEq f = lens (find f) alterIt
  where 
    alterIt arr a = fromMaybe arr $ findIndex f arr >>= \i -> alterAt i (const a) arr
