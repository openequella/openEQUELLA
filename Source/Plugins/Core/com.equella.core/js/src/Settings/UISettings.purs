module Settings.UISettings where

import Prelude

import Control.Monad.Aff (Fiber, Milliseconds(..), delay, error, forkAff, killFiber)
import Control.Monad.Aff.Console (CONSOLE, log)
import Control.Monad.Eff.Uncurried (mkEffFn1, mkEffFn2)
import Control.Monad.Reader (ask, runReaderT)
import Control.Monad.Trans.Class (lift)
import Data.Argonaut (class DecodeJson, class EncodeJson, decodeJson, encodeJson, jsonEmptyObject, (.?), (:=), (~>))
import Data.Array (deleteAt, mapWithIndex, modifyAt, snoc, updateAt)
import Data.Either (either)
import Data.Lens (over)
import Data.Lens.Index (ix)
import Data.Lens.Iso.Newtype (_Newtype)
import Data.Lens.Record (prop)
import Data.Lens.Setter (set)
import Data.Maybe (Maybe(..), fromMaybe, maybe)
import Data.Newtype (class Newtype)
import Data.String (joinWith)
import Data.Symbol (SProxy(..))
import Dispatcher (DispatchEff(..))
import Dispatcher.React (ReactProps(..), createLifecycleComponent, didMount, getState, modifyState)
import EQUELLA.Environment (baseUrl)
import MaterialUI.Button (button, fab)
import MaterialUI.ButtonBase (onClick)
import MaterialUI.ExpansionPanelDetails (expansionPanelDetails_)
import MaterialUI.FormControl (formControl, formControl_)
import MaterialUI.FormControlLabel (control, formControlLabel, label)
import MaterialUI.Icon (icon, icon_)
import MaterialUI.PropTypes (handle)
import MaterialUI.Properties (IProp, className, style, variant)
import MaterialUI.Styles (withStyles)
import MaterialUI.Switch (switch)
import MaterialUI.SwitchBase (checked, disabled, onChange)
import MaterialUI.TextField (margin, placeholder, textField, value)
import MaterialUI.TextStyle (headline, subheading)
import MaterialUI.Typography (typography, typography_)
import Network.HTTP.Affjax (AJAX, get, put_)
import React (ReactElement, createFactory)
import React.DOM (text)
import React.DOM as D
import React.DOM.Props as DP


newtype FacetSetting = FacetSetting { name :: String, path :: String }
newtype NewUISettings = NewUISettings { enabled :: Boolean, facets :: Array FacetSetting }
newtype UISettings = UISettings { newUI :: NewUISettings }

derive instance newtypeUISettings :: Newtype UISettings _
derive instance newtypeNewUISettings :: Newtype NewUISettings _
derive instance newtypeFacetSettings :: Newtype FacetSetting _

instance facetDec :: DecodeJson FacetSetting where
  decodeJson v = do
    o <- decodeJson v
    name <- o .? "name"
    path <- o .? "path"
    pure $ FacetSetting {name, path}

instance decNewUISettings :: DecodeJson NewUISettings where
  decodeJson v = do
    o <- decodeJson v
    enabled <- o .? "enabled"
    facets <- o .? "facets"
    pure $ NewUISettings {enabled,facets}

instance decUISettings :: DecodeJson UISettings where
  decodeJson v = do
    o <- decodeJson v
    newUI <- o .? "newUI"
    pure $ UISettings {newUI}

instance encFacetSetting :: EncodeJson FacetSetting where
  encodeJson (FacetSetting {name,path}) =
    "name" := name ~>
    "path" := path ~>
    jsonEmptyObject

instance encNewUISettings :: EncodeJson NewUISettings where
  encodeJson (NewUISettings {enabled,facets}) =
     "enabled" := enabled ~>
     "facets" := facets ~>
     jsonEmptyObject

instance encUISettings :: EncodeJson UISettings where
  encodeJson (UISettings {newUI}) = "newUI" := newUI ~> jsonEmptyObject

data Command = LoadSetting | SetNewUI Boolean
              | ModifyFacet Int (FacetSetting -> FacetSetting) | RemoveFacet Int
              | AddFacet

type State eff = {
  disabled :: Boolean,
  settings :: UISettings,
  saving :: Maybe (Fiber eff Unit)
}


initialState :: forall eff. State eff
initialState = {disabled:true, saving:Nothing, settings:UISettings {newUI: NewUISettings {enabled:false, facets:[]}}}

uiSettingsEditor :: ReactElement
uiSettingsEditor = createFactory (withStyles styles $ createLifecycleComponent (didMount LoadSetting) initialState render eval) {}
  where
  styles theme = {
    fab: {
      position: "absolute",
      bottom: 0,
      right: 16
    },
    enableColumn: {
      flexBasis: "33.3%"
    },
    facetColumn: {
      flexBasis: "50%"
    },
    facetConfig: {
      position:"relative",
      paddingBottom: 64
    },
    pathField: {
      marginLeft: theme.spacing.unit,
      marginRight: theme.spacing.unit,
      width: 300
    }
  }
  _enabled = prop (SProxy :: SProxy "enabled")
  _newUI = prop (SProxy :: SProxy "newUI")
  _settings = prop (SProxy :: SProxy "settings")
  _facets = prop (SProxy :: SProxy "facets")
  _name = prop (SProxy :: SProxy "name")
  _path = prop (SProxy :: SProxy "path")
  _enabledFlag = _Newtype <<< _newUI <<< _Newtype <<< _enabled
  _UIfacets = _Newtype <<< _newUI <<< _Newtype <<< _facets

  render s@{settings:UISettings uis@{newUI: (NewUISettings newUI)}} (ReactProps {classes}) (DispatchEff d) =
    let
      dis :: forall r. IProp (disabled::Boolean|r)
      dis = disabled $ not newUI.enabled
      facetEditor ind (FacetSetting {name,path}) = D.div' [
        textField [dis, label "Name", margin "normal", value name, changeField _name, placeholder "Name"],
        textField [className classes.pathField, dis, margin "normal", label "Path", value path, changeField _path, placeholder "/item/metadata/path" ],
        button [dis, onClick $ handle $ d \_ -> RemoveFacet ind ] [ icon_ [ text "delete"] ]
      ]
        where changeField l = onChange $ mkEffFn1 (d $ \e -> ModifyFacet ind $ set (_Newtype <<< l) e.target.value)
    in
    expansionPanelDetails_ [
      D.div [DP.className classes.enableColumn] [
        formControl_ [
          formControlLabel [ label "Enable new UI", control $ switch [checked newUI.enabled,
                          disabled s.disabled, onChange $ mkEffFn2 \e -> d $ SetNewUI ]]
                          ]],
      D.div [DP.className $ joinWith " " [classes.facetConfig, classes.facetColumn] ] $ [
          typography [variant subheading] [text "Search facets"]
        ] <> (mapWithIndex facetEditor newUI.facets) <>
        [
          button [dis, variant fab, className classes.fab, onClick $ handle $ d \e -> AddFacet] [ icon_ [text "add"] ]
        ]
    ]

  modifyFacets = modifyState <<< over (_settings <<< _UIfacets)
  modifyFacetsM f = modifyFacets \facets -> fromMaybe facets $ f facets

  save = do
    {saving} <- getState
    this <- ask
    newFiber <- lift $ forkAff $ do
      delay (Milliseconds 1000.0)
      {settings} <- runReaderT getState this
      void $ put_ (baseUrl <> "api/settings/ui") $ encodeJson settings
    modifyState _{saving=Just newFiber}
    lift $ maybe (pure unit) (killFiber (error "")) saving

  eval (LoadSetting) = do
    result <- lift $ get $ baseUrl <> "api/settings/ui"
    either (lift <<< log) (\r -> modifyState _ {settings=r, disabled=false}) $ decodeJson result.response
  eval AddFacet = do
    modifyFacets (flip snoc $ FacetSetting {name:"",path:""})
    save
  eval (RemoveFacet ind) = do
    modifyFacetsM $ deleteAt ind
    save
  eval (ModifyFacet ind f) = do
    modifyFacetsM $ modifyAt ind f
    save
  eval (SetNewUI v) = do
    modifyState $ set (_settings <<< _enabledFlag) v
    save
