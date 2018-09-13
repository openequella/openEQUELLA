module OEQ.MainUI.SettingsPage where

import Prelude

import Control.Monad.Trans.Class (lift)
import Data.Argonaut (class DecodeJson, decodeJson, (.?), (.??))
import Data.Array (mapMaybe, sortWith)
import Data.Either (either)
import Data.Maybe (Maybe(..), maybe)
import Data.Tuple (Tuple(..))
import Dispatcher (affAction)
import Dispatcher.React (modifyState, renderer)
import Effect.Class.Console (log)
import Foreign.Object as SM
import MaterialUI.CircularProgress (circularProgress)
import MaterialUI.ExpansionPanel (expansionPanel_)
import MaterialUI.ExpansionPanelDetails (expansionPanelDetails_)
import MaterialUI.ExpansionPanelSummary (expandIcon, expansionPanelSummary)
import MaterialUI.List (list_)
import MaterialUI.ListItem (listItem_)
import MaterialUI.ListItemText (listItemText, primary, secondary)
import MaterialUI.Properties (className)
import MaterialUI.Styles (withStyles)
import MaterialUI.Typography (typography)
import Network.HTTP.Affjax (get)
import Network.HTTP.Affjax.Response (json)
import OEQ.Data.Settings (Setting(..))
import OEQ.Environment (baseUrl, prepLangStrings)
import OEQ.MainUI.Template (template', templateDefaults)
import OEQ.UI.Icons (expandMoreIcon)
import OEQ.UI.Settings.UISettings (uiSettingsEditor)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (a, div, text) as D
import React.DOM.Props (_id)
import React.DOM.Props as DP

type State = {
  settings :: Maybe (Array Setting)
}

data Command = LoadSettings

settingsPage :: {legacyMode::Boolean} -> ReactElement
settingsPage = unsafeCreateLeafElement $ withStyles styles $ component "SettingsPage" $ \this -> do
  let 
    groupDetails :: Array (Tuple String { name :: String, desc :: String })
    groupDetails = [
      Tuple "general" string.general,
      Tuple "integration" string.integration,
      Tuple "diagnostics" string.diagnostics,
      Tuple "ui" string.ui
    ]

    string = prepLangStrings rawStrings
    coreString = prepLangStrings coreStrings 

    render {state:{settings}, props:{legacyMode,classes}} = if not legacyMode
                        then template' (templateDefaults coreString.title) [ mainContent ]
                        else mainContent
      where
      mainContent = maybe (D.div [DP.className classes.progress] [ circularProgress [] ]) renderSettings settings
      renderSettings allSettings =
        let groupMap = SM.fromFoldableWith append $ (\(Setting s) -> Tuple s.group [s]) <$> allSettings
            renderGroup (Tuple id details) | Just _pages <- SM.lookup id groupMap =
              let pages = sortWith _.name _pages
                  linksOrEditor = case id of 
                    "ui" -> uiSettingsEditor
                    o -> expansionPanelDetails_ [ list_ $ mapMaybe pageLink pages ]
              in Just $ settingGroup details linksOrEditor
            renderGroup _ = Nothing
        in D.div [_id "settingsPage"] $ mapMaybe renderGroup groupDetails

      settingGroup {name,desc} contents = expansionPanel_ [
        expansionPanelSummary [expandIcon expandMoreIcon ] [
          typography [className classes.heading] [ D.text name ],
          typography [className classes.secondaryHeading] [ D.text desc ]
        ],
        contents
      ]

      pageLink s@{pageUrl:Just pageUrl} = Just $ listItem_ [
        listItemText [
          primary $ D.a [DP.href pageUrl] [ D.text s.name ],
          secondary s.description
        ]
      ]
      pageLink _ = Nothing

    eval (LoadSettings) = do
      result <- lift $ get json $ baseUrl <> "api/settings"
      either (lift <<< log) (\r -> modifyState _ {settings=Just r}) $ decodeJson result.response

  pure {state:{settings:Nothing} :: State, render: renderer render this, componentDidMount: affAction this $ eval LoadSettings}
  where 
  styles theme = {
    heading: {
      fontSize: theme.typography.pxToRem 15,
      flexBasis: "33.33%",
      flexShrink: 0
    },
    secondaryHeading: {
      fontSize: theme.typography.pxToRem 15,
      color: theme.palette.text.secondary
    }, 
    progress: {
      display: "flex", 
      marginTop: theme.spacing.unit * 4,
      justifyContent: "center"
    } 
  }

type GroupStrings = { name :: String, desc :: String }


rawStrings = {prefix: "settings", 
  strings: {
    general: {name:"General",desc:"General settings"},
    integration: {name:"Integrations",desc:"Settings for integrating with external systems"},
    diagnostics: {name:"Diagnostics",desc:"Diagnostic pages"},
    ui: {name:"UI",desc:"UI settings"}
  }
}

coreStrings = {prefix: "com.equella.core", strings: { title: "Settings" }}
