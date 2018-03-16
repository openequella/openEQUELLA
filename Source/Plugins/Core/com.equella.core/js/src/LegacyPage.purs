module LegacyPage where

import Prelude

import Control.Monad.Eff (Eff)
import DOM.HTML.Types (HTMLElement)
import Data.Array (catMaybes)
import Data.Maybe (Maybe(Nothing, Just), fromMaybe, isJust)
import Data.Nullable (Nullable, toNullable)
import Data.StrMap (StrMap, lookup)
import Dispatcher (DispatchEff(..), effEval)
import Dispatcher.React (ReactProps(..), createComponent, modifyState)
import MaterialUI.ButtonBase (onClick)
import MaterialUI.Color (inherit)
import MaterialUI.Icon (icon_)
import MaterialUI.IconButton (iconButton)
import MaterialUI.Modal (onClose, open)
import MaterialUI.Popover (anchorEl, anchorOrigin, marginThreshold, popover)
import MaterialUI.PropTypes (handle)
import MaterialUI.Properties (color)
import MaterialUI.Styles (withStyles)
import React (ReactElement, Ref, createFactory)
import React.DOM (text)
import React.DOM as D
import React.DOM.Props (Props)
import React.DOM.Props as DP
import Template (renderData, template')

foreign import setBodyHtml :: forall eff. String -> Nullable Ref -> Eff eff Unit

divWithHtml :: Array Props -> String -> ReactElement
divWithHtml p html = D.div (p <> [ DP.withRef $ setBodyHtml html ]) []

data Command = OptionsAnchor (Maybe HTMLElement)
type State = {optionsAnchor::Maybe HTMLElement}

legacy :: StrMap String -> ReactElement
legacy htmlMap = createFactory (withStyles styles $ createComponent {optionsAnchor:Nothing} render $ effEval eval) {}
  where 
  styles t = {
    screenOptions: {
        margin: 20
    }
  }
  render s (ReactProps {classes}) (DispatchEff d) = 
        template' {title:renderData.title, mainContent, titleExtra:Nothing, 
            menuExtra: fromMaybe [] $ (options <$> lookup "so" htmlMap)}
    where
    options h = [ 
        iconButton [color inherit, onClick $ handle $ d \e -> OptionsAnchor $ Just e.currentTarget] [ icon_ [text "more_vert"] ],
        popover [ open $ isJust s.optionsAnchor, marginThreshold 64
            , anchorOrigin {vertical:"bottom",horizontal:"left"}
            , onClose (handle $ d \_ -> OptionsAnchor Nothing)
            , anchorEl $ toNullable s.optionsAnchor ] 
        [ 
            divWithHtml [DP.className $ classes.screenOptions] h 
        ]
    ]
    mainContent = D.div' $ catMaybes [ 
        divWithHtml [] <$> lookup "body" htmlMap
    ]
  eval (OptionsAnchor el) = modifyState _ {optionsAnchor = el}
