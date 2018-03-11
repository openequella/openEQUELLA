module Uploads.FileDrop where 

import Prelude hiding (div)

import Control.Monad.Eff.Class (liftEff)
import Control.Monad.IOSync (IOSync, runIOSync')
import DOM.File.Types (File)
import DOM.HTML.HTMLElement (click)
import DOM.HTML.HTMLInputElement (setValue)
import Data.Maybe (maybe)
import React (ReactElement, createClass, createFactory, getProps, preventDefault, readRef, spec, stopPropagation, writeRef)
import React.DOM (button, div, input, span, text)
import React.DOM.Props (Props, _id, _type, className, multiple, onChange, onClick, onDragEnter, onDragLeave, onDragOver, onDrop, style, withRef)
 
import Unsafe.Coerce (unsafeCoerce)

invisibleFile :: String -> IOSync Unit -> Array Props -> ReactElement
invisibleFile i _ p = input (p <> [_id i, style {display: "none"}]) []

customFile :: String -> IOSync Unit -> Array Props -> ReactElement
customFile i doClick p = div [ className "customfile focus", onClick \_ -> runIOSync' doClick ] [
  button [className "customfile-button focus btn btn-equella btn-mini" ] [ text "Browse" ],
  span [className "customfile-feedback"] [text "No file selected..."],
  input (p <> [_id i, className "customfile-input"]) []
]

fileDrop :: {fileInput::IOSync Unit -> Array Props -> ReactElement, dropText::String, onFiles :: Array File -> IOSync Unit } -> ReactElement
fileDrop = createFactory $ createClass $ spec {} \this -> render this <$> getProps this
  where 
  render this {fileInput,dropText,onFiles} = [
    fileInput (liftEff clickFile) [ 
      withRef $ writeRef this "file", _type "file", multiple true, 
      onChange $ \ev -> runIOSync' $ do 
        let elem = (unsafeCoerce ev).target
        onFiles (unsafeCoerce elem).files
        liftEff $ setValue "" elem
      ],
    div ([ className "filedrop", onDrop dropFiles, onClick \_ -> clickFile ] <>
        ((#) stopBoth <$> [onDragEnter, onDragOver, onDragLeave ]))
      [text dropText]
  ]
    where 
      clickFile = do
        r <- readRef this "file"
        maybe (pure unit) click (unsafeCoerce <$> r)
      stopBoth = stopPropagation *> preventDefault
      dropFiles e = do 
        stopBoth e
        runIOSync' $ onFiles (unsafeCoerce e).dataTransfer.files

