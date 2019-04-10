module OEQ.UI.Common where 

import Prelude

import Data.Int (floor)
import Data.Maybe (Maybe, fromJust, maybe)
import Data.Traversable (traverse)
import Effect (Effect)
import Effect.Ref (Ref)
import Effect.Ref as Ref
import Effect.Uncurried (EffectFn1, EffectFn2, mkEffectFn1, mkEffectFn2)
import ExtUI.MaterialUIPicker.MuiPickersUtilsProvider (luxonUtils, muiPickersUtilsProvider)
import MaterialUI.CssBaseline (cssBaseline')
import MaterialUI.Styles (createMuiTheme, muiThemeProvider)
import MaterialUI.Theme (Theme)
import Partial.Unsafe (unsafePartial)
import React (ReactElement, ReactRef, ReactThis)
import React.DOM as D
import React.DOM.Props as DP
import React.SyntheticEvent (NativeEventTarget, SyntheticEvent, SyntheticEvent_, SyntheticKeyboardEvent, SyntheticMouseEvent, currentTarget, keyCode, target)
import ReactDOM (render)
import Unsafe.Coerce (unsafeCoerce)
import Web.DOM.Document (documentElement)
import Web.DOM.Element (setScrollTop)
import Web.DOM.NonElementParentNode (getElementById)
import Web.HTML (HTMLElement, window)
import Web.HTML.HTMLDocument (toNonElementParentNode)
import Web.HTML.HTMLDocument as HTMLDoc
import Web.HTML.Window (document)

foreign import themeSettings :: {primaryColor :: String, secondaryColor :: String,
                                backgroundColor :: String, primaryTextColor :: String,
                                menuItemColor :: String, menuItemTextColor :: String, menuItemIconColor :: String, menuTextColor :: String, fontSize :: Int}


type ClickableHref = {href::String, onClick :: EffectFn1 SyntheticMouseEvent Unit}

type ExtTheme = {
  palette:: {
    menu:: {
      text :: String, 
      icon :: String, 
      background :: String
    }
  }
}
extendedTheme :: Theme -> ExtTheme 
extendedTheme = unsafeCoerce

ourTheme :: Theme
ourTheme = createMuiTheme {
  palette: {
    primary: {
      main: themeSettings.primaryColor
    },
    secondary: {
      main: themeSettings.secondaryColor
    },
    background: {
      default: themeSettings.backgroundColor
    },
    text: {
      primary: themeSettings.primaryTextColor,
      secondary: themeSettings.menuTextColor 
    }, 
    menu: {
      text: themeSettings.menuItemTextColor,
      icon: themeSettings.menuItemIconColor,
      background: themeSettings.menuItemColor
    }
  },
  typography: {
    useNextVariants: true,
    fontSize: themeSettings.fontSize
  }
}
rootTag :: String -> Array ReactElement -> ReactElement
rootTag rootClass content = 
  muiPickersUtilsProvider luxonUtils [
      D.div [DP.className rootClass] $ [
        cssBaseline' {}
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


valueChange :: forall v r. (v -> Effect Unit) -> EffectFn1 (SyntheticEvent_ (target :: NativeEventTarget|r)) Unit 
valueChange f = mkEffectFn1 $ target >=> \t -> f $ (unsafeCoerce t).value

textChange :: forall c. (c -> Effect Unit) -> (String -> c) -> EffectFn1 SyntheticEvent Unit
textChange d f = valueChange $ f >>> d

enterSubmit :: Effect Unit -> EffectFn1 SyntheticKeyboardEvent Unit
enterSubmit s = mkEffectFn1 \e -> keyCode e >>= \k -> case floor k of 
    13 -> s 
    _ -> pure unit 

withCurrentTarget :: forall r. (HTMLElement -> Effect Unit) -> EffectFn1 (SyntheticEvent_ (currentTarget :: NativeEventTarget|r)) Unit 
withCurrentTarget f = mkEffectFn1 $ currentTarget >=> f <<< unsafeCoerce

unsafeWithRef :: forall p s a. Ref (Maybe ReactRef) -> (ReactThis p s -> Effect a) -> Effect (Maybe a)
unsafeWithRef r f = Ref.read r >>= (traverse f <<< unsafeCoerce)

scrollWindowToTop :: Effect Unit 
scrollWindowToTop = do 
    doc <- window >>= document
    elem <- documentElement $ HTMLDoc.toDocument doc
    maybe (pure unit) (setScrollTop 0.0) elem
 
checkChange :: forall e. (Boolean -> Effect Unit) -> EffectFn2 e Boolean Unit
checkChange f = mkEffectFn2 \_ c -> f c
