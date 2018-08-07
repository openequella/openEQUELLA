module Uploads.UploadList (inlineUpload, universalUpload) where


import Control.Monad.IOEffFn (IOFn2, runIOFn2)
import Prelude (Unit, bind, const, mod, not, pure, show, unit, void, whenM, (#), ($), ($>), (&&), (*), (*>), (-), (/), (/=), (<$>), (<<<), (<>), (=<<), (==), (>), (>=))
import Uploads.UploadModel (Command(..), FileElement(..), State, commandEval, fileToEntry)
import Control.Monad.Eff.Class (liftEff)
import Control.Monad.Eff.Unsafe (unsafePerformEff)
import Control.Monad.IOSync (IOSync, runIOSync')
import Control.Monad.State (modify)
import Control.MonadZero (guard)
import DOM.HTML (window)
import DOM.HTML.Window (confirm)
import DOM.Node.Types (Element)
import Data.Array (catMaybes, concat, intercalate, length, mapWithIndex)
import Data.Either (Either(..), either)
import Data.Int (floor)
import Data.Maybe (Maybe(Just, Nothing), maybe)
import Data.Nullable (Nullable, toMaybe)
import Data.Tuple (Tuple(..))
import Data.Unfoldable as U
import Dispatcher (DispatchEff(DispatchEff))
import Dispatcher.React (createComponent, createLifecycleComponent)
import Network.HTTP.Affjax (URL)
import React (ReactElement, createFactory, readState)
import React.DOM (a, div, p, strong', table, tbody', td, text, tr)
import React.DOM.Dynamic (span, td')
import React.DOM.Props (Props, _id, className, href, key, onClick, target, title)
import ReactDOM (render) as RD
import Uploads.FileDrop (fileDrop, invisibleFile, customFile)
import Uploads.ProgressBar (progressBar)

type ControlStrings = { 
  edit :: String, replace :: String, delete :: String, deleteConfirm :: String,
  cancel :: String, add :: String, drop :: String, 
  none :: String, preview :: String, toomany :: String}

type DialogStrings = {
  scrapbook :: String,
  delete :: String,
  cancel :: String, 
  drop :: String
}

jsVoid :: Props
jsVoid = href "javascript:void(0);"

renderError :: String -> ReactElement
renderError msg = div [ className "ctrlinvalid" ] [ p [ className "ctrlinvalidmessage" ] [ text msg ] ]

foreign import updateCtrlErrorText :: IOFn2 String String Unit
foreign import simpleFormat :: String -> Array String -> String 

inlineUpload :: {
    elem :: Element,
    ctrlId :: String,
    entries :: Array FileElement,
    maxAttachments :: Nullable Int,
    dialog :: IOFn2 String String Unit,
    onAdd :: IOSync Unit,
    canUpload :: Boolean,
    editable :: Boolean,
    commandUrl :: URL,
    strings :: ControlStrings
  } -> Unit
inlineUpload props@{strings,ctrlId,commandUrl} = unsafePerformEff $ do
  void $ RD.render (createFactory (createLifecycleComponent updater initialState render $ commandEval {commandUrl,updateUI:Nothing}) {}) props.elem
  where
  maxAttach = toMaybe props.maxAttachments
  updater = modify _ { componentDidUpdate = \this _ {entries:oldEntries} -> do
    {entries} <- readState this
    let oldError = ctrlError oldEntries
        newError = ctrlError entries
    if oldError /= newError then runIOFn2 updateCtrlErrorText ctrlId newError else pure unit}

  ctrlError entries = if compareMax (>) entries 
    then maybe "" (\ma -> simpleFormat strings.toomany [show ma, show (length entries - ma)]) maxAttach
    else ""
  compareMax f entries = maybe false (f $ length entries) maxAttach

  initialState :: State
  initialState = {entries: fileToEntry <$> props.entries, error:Nothing}

  render {error, entries} (DispatchEff d) = 
    div [_id $ ctrlId <> "universalresources", className "universalresources"] $ concat [
      U.fromMaybe $ renderError <$> error,
      pure $ table [ className "zebra selections" ] [
        tbody' $ mapWithIndex (#) $ orNone allEntries
      ],
      guard (props.editable && (not $ compareMax (>=) entries)) *> addMore
    ]
    where 
    allEntries = either renderFile renderUpload <$> (flattenAll =<< entries)

    orNone [] = [\_ -> row "none" 0 $ [ td' [ text strings.none ] ]]
    orNone a = a

    addMore = catMaybes [
      Just $ dialogLink [ _id $ ctrlId <> "_addLink", className "add", title strings.add ] strings.add "" "",
      guard props.canUpload $> fileDrop {fileInput: invisibleFile $ ctrlId <> "_fileUpload_file", dropText:strings.drop, onFiles: liftEff <<< d UploadFiles}
    ]  
    dialogLink p name a1 a2 = a (p <> [jsVoid, onClick \_ -> runIOFn2 props.dialog a1 a2 ]) [ text name ]

    row k i = tr [ key k, className $ if i `mod` 2 == 0 then "even" else "odd" <> " rowShown " ]

    renderUpload {id,name,length,finished} i = row id i [
        td [ className "name" ] [text name, progressBar {progress: floor $ 100.0 * (finished / length)}],
        td [ className "actions" ] [ a [jsVoid, className "unselect", title strings.cancel, onClick $ d \_ -> CancelUpload id] [ ] ]
    ]

    renderFile {id,name,link,preview,indented,editable} i = row id i [
        td [ className "name" ] $ [ 
          a ((guard indented $> className "indent") <> [ href link, target "_blank" ]) [ text name ] 
        ] <> (guard preview $> span [className "preview-tag"] [text strings.preview]),
        td [ className "actions" ] $ intercalate [text " | "] $ 
          pure <$> (guard editable *> [ 
            dialogLink [] strings.edit "" id, 
            dialogLink [] strings.replace id ""]) 
          <> [ a [jsVoid, onClick $ deleteHandler id ] [ text strings.delete ] ]
      ]
    deleteHandler id _ = do 
      w <- window
      whenM (confirm strings.deleteConfirm w) $ d (const $ DeleteFile id) unit

  flattenAll (Tuple _ (Left fe)) = flattened false fe
  flattenAll (Tuple _ (Right o)) = [Right o]
  flattened indented (FileElement {id,name,link,editable,preview,children}) = [Left {id,name,link,preview,editable,indented}] <> (flattened true =<< children)

universalUpload :: {
    elem :: Element,
    ctrlId :: String,
    updateFooter :: IOSync Unit,
    scrapBookOnClick :: Nullable (IOSync Unit),
    commandUrl :: URL,
    strings :: DialogStrings
  } -> Unit
universalUpload {elem:renderElem,ctrlId,commandUrl,updateFooter, scrapBookOnClick, strings} = unsafePerformEff $ do
  void $ RD.render (createFactory (createComponent initialState render $ commandEval {commandUrl,updateUI:Just $ updateFooter}) {}) renderElem
  where 

  initialState :: State
  initialState = {entries:[], error:Nothing}
  render {entries, error} (DispatchEff d) = div [_id "uploads"] $ [ 
    div [ className "uploadsprogress" ] $ renderEntry <$> entries,
    fileDrop { fileInput: customFile $ ctrlId <> "_fileUpload", 
        dropText: strings.drop, onFiles: liftEff <<< d UploadFiles }
  ] <> catMaybes [
      renderError <$> error,
      renderScrap <$> toMaybe scrapBookOnClick
    ]
    where 
    fileRow id name a = div [ key id, className "file-upload" ] [
      span [ className "file-name" ] [ name ],
      span [ className "file-upload-progress" ] a
    ]
    renderEntry (Tuple _ (Left (FileElement fe))) = renderFinished fe
    renderEntry (Tuple _ (Right u)) = renderUpload u

    renderUpload {id,name,length,finished} = fileRow id (text name) [ 
      progressBar {progress: floor $ 100.0 * (finished / length)},
      a [jsVoid, className "unselect", title strings.cancel, onClick $ d \_ -> CancelUpload id] [ ]
    ]

    renderFinished {id,name} = fileRow id (strong' [ text name ]) [
      div [className "progress-bar"] [ div [ className "progress-bar-inner complete" ] []],
      a [jsVoid, className "unselect", title strings.delete, onClick $ d \_ -> DeleteFile id] [ ]
    ]

    renderScrap clicked = div [ className "addLink" ] [
      a [ _id $ ctrlId <> "_filesFromScrapbookLink", className "add", jsVoid, onClick \_ -> runIOSync' clicked] [text strings.scrapbook]
    ]
