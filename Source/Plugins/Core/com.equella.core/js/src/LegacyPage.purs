module LegacyPage where

import Prelude

import Control.Monad.Reader (runReaderT)
import Control.Monad.Trans.Class (lift)
import Data.Argonaut (class DecodeJson, class EncodeJson, decodeJson, encodeJson, (.?))
import Data.Array (catMaybes)
import Data.Either (either)
import Data.Maybe (Maybe(..), isJust)
import Data.Nullable (Nullable, toNullable)
import Data.String (joinWith)
import Debug.Trace (traceM)
import Dispatcher (affAction)
import Dispatcher.React (getProps, getState, modifyState, propsRenderer, renderer)
import EQUELLA.Environment (baseUrl)
import Effect (Effect)
import Effect.Class (liftEffect)
import Effect.Class.Console (log)
import Foreign.Object (Object, lookup)
import Global.Unsafe (unsafeEncodeURIComponent)
import MaterialUI.Color (inherit)
import MaterialUI.Icon (icon_)
import MaterialUI.IconButton (iconButton)
import MaterialUI.Modal (open)
import MaterialUI.Popover (anchorEl, anchorOrigin, marginThreshold, popover)
import MaterialUI.Properties (color, onClick, onClose)
import MaterialUI.Styles (withStyles)
import Network.HTTP.Affjax (get, post)
import Network.HTTP.Affjax.Request as Req
import Network.HTTP.Affjax.Response as Resp
import React (ReactElement, ReactRef, component, unsafeCreateLeafElement)
import React as R
import React.DOM (text)
import React.DOM as D
import React.DOM.Props (Props, _id, _type)
import React.DOM.Props as DP
import Template (renderData, template, template', templateDefaults)
import Unsafe.Coerce (unsafeCoerce)
import Utils.UI (withCurrentTarget)
import Web.HTML (HTMLElement)

foreign import setInnerHtml :: String -> Nullable ReactRef -> Effect Unit

foreign import setupLegacyHooks :: (Array NameValue -> Effect Unit) -> Effect Unit

type Resource = { src::String, "type" :: String }

foreign import loadResources :: (Array Resource) -> Effect Unit

newtype NameValue = NameValue {name::String, value::String}

newtype LegacyContent = LegacyContent {
  -- baseResources::String, 
  html:: Object String, 
  state :: Array NameValue,
  css :: Array String, 
  js :: Array String
  -- title::String, 
  -- menuItems :: Array (Array MenuItem), 
  -- menuMode :: String,
  -- fullscreenMode :: String,
  -- hideAppBar :: Boolean,
  -- newUI::Boolean, 
  -- user::UserData
}

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

data Command = OptionsAnchor (Maybe HTMLElement) | LoadPage String | Submit (Array NameValue)

type State = {optionsAnchor::Maybe HTMLElement, htmlMap :: Object String, pagePath :: String}

legacy :: {pagePath::String} -> ReactElement
legacy = unsafeCreateLeafElement $ withStyles styles $ component "LegacyPage" $ \this -> do
  -- 
  let 
    d = eval >>> affAction this
    stateinp (NameValue {name,value}) = D.input [_type "hidden", DP.name name, DP.value value ]
    render {state:s@{content}, props:{classes}} = case content of 
        Just (LegacyContent {html,state}) -> 
          let mainContent = D.form [DP.name "eqForm", DP._id "eqForm"] $ (stateinp <$> state) <> [
                D.div [DP.className $ joinWith " " $ ["content"] <> extraClass] $ catMaybes [ 
                  (divWithHtml <<< {divProps:[_id "breadcrumbs"], html: _} <$> lookup "crumbs" html),
                  (divWithHtml <<< {divProps:[], html: _} <$> lookup "upperbody" html),
                  (divWithHtml <<< {divProps:[], html: _} <$> lookup "body" html)
                ]
          ] 
          in template' (templateDefaults renderData.title) {
                                menuExtra = toNullable $ options <$> lookup "so" html 
                          } [ mainContent ]
        Nothing ->  template "Loading" []
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

    eval = case _ of 
      (OptionsAnchor el) -> modifyState _ {optionsAnchor = el}
      (LoadPage lp) -> do 
        modifyState _ {pagePath = lp}
        {response} <- lift $ get (Resp.json) $ baseUrl <> "api/content/render/" <> (unsafeEncodeURIComponent lp)
        either log updateContent $ decodeJson response 
      Submit vals -> do 
        {pagePath} <- getState
        {response} <- lift $ post (Resp.json) (baseUrl <> "api/content/submit/" <> (unsafeEncodeURIComponent pagePath)) (Req.json $ encodeJson vals)
        either log updateContent $ decodeJson response 
    updateContent lc@(LegacyContent {css, js}) = do 
      modifyState _ {content = Just lc}
      liftEffect $ loadResources $ ({"type": "css", src: _} <$> css) <> ({"type": "js", src: _} <$> js)
  {pagePath} <- R.getProps this
  setupLegacyHooks (d <<< Submit)
  pure {
    state:{optionsAnchor:Nothing, content: Nothing, pagePath:""}, 
    render: renderer render this, 
    componentDidMount: d $ LoadPage pagePath
  }

  where
  styles t = {
    screenOptions: {
        margin: 20
    },
    withPadding: {
      padding: t.spacing.unit * 2
    }
  }
instance decodeNV :: DecodeJson NameValue where 
 decodeJson v = do 
    o <- decodeJson v 
    name <- o .? "name"
    value <- o .? "value"
    pure $ NameValue {name, value}
 

instance encodeNV :: EncodeJson NameValue where 
  encodeJson (NameValue nv) = unsafeCoerce nv

instance decodeLC :: DecodeJson LegacyContent where 
  decodeJson v = do 
    o <- decodeJson v 
    html <- o .? "html"
    state <- o .? "state"
    css <- o .? "css"
    js <- o .? "js"
    pure $ LegacyContent {html, state, css, js}
