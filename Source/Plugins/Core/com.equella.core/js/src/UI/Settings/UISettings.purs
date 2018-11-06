module OEQ.UI.Settings.UISettings where

import Prelude

import Control.Monad.Reader (runReaderT)
import Control.Monad.Trans.Class (lift)
import Data.Argonaut (decodeJson, encodeJson)
import Data.Either (either)
import Data.Lens.Iso.Newtype (_Newtype)
import Data.Lens.Record (prop)
import Data.Lens.Setter (set)
import Data.Maybe (Maybe(..), maybe)
import Data.Symbol (SProxy(..))
import Dispatcher (affAction)
import Dispatcher.React (getState, modifyState, renderer)
import Effect.Aff (Fiber, Milliseconds(..), delay, error, forkAff, killFiber)
import Effect.Class.Console (log)
import Effect.Uncurried (mkEffectFn2)
import MaterialUI.ExpansionPanelDetails (expansionPanelDetails_)
import MaterialUI.FormControl (formControl_)
import MaterialUI.FormControlLabel (formControlLabel')
import MaterialUI.Styles (withStyles)
import MaterialUI.Switch (switch')
import Network.HTTP.Affjax (get, put_)
import Network.HTTP.Affjax.Request (json)
import Network.HTTP.Affjax.Response (json) as Resp
import OEQ.Data.Settings (NewUISettings(..), UISettings(..))
import OEQ.Environment (baseUrl, prepLangStrings)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM as D
import React.DOM.Props as DP


data Command = LoadSetting | SetNewUI Boolean | SetNewSearch Boolean
type State eff = {
  disabled :: Boolean,
  settings :: UISettings,
  saving :: Maybe (Fiber Unit)
}

initialState :: forall eff. State eff
initialState = {disabled:true, saving:Nothing, settings:UISettings {newUI: NewUISettings {enabled:false, newSearch: false}}}

uiSettingsEditor :: ReactElement
uiSettingsEditor = flip unsafeCreateLeafElement {} $ withStyles styles $ component "UISettings" $ \this -> do 
  let
    d = eval >>> affAction this
    string = prepLangStrings rawStrings
    _newSearch = prop (SProxy :: SProxy "newSearch")
    _enabled = prop (SProxy :: SProxy "enabled")
    _newUI = prop (SProxy :: SProxy "newUI")
    _settings = prop (SProxy :: SProxy "settings")
    _name = prop (SProxy :: SProxy "name")
    _path = prop (SProxy :: SProxy "path")
    _newUISettings = _settings <<< _Newtype <<< _newUI <<< _Newtype

    render {state: s@{settings:UISettings uis@{newUI: (NewUISettings newUI)}}, props: {classes}} =
      let
        disabled = not newUI.enabled
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
          ]
        ]
      ]

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
