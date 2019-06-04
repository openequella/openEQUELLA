module OEQ.MainUI.SettingsPage where

import Prelude

import Common.Strings (languageStrings)
import Control.Monad.Trans.Class (lift)
import Data.Argonaut (decodeJson)
import Data.Array (mapMaybe, sortWith)
import Data.Either (either)
import Data.Maybe (Maybe(..), maybe)
import Data.Tuple (Tuple(..))
import Dispatcher (affAction)
import Dispatcher.React (modifyState, renderer)
import Effect (Effect)
import Effect.Class.Console (log)
import Effect.Uncurried (mkEffectFn1)
import Foreign.Object as SM
import MaterialUI.CircularProgress (circularProgress_)
import MaterialUI.Dialog (dialog')
import MaterialUI.DialogContent (dialogContent_)
import MaterialUI.ExpansionPanel (expansionPanel_)
import MaterialUI.ExpansionPanelDetails (expansionPanelDetails_)
import MaterialUI.ExpansionPanelSummary (expansionPanelSummary)
import MaterialUI.List (list_)
import MaterialUI.ListItem (listItem_)
import MaterialUI.ListItemText (listItemText')
import MaterialUI.Styles (withStyles)
import MaterialUI.Typography (typography)
import Network.HTTP.Affjax (get)
import Network.HTTP.Affjax.Response (json)
import OEQ.Data.Settings (Setting(..))
import OEQ.Environment (baseUrl)
import OEQ.MainUI.TSRoutes (TemplateUpdateCB, link, runTemplateUpdate, toLocation)
import OEQ.MainUI.Template (templateDefaults)
import OEQ.UI.Icons (expandMoreIcon)
import OEQ.UI.Settings.UISettings (uiSettingsEditor)
import React (ReactClass, component, unsafeCreateLeafElement)
import React.DOM (a, div, div', text) as D
import React.DOM.Props (_id)
import React.DOM.Props as DP
import TSComponents (adminDownloadDialogClass)

type State = {
  settings :: Maybe (Array Setting),
  adminDialogOpen :: Boolean
}

data Command = LoadSettings | DialogOpen Boolean

settingsPageClass :: ReactClass {updateTemplate :: TemplateUpdateCB, refreshUser :: Effect Unit }
settingsPageClass = withStyles styles $ component "SettingsPage" $ \this -> do
  let 
    d = eval >>> affAction this
    groupDetails :: Array (Tuple String { name :: String, desc :: String })
    groupDetails = [
      Tuple "general" string.general,
      Tuple "integration" string.integration,
      Tuple "diagnostics" string.diagnostics,
      Tuple "ui" string.ui
    ]

    string = languageStrings.settings
    coreString = languageStrings."com.equella.core"

    render {state:state@{settings}, props:{refreshUser, classes}} = mainContent
      where
      mainContent = maybe (D.div [DP.className classes.progress] [ circularProgress_ [] ]) renderSettings settings
      renderSettings allSettings =
        let groupMap = SM.fromFoldableWith append $ (\(Setting s) -> Tuple s.group [s]) <$> allSettings
            renderGroup (Tuple id details) | Just _pages <- SM.lookup id groupMap =
              let pages = sortWith _.name _pages
                  linksOrEditor = case id of 
                    "ui" -> uiSettingsEditor {refreshUser}
                    o -> expansionPanelDetails_ [ list_ $ map pageLink pages ]
              in Just $ settingGroup details linksOrEditor
            renderGroup _ = Nothing
        in D.div [_id "settingsPage"] $ mapMaybe renderGroup groupDetails <> [
          unsafeCreateLeafElement adminDownloadDialogClass {open: state.adminDialogOpen, onClose: mkEffectFn1 \_ -> d $ DialogOpen false } 
        ]

      settingGroup {name,desc} contents = expansionPanel_ [
        expansionPanelSummary {expandIcon: expandMoreIcon} [
          typography {className: classes.heading} [ D.text name ],
          typography {className: classes.secondaryHeading} [ D.text desc ]
        ],
        contents
      ]

      pageLink s@{id:"adminconsole"} = listItem_ [
        listItemText' {primary: D.a [DP.onClick $ \_ -> d $ DialogOpen true ] [D.text s.name], secondary:s.description} 
      ]
      pageLink s = listItem_ [
        listItemText' {
          primary: (case s.route, s.href of 
            Just route, _ -> link { to: toLocation route }
            _, Just href -> D.a [DP.href href]
            _, _ -> D.div')
                [ D.text s.name ],
          secondary: s.description
        }
      ]

    eval (DialogOpen b) = modifyState _ {adminDialogOpen = b}
    eval (LoadSettings) = do
      runTemplateUpdate \_ -> templateDefaults coreString.title
      result <- lift $ get json $ baseUrl <> "api/settings"
      either (lift <<< log) (\r -> modifyState _ {settings=Just r}) $ decodeJson result.response

  pure {state: { 
      settings:Nothing, 
      adminDialogOpen:false
    } :: State, 
        render: renderer render this, 
        componentDidMount: affAction this $ eval LoadSettings}
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
