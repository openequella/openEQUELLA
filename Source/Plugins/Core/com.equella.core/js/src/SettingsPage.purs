module SettingsPage where

import Prelude

import Control.Monad.Aff.Console (log)
import Control.Monad.Eff (Eff)
import Control.Monad.Eff.Console (CONSOLE)
import Control.Monad.Trans.Class (lift)
import DOM (DOM)
import Data.Argonaut (class DecodeJson, decodeJson, (.?), (.??))
import Data.Array (mapMaybe, sortWith)
import Data.Either (either)
import Data.Maybe (Maybe(..), maybe)
import Data.StrMap as SM
import Data.Tuple (Tuple(..))
import Dispatcher.React (ReactProps(ReactProps), createLifecycleComponent, didMount, modifyState)
import EQUELLA.Environment (baseUrl)
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
import Network.HTTP.Affjax (AJAX, get)
import React (ReactElement, createFactory)
import React.DOM as D
import React.DOM.Props as D
import Settings.UISettings (uiSettingsEditor)
import Template (renderData, renderMain, renderReact, template)

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


groupDetails :: Array (Tuple String { n :: String, d :: String })
groupDetails = [
  Tuple "general" {n:"General",d:"General settings"},
  Tuple "integration" {n:"Integrations",d:"Settings for integrating with external systems"},
  Tuple "diagnostics" {n:"Diagnostics",d:"Diagnostic pages"},
  Tuple "ui" {n:"UI",d:"UI settings"}
]

type State = {
  settings :: Maybe (Array Setting)
}

data Command = LoadSettings

initialState :: State
initialState = {settings:Nothing}

settingsPage :: ReactElement
settingsPage = createFactory (withStyles styles $ createLifecycleComponent (didMount LoadSettings) initialState render eval) {}
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
    }
  }
  render {settings} (ReactProps {classes})= if renderData.newUI
                      then template {mainContent,titleExtra:Nothing}
                      else mainContent
    where
    mainContent = maybe (D.div' []) renderSettings settings
    renderSettings s =
      let groupMap = SM.fromFoldableWith append $ (\(Setting s) -> Tuple s.group [s]) <$> s
          renderGroup (Tuple "ui" details) = Just $ settingGroup details uiSettingsEditor
          renderGroup (Tuple id details) | Just _pages <- SM.lookup id groupMap =
            let pages = sortWith _.name _pages
            in Just $ settingGroup details $ expansionPanelDetails_ [ list_ $ mapMaybe pageLink pages ]
          renderGroup _ = Nothing
      in D.div' $ mapMaybe renderGroup groupDetails

    settingGroup {n,d} contents = expansionPanel_ [
      expansionPanelSummary [expandIcon $ icon_ [D.text "expand_more"] ] [
        typography [className classes.heading] [ D.text n ],
        typography [className classes.secondaryHeading] [ D.text d ]
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

main :: forall eff. Eff (dom :: DOM, ajax :: AJAX, console::CONSOLE | eff) Unit
main = (if renderData.newUI then renderMain else renderReact "settingsPage") $ settingsPage
