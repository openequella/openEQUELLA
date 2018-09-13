module OEQ.UI.Common where 

import Prelude

import Data.Int (floor)
import Data.Maybe (Maybe, fromJust, maybe)
import Data.Traversable (traverse)
import Effect (Effect)
import Effect.Ref (Ref)
import Effect.Ref as Ref
import Effect.Uncurried (EffectFn1, EffectFn2, mkEffectFn2)
import ExtUI.MaterialUIPicker.DatePicker (utils)
import ExtUI.MaterialUIPicker.MuiPickersUtilsProvider (luxonUtils, muiPickersUtilsProvider)
import MaterialUI.Colors (blue, orange)
import MaterialUI.CssBaseline (cssBaseline_)
import MaterialUI.PropTypes (EventHandler, toHandler)
import MaterialUI.Properties (IProp, onChange)
import MaterialUI.Styles (createMuiTheme, muiThemeProvider)
import MaterialUI.Theme (Theme)
import Partial.Unsafe (unsafePartial)
import React (ReactElement, ReactRef, ReactThis)
import React.DOM as D
import React.DOM.Props as DP
import React.SyntheticEvent (NativeEventTarget, SyntheticEvent, SyntheticEvent_, SyntheticKeyboardEvent, currentTarget, keyCode, target)
import ReactDOM (render)
import Unsafe.Coerce (unsafeCoerce)
import Web.DOM.Document (documentElement)
import Web.DOM.Element (setScrollTop)
import Web.DOM.NonElementParentNode (getElementById)
import Web.HTML (HTMLElement, window)
import Web.HTML.HTMLDocument (toNonElementParentNode)
import Web.HTML.HTMLDocument as HTMLDoc
import Web.HTML.Window (document)

type ClickableHref = {href::String, onClick :: EffectFn1 SyntheticEvent Unit}

ourTheme :: Theme
ourTheme = createMuiTheme {
  palette: {
    primary: blue, 
    secondary: orange
  }
}

rootTag :: String -> Array ReactElement -> ReactElement
rootTag rootClass content = 
  muiPickersUtilsProvider [utils luxonUtils] [
      D.div [DP.className rootClass] $ [
        cssBaseline_ []
      ] <> content
  ]

renderReact :: String -> ReactElement -> Effect Unit
renderReact divId main = do
  void (elm' >>= render (muiThemeProvider {theme:ourTheme} [ main ]))
  where

  elm' = do
    doc <- window >>= document
    elm <- getElementById divId (toNonElementParentNode doc)
    pure $ unsafePartial (fromJust elm)


renderMain :: ReactElement -> Effect Unit
renderMain = renderReact "mainDiv"


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

checkChange :: forall e. (Boolean -> Effect Unit) -> EffectFn2 e Boolean Unit
checkChange f = mkEffectFn2 \_ c -> f c