module LegacyPage where

import Prelude

import Control.Monad.Eff (Eff)
import Control.Monad.State (modify)
import Control.Monad.Trans.Class (lift)
import DOM.HTML.Types (HTMLElement)
import Data.Array (catMaybes)
import Data.Maybe (Maybe(Nothing, Just), isJust)
import Data.Nullable (Nullable, toNullable)
import Data.StrMap (StrMap, lookup)
import Data.String (joinWith)
import Debug.Trace (traceAny)
import Dispatcher (DispatchEff(..), effEval)
import Dispatcher.React (ReactProps(ReactProps), createComponent, createLifecycleComponent, getProps, modifyState)
import MaterialUI.Color (inherit)
import MaterialUI.Icon (icon_)
import MaterialUI.IconButton (iconButton)
import MaterialUI.Modal (open)
import MaterialUI.Popover (anchorEl, anchorOrigin, marginThreshold, popover)
import MaterialUI.Properties (color, onClick, onClose)
import MaterialUI.Styles (withStyles)
import React (ReactElement, Ref, createFactory)
import React.DOM (text)
import React.DOM as D
import React.DOM.Props (Props, _id)
import React.DOM.Props as DP
import Template (renderData, template', templateDefaults)

foreign import setInnerHtml :: forall eff. String -> Nullable Ref -> Eff eff Unit

data RawHtml = DomNode (Nullable Ref)

divWithHtml :: {divProps :: Array Props, html :: String} -> ReactElement
divWithHtml = createFactory $ createLifecycleComponent lc {} render eval 
  where 
    lc = modify _ { shouldComponentUpdate = \_ _ _ -> pure false }
    eval (DomNode r) = do
      {html} <- getProps
      lift $ setInnerHtml html r
    render _ (ReactProps {divProps,html}) (DispatchEff d) = D.div (divProps <> [ DP.withRef $ d DomNode ]) []

data Command = OptionsAnchor (Maybe HTMLElement)
type State = {optionsAnchor::Maybe HTMLElement}

legacy :: StrMap String -> ReactElement
legacy htmlMap = createFactory (withStyles styles $ createComponent {optionsAnchor:Nothing} render $ effEval eval) {}
  where 
  styles t = {
    screenOptions: {
        margin: 20
    },
    withPadding: {
      padding: t.spacing.unit * 2
    }
  }
  render s (ReactProps {classes}) (DispatchEff d) = 
        template' (templateDefaults renderData.title) {menuExtra = toNullable $ options <$> lookup "so" htmlMap} [ mainContent ]
    where
    extraClass = case renderData.fullscreenMode of 
      "YES" -> []
      "YES_WITH_TOOLBAR" -> []
      _ -> case renderData.menuMode of 
        "HIDDEN" -> []
        _ -> [classes.withPadding]
    options html = [ 
        iconButton [color inherit, onClick $ d \e -> OptionsAnchor $ Just e.currentTarget] [ icon_ [text "more_vert"] ],
        popover [ open $ isJust s.optionsAnchor, marginThreshold 64
            , anchorOrigin {vertical:"bottom",horizontal:"left"}
            , onClose (d \_ -> OptionsAnchor Nothing)
            , anchorEl $ toNullable s.optionsAnchor ] 
        [ 
            divWithHtml {divProps:[DP.className $ classes.screenOptions], html}
        ]
    ]
    mainContent = D.div [DP.className $ joinWith " " $ ["content"] <> extraClass] $ catMaybes [ 
      (divWithHtml <<< {divProps:[_id "breadcrumbs"], html: _} <$> lookup "crumbs" htmlMap),
      (divWithHtml <<< {divProps:[], html: _} <$> lookup "upperbody" htmlMap),
      (divWithHtml <<< {divProps:[], html: _} <$> lookup "body" htmlMap)
    ]
  eval (OptionsAnchor el) = modifyState _ {optionsAnchor = el}
