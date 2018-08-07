module Utils.UI where 

import Prelude

import Data.Int (floor)
import Data.Maybe (Maybe, maybe)
import Data.Traversable (traverse)
import Effect (Effect)
import Effect.Ref (Ref)
import Effect.Ref as Ref
import MaterialUI.PropTypes (EventHandler, toHandler)
import MaterialUI.Properties (IProp, onChange)
import React (ReactRef, ReactThis)
import React.SyntheticEvent (NativeEventTarget, SyntheticEvent, SyntheticEvent_, SyntheticKeyboardEvent, currentTarget, keyCode, target)
import Unsafe.Coerce (unsafeCoerce)
import Web.DOM.Document (documentElement)
import Web.DOM.Element (setScrollTop)
import Web.HTML (HTMLElement, window)
import Web.HTML.HTMLDocument as HTMLDoc
import Web.HTML.Window (document)

valueChange :: forall v r. (v -> Effect Unit) -> SyntheticEvent_ (target :: NativeEventTarget|r) -> Effect Unit 
valueChange f = target >=> \t -> f $ (unsafeCoerce t).value

textChange :: forall r c. (c -> Effect Unit) -> (String -> c) -> IProp (onChange :: EventHandler SyntheticEvent|r)
textChange d f = onChange $ valueChange $ f >>> d

enterSubmit :: Effect Unit -> EventHandler SyntheticKeyboardEvent
enterSubmit s = toHandler \e -> keyCode e >>= \k -> case floor k of 
    13 -> s 
    _ -> pure unit 

withCurrentTarget :: (HTMLElement -> Effect Unit) -> SyntheticEvent -> Effect Unit 
withCurrentTarget f e = currentTarget e >>= \t -> f $ unsafeCoerce t

unsafeWithRef :: forall p s a. Ref (Maybe ReactRef) -> (ReactThis p s -> Effect a) -> Effect (Maybe a)
unsafeWithRef r f = Ref.read r >>= (traverse f <<< unsafeCoerce)

scrollWindowToTop :: Effect Unit 
scrollWindowToTop = do 
    doc <- window >>= document
    elem <- documentElement $ HTMLDoc.toDocument doc
    maybe (pure unit) (setScrollTop 0.0) elem