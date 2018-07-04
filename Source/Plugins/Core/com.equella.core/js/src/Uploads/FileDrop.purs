module Uploads.FileDrop where 

import Prelude hiding (div)

import Data.Maybe (Maybe(..))
import Dispatcher.React (propsRenderer, saveRef, withRef)
import Effect (Effect)
import Effect.Class (liftEffect)
import Effect.Ref (new)
import Effect.Uncurried (runEffectFn1)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (button, div, div', input, span, text)
import React.DOM.Props (Props, _id, _type, className, multiple, onChange, onClick, onDragEnter, onDragLeave, onDragOver, onDrop, ref, style)
import React.SyntheticEvent (preventDefault, stopPropagation)
import Unsafe.Coerce (unsafeCoerce)
import Web.File.File (File)
import Web.HTML.HTMLElement (click)
import Web.HTML.HTMLInputElement (setValue)

invisibleFile :: String -> Effect Unit -> Array Props -> ReactElement
invisibleFile i _ p = input (p <> [_id i, style {display: "none"}])

customFile :: String -> Effect Unit -> Array Props -> ReactElement
customFile i doClick p = div [ className "customfile focus", onClick \_ -> doClick ] [
  button [className "customfile-button focus btn btn-equella btn-mini" ] [ text "Browse" ],
  span [className "customfile-feedback"] [text "No file selected..."],
  input (p <> [_id i, className "customfile-input"]) 
]

fileDrop :: {fileInput:: Effect Unit -> Array Props -> ReactElement, dropText::String, onFiles :: Array File -> Effect Unit } -> ReactElement
fileDrop = unsafeCreateLeafElement $ component "FileDrop" $ \this -> do
  fileInputRef <- new Nothing
  let
    render {fileInput,dropText,onFiles} = div' [
      fileInput clickFile [ 
        ref $ runEffectFn1 $ saveRef (unsafeCoerce fileInputRef), _type "file", multiple true, 
        onChange $ \ev -> do 
          let elem = (unsafeCoerce ev).target
          onFiles (unsafeCoerce elem).files
          liftEffect $ setValue "" elem
        ],
      div ([ className "filedrop", onDrop dropFiles, onClick \_ -> clickFile ] <>
          ((#) stopBoth <$> [onDragEnter, onDragOver, onDragLeave ]))
        [text dropText]
    ]
      where 
        clickFile = withRef fileInputRef click
        stopBoth = stopPropagation *> preventDefault
        dropFiles e = do 
          stopBoth e
          onFiles (unsafeCoerce e).dataTransfer.files
  pure {render: propsRenderer render this}

