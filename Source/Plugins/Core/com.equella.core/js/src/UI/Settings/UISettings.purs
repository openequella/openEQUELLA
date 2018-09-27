module OEQ.UI.Settings.UISettings where

import Prelude

import Control.Monad.Reader (runReaderT)
import Control.Monad.Trans.Class (lift)
import Data.Argonaut (decodeJson, encodeJson)
import Data.Array (deleteAt, mapWithIndex, modifyAt, snoc)
import Data.Either (either)
import Data.Lens (over)
import Data.Lens.Iso.Newtype (_Newtype)
import Data.Lens.Record (prop)
import Data.Lens.Setter (set)
import Data.Maybe (Maybe(..), fromMaybe, maybe)
import Data.Symbol (SProxy(..))
import Dispatcher (affAction)
import Dispatcher.React (getState, modifyState, renderer)
import Effect.Aff (Fiber, Milliseconds(..), delay, error, forkAff, killFiber)
import Effect.Class.Console (log)
import Effect.Uncurried (mkEffectFn2)
import MaterialUI.Button (button)
import MaterialUI.Enums (fab, subheading)
import MaterialUI.ExpansionPanelDetails (expansionPanelDetails_)
import MaterialUI.FormControl (formControl_)
import MaterialUI.FormControlLabel (formControlLabel')
import MaterialUI.Icon (icon_)
import MaterialUI.Styles (withStyles)
import MaterialUI.Switch (switch')
import MaterialUI.TextField (textField')
import MaterialUI.Typography (typography)
import Network.HTTP.Affjax (get, put_)
import Network.HTTP.Affjax.Request (json)
import Network.HTTP.Affjax.Response (json) as Resp
import OEQ.Data.Settings (FacetSetting(..), NewUISettings(..), UISettings(..))
import OEQ.Environment (baseUrl, prepLangStrings)
import OEQ.UI.Common (textChange)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (text)
import React.DOM as D
import React.DOM.Props as DP


data Command = LoadSetting | SetNewUI Boolean | SetNewSearch Boolean
              | ModifyFacet Int (FacetSetting -> FacetSetting) | RemoveFacet Int
              | AddFacet

type State eff = {
  disabled :: Boolean,
  settings :: UISettings,
  saving :: Maybe (Fiber Unit)
}

initialState :: forall eff. State eff
initialState = {disabled:true, saving:Nothing, settings:UISettings {newUI: NewUISettings {enabled:false, newSearch: false, facets:[]}}}

uiSettingsEditor :: ReactElement
uiSettingsEditor = flip unsafeCreateLeafElement {} $ withStyles styles $ component "UISettings" $ \this -> do 
  let
    d = eval >>> affAction this
    string = prepLangStrings rawStrings
    _newSearch = prop (SProxy :: SProxy "newSearch")
    _enabled = prop (SProxy :: SProxy "enabled")
    _newUI = prop (SProxy :: SProxy "newUI")
    _settings = prop (SProxy :: SProxy "settings")
    _facets = prop (SProxy :: SProxy "facets")
    _name = prop (SProxy :: SProxy "name")
    _path = prop (SProxy :: SProxy "path")
    _newUISettings = _settings <<< _Newtype <<< _newUI <<< _Newtype

    render {state: s@{settings:UISettings uis@{newUI: (NewUISettings newUI)}}, props: {classes}} =
      let
        disabled = not newUI.enabled
        facetEditor ind (FacetSetting {name,path}) = D.div' [
          textField' {disabled, 
            label: string.facet.name, 
            value: name, 
            onChange: changeField _name, 
            placeholder: string.facet.name},
          textField' {className: classes.pathField, disabled, label: "Path", value: path, onChange: changeField _path,
            placeholder: "/item/metadata/path" },
          button {disabled, onClick: d $ RemoveFacet ind } [ icon_ [ text "delete"] ]
        ]
          where changeField l = textChange d (ModifyFacet ind <<< set (_Newtype <<< l))
      in
      expansionPanelDetails_ [
        D.div [DP.className classes.enableColumn] [
          formControl_ [
            formControlLabel' { label: string.enableNew, control: switch' { checked: newUI.enabled,
                            disabled: s.disabled, onChange: mkEffectFn2 $ \e -> d <<< SetNewUI} }
          ]
        ],
        D.div [DP.className classes.facetColumn] $ [
          formControl_ [
            formControlLabel' {label: string.enableSearch, control: switch' {checked: newUI.newSearch,
                            disabled, onChange: mkEffectFn2 \e -> d <<< SetNewSearch }}
          ],
          D.div [DP.className classes.facetConfig ] $ [
            typography {variant: subheading} [text string.facet.title]
          ] <> (mapWithIndex facetEditor newUI.facets) <>
          [
            button {disabled, variant: fab, className: classes.fab, onClick: d AddFacet} [ icon_ [text "add"] ]
          ]
        ]
      ]

    modifyFacets = modifyState <<< over (_newUISettings <<< _facets)
    modifyFacetsM f = modifyFacets \facets -> fromMaybe facets $ f facets

    save = do
      {saving} <- getState
      newFiber <- lift $ forkAff $ do
        delay (Milliseconds 1000.0)
        {settings} <- runReaderT getState this
        void $ put_ (baseUrl <> "api/settings/ui") $ json $ encodeJson settings
      modifyState _{saving=Just newFiber}
      lift $ maybe (pure unit) (killFiber (error "")) saving

    eval LoadSetting = do
      result <- lift $ get Resp.json $ baseUrl <> "api/settings/ui"
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
      modifyState $ set (_newUISettings <<< _enabled) v
      save
    eval (SetNewSearch v) = do
      modifyState $ set (_newUISettings <<< _newSearch) v
      save
  pure {state:initialState, render: renderer render this, componentDidMount: d LoadSetting}
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


rawStrings :: { prefix :: String
, strings :: { facet :: { name :: String
                        , path :: String
                        , title :: String
                        }
             , enableNew :: String
             , enableSearch :: String
             }
}
rawStrings = {
  prefix: "uiconfig", 
  strings: {
    facet: {
      name: "Name",
      path: "Path",
      title: "Search facets"
    },
    enableNew: "Enable new UI",
    enableSearch: "Enable new search page"
  }
}
