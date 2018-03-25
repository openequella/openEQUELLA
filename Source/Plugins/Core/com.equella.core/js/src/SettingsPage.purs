module SettingsPage where

import Prelude

import Control.Monad.Aff.Console (log)
import Control.Monad.Trans.Class (lift)
import Data.Argonaut (class DecodeJson, decodeJson, (.?), (.??))
import Data.Array (mapMaybe, sortWith)
import Data.Either (either)
import Data.Maybe (Maybe(..), maybe)
import Data.StrMap as SM
import Data.Tuple (Tuple(..))
import Dispatcher.React (ReactProps(ReactProps), createLifecycleComponent, didMount, modifyState)
import EQUELLA.Environment (baseUrl, prepLangStrings)
import MaterialUI.ExpansionPanel (expansionPanel_)
import MaterialUI.ExpansionPanelDetails (expansionPanelDetails_)
import MaterialUI.ExpansionPanelSummary (expandIcon, expansionPanelSummary)
import MaterialUI.Icon (icon_)
import MaterialUI.List (list_)
import MaterialUI.ListItem (listItem_)
import MaterialUI.ListItemText (listItemText, primary, secondary)
import MaterialUI.Properties (className)
import MaterialUI.Styles (withStyles)
import MaterialUI.Typography (typography)
import Network.HTTP.Affjax (get)
import React (ReactElement, createFactory)
import React.DOM (a, div, div', text) as D
import React.DOM.Props (_id)
import React.DOM.Props (href) as D
import Settings.UISettings (uiSettingsEditor)
import Template (template)

newtype Setting = Setting {
  id :: String,
  group :: String,
  name :: String,
  description :: String,
  pageUrl :: Maybe String
}

instance decodeSetting :: DecodeJson Setting where
  decodeJson v = do
    o <- decodeJson v
    id <- o .? "id"
    name <- o .? "name"
    group <- o .? "group"
    description <- o .? "description"
    links <- o .? "links"
    pageUrl <- links .?? "web"
    pure $ Setting {id,group,name,description,pageUrl}


type State = {
  settings :: Maybe (Array Setting)
}

data Command = LoadSettings

initialState :: State
initialState = {settings:Nothing}

settingsPage :: {legacyMode::Boolean} -> ReactElement
settingsPage = createFactory (withStyles styles $ createLifecycleComponent (didMount LoadSettings) initialState render eval)
  where
  groupDetails :: Array (Tuple String { name :: String, desc :: String })
  groupDetails = [
    Tuple "general" string.general,
    Tuple "integration" string.integration,
    Tuple "diagnostics" string.diagnostics,
    Tuple "ui" string.ui
  ]

  string = prepLangStrings rawStrings
  coreString = prepLangStrings coreStrings

  styles theme = {
    heading: {
      fontSize: theme.typography.pxToRem 15,
      flexBasis: "33.33%",
      flexShrink: 0
    },
    secondaryHeading: {
      fontSize: theme.typography.pxToRem 15,
      color: theme.palette.text.secondary
    }
  }
  render {settings} (ReactProps {legacyMode,classes})= if not legacyMode
                      then template {mainContent, title:coreString.title, titleExtra:Nothing}
                      else mainContent
    where
    mainContent = maybe (D.div' []) renderSettings settings
    renderSettings allSettings =
      let groupMap = SM.fromFoldableWith append $ (\(Setting s) -> Tuple s.group [s]) <$> allSettings
          renderGroup (Tuple "ui" details) = Just $ settingGroup details uiSettingsEditor
          renderGroup (Tuple id details) | Just _pages <- SM.lookup id groupMap =
            let pages = sortWith _.name _pages
            in Just $ settingGroup details $ expansionPanelDetails_ [ list_ $ mapMaybe pageLink pages ]
          renderGroup _ = Nothing
      in D.div [_id "settingsPage"] $ mapMaybe renderGroup groupDetails

    settingGroup {name,desc} contents = expansionPanel_ [
      expansionPanelSummary [expandIcon $ icon_ [D.text "expand_more"] ] [
        typography [className classes.heading] [ D.text name ],
        typography [className classes.secondaryHeading] [ D.text desc ]
      ],
      contents
    ]

    pageLink s@{pageUrl:Just pageUrl} = Just $ listItem_ [
      listItemText [
        primary $ D.a [D.href pageUrl] [ D.text s.name ],
        secondary s.description
      ]
    ]
    pageLink _ = Nothing

  eval (LoadSettings) = do
    result <- lift $ get $ baseUrl <> "api/settings"
    either (lift <<< log) (\r -> modifyState _ {settings=Just r}) $ decodeJson result.response

type GroupStrings = { name :: String, desc :: String }

rawStrings :: Tuple String
  { general :: GroupStrings
  , integration :: GroupStrings
  , diagnostics :: GroupStrings
  , ui :: GroupStrings
  }
rawStrings = Tuple "settings" {
  general: {name:"General",desc:"General settings"},
  integration: {name:"Integrations",desc:"Settings for integrating with external systems"},
  diagnostics: {name:"Diagnostics",desc:"Diagnostic pages"},
  ui: {name:"UI",desc:"UI settings"}
}

coreStrings :: Tuple String
  { title :: String
  }
coreStrings = Tuple "com.equella.core" {
  title: "Settings"
}
