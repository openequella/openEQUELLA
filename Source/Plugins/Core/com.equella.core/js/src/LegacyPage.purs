module LegacyPage where

import Prelude

import Control.Monad.Reader (runReaderT)
import Control.Monad.Trans.Class (lift)
import Data.Array (catMaybes)
import Data.Maybe (Maybe(Nothing, Just), isJust)
import Data.Nullable (Nullable, toNullable)
import Data.String (joinWith)
import Dispatcher.React (getProps, modifyState, propsRenderer, renderer)
import Effect (Effect)
import Foreign.Object (Object, lookup)
import MaterialUI.Color (inherit)
import MaterialUI.Icon (icon_)
import MaterialUI.IconButton (iconButton)
import MaterialUI.Modal (open)
import MaterialUI.Popover (anchorEl, anchorOrigin, marginThreshold, popover)
import MaterialUI.Properties (color, onClick, onClose)
import MaterialUI.Styles (withStyles)
import React (ReactElement, ReactRef, component, unsafeCreateLeafElement)
import React.DOM (text)
import React.DOM as D
import React.DOM.Props (Props, _id)
import React.DOM.Props as DP
import Template (renderData, template', templateDefaults)
import Utils.UI (withCurrentTarget)
import Web.HTML (HTMLElement)

foreign import setInnerHtml :: String -> Nullable ReactRef -> Effect Unit

data RawHtml = DomNode (Nullable ReactRef)

divWithHtml :: {divProps :: Array Props, html :: String} -> ReactElement
divWithHtml = unsafeCreateLeafElement $ component "JQueryDiv" $ \this -> do
  let
    d = eval >>> flip runReaderT this
    eval (DomNode r) = do
      {html} <- getProps
      lift $ setInnerHtml html r
    render {divProps,html} = D.div (divProps <> [ DP.ref $ d <<< DomNode ]) []
  pure {render: propsRenderer render this, shouldComponentUpdate: \_ _ -> pure false}

data Command = OptionsAnchor (Maybe HTMLElement)
type State = {optionsAnchor::Maybe HTMLElement}

legacy :: Object String -> ReactElement
legacy htmlMap = flip unsafeCreateLeafElement {} $ withStyles styles $ component "LegacyPage" $ \this -> do
  -- 
  let 
    d :: Command -> Effect Unit
    d = eval >>> flip runReaderT this
    render {state:s, props:{classes}} = 
          template' (templateDefaults renderData.title) {menuExtra = toNullable $ options <$> lookup "so" htmlMap} [ mainContent ]
      where
      extraClass = case renderData.fullscreenMode of 
        "YES" -> []
        "YES_WITH_TOOLBAR" -> []
        _ -> case renderData.menuMode of 
          "HIDDEN" -> []
          _ -> [classes.withPadding]
      options html = [ 
          iconButton [color inherit, onClick $ withCurrentTarget $ d <<< OptionsAnchor <<< Just] [ icon_ [text "more_vert"] ],
          popover [ open $ isJust s.optionsAnchor, marginThreshold 64
              , anchorOrigin {vertical:"bottom",horizontal:"left"}
              , onClose (\_ -> d $ OptionsAnchor Nothing)
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
  pure {state:{optionsAnchor:Nothing}, render: renderer render this}

  where
  styles t = {
    screenOptions: {
        margin: 20
    },
    withPadding: {
      padding: t.spacing.unit * 2
    }
  }

