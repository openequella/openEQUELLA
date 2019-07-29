module Uploads.UploadList (inlineUpload, universalUpload, main) where

import Prelude hiding (div)

import Control.MonadZero (guard)
import Data.Array (catMaybes, concat, intercalate, length, mapWithIndex)
import Data.Either (Either(..), either)
import Data.Int (floor)
import Data.Maybe (Maybe(Just, Nothing), fromMaybe, maybe)
import Data.Nullable (Nullable, toMaybe)
import Data.Tuple (Tuple(..))
import Data.Unfoldable as U
import Dispatcher (affAction)
import Dispatcher.React (stateRenderer)
import Effect (Effect)
import Effect.Uncurried (EffectFn1, EffectFn2, mkEffectFn1, runEffectFn1, runEffectFn2)
import Effect.Unsafe (unsafePerformEffect)
import Network.HTTP.Affjax (URL)
import React (ReactClass, ReactElement, component, getState, unsafeCreateLeafElement)
import React as R
import React.DOM (a, div, p, strong', table, tbody', td, text, tr)
import React.DOM.Dynamic (span, td')
import React.DOM.Props (Props, _id, className, href, key, onClick, target, title)
import ReactDOM (render) as RD
import Unsafe.Coerce (unsafeCoerce)
import Uploads.FileDrop (fileDrop, invisibleFile, customFile)
import Uploads.ProgressBar (progressBar)
import Uploads.UploadModel (Command(..), FileElement(..), State, commandEval, fileToEntry)
import Web.DOM (Element)
import Web.HTML (window)
import Web.HTML.Window (confirm)

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

foreign import register :: forall a. EffectFn1 a Unit 

foreign import updateCtrlErrorText :: EffectFn2 String String Unit
foreign import simpleFormat :: String -> Array String -> String

foreign import updateDuplicateMessage :: EffectFn1 Boolean Unit

type InlineProps = (
    ctrlId :: String,
    entries :: Array FileElement,
    maxAttachments :: Nullable Int,
    canUpload :: Boolean,
    dialog :: EffectFn2 String String Unit,
    onAdd :: Effect Unit,
    editable :: Boolean,
    commandUrl :: URL,
    strings :: ControlStrings,
    reloadState :: Effect Unit
)

inlineUploadClass :: ReactClass {|InlineProps}
inlineUploadClass = component "InlineUpload" $ \this -> do 
  props@{commandUrl,ctrlId,strings} <- R.getProps this
  let 
    d = commandEval {commandUrl,updateUI:Just props.reloadState} >>> affAction this
    maxAttach = toMaybe props.maxAttachments

    componentDidUpdate _ {entries:oldEntries} _ = do
      {entries, hasAttachmentDuplicate} <- getState this
      let oldError = ctrlError oldEntries
          newError = ctrlError entries
      if oldError /= newError then runEffectFn2 updateCtrlErrorText ctrlId newError else pure unit
      runEffectFn1 updateDuplicateMessage $ fromMaybe false hasAttachmentDuplicate

    ctrlError entries = if compareMax (>) entries 
      then maybe "" (\ma -> simpleFormat strings.toomany [show ma, show (length entries - ma)]) maxAttach
      else ""
    compareMax f entries = maybe false (f $ length entries) maxAttach

    initialState :: State
    initialState = {entries: fileToEntry <$> props.entries, error:Nothing, hasAttachmentDuplicate: Just false}

    render {error, entries} = 
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
        guard props.canUpload $> fileDrop {fileInput: invisibleFile $ ctrlId <> "_fileUpload_file", dropText:strings.drop, onFiles: d <<< UploadFiles}
      ]  
      dialogLink p name a1 a2 = a (p <> [jsVoid, onClick \_ -> runEffectFn2 props.dialog a1 a2 ]) [ text name ]

      row k i = tr [ key k, className $ if i `mod` 2 == 0 then "even" else "odd" <> " rowShown " ]

      renderUpload {id,name,length,finished} i = row id i [
          td [ className "name" ] [text name, progressBar {progress: floor $ 100.0 * (finished / length)}],
          td [ className "actions" ] [ a [jsVoid, className "unselect", title strings.cancel, onClick $ \_ -> d $ CancelUpload id] [ ] ]
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
        whenM (confirm strings.deleteConfirm w) $ (\_ -> d $ DeleteFile id) unit

    flattenAll (Tuple _ (Left fe)) = flattened false fe
    flattenAll (Tuple _ (Right o)) = [Right o]
    flattened indented (FileElement {id,name,link,editable,preview,children}) = 
      [Left {id,name,link,preview,editable,indented}] <> (flattened true =<< children)
  pure {state:initialState, render: stateRenderer render this, componentDidUpdate}

inlineUpload :: EffectFn1 { elem :: Element | InlineProps } Unit
inlineUpload = mkEffectFn1 $ \props -> void $ flip RD.render props.elem $ unsafeCreateLeafElement inlineUploadClass (unsafeCoerce props)

main :: Effect Unit
main = runEffectFn1 register {inlineUpload, universalUpload}

universalUpload :: {
    elem :: Element,
    ctrlId :: String,
    updateFooter :: Effect Unit,
    scrapBookOnClick :: Nullable (Effect Unit),
    commandUrl :: URL,
    strings :: DialogStrings
  } -> Unit
universalUpload {elem:renderElem,ctrlId,commandUrl,updateFooter, scrapBookOnClick, strings} = unsafePerformEffect $ do
  void $ flip RD.render renderElem $ flip unsafeCreateLeafElement {} $ component "UniversalUpload" $ \this -> do
    let  
      d = commandEval {commandUrl,updateUI:Just $ updateFooter} >>> affAction this
      render {entries, error, hasAttachmentDuplicate} = div [_id "uploads"] $ [
        div [ className "uploadsprogress" ] $ renderEntry <$> entries,
        fileDrop { fileInput: customFile $ ctrlId <> "_fileUpload", 
            dropText: strings.drop, onFiles: d <<< UploadFiles }
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
          a [jsVoid, className "unselect", title strings.cancel, onClick $ \_ -> d $ CancelUpload id] [ ]
        ]

        renderFinished {id,name} = fileRow id (strong' [ text name ]) [
          div [className "progress-bar"] [ div [ className "progress-bar-inner complete" ] []],
          a [jsVoid, className "unselect", title strings.delete, onClick $ \_ -> d $ DeleteFile id] [ ]
        ]

        renderScrap clicked = div [ className "addLink" ] [
          a [ _id $ ctrlId <> "_filesFromScrapbookLink", className "add", jsVoid, onClick \_ -> clicked] [text strings.scrapbook]
        ]
    pure {render: stateRenderer render this, state: {entries:[], error:Nothing, hasAttachmentDuplicate: Just false}}
