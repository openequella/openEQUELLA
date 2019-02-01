module OEQ.MainUI.LegacyPage where

import Prelude hiding (div)

import Data.Array (catMaybes)
import Data.Maybe (Maybe(..), isJust, maybe)
import Data.Nullable (toMaybe, toNullable)
import Dispatcher (affAction)
import Dispatcher.React (modifyState, renderer, saveRef, withRef)
import Effect.Class (liftEffect)
import Effect.Ref (new)
import Foreign.Object (lookup)
import MaterialUI.Card (card_)
import MaterialUI.CardContent (cardContent_)
import MaterialUI.Enums (display2, headline)
import MaterialUI.Enums as String
import MaterialUI.Icon (icon_)
import MaterialUI.IconButton (iconButton)
import MaterialUI.Popover (popover)
import MaterialUI.Styles (withStyles)
import MaterialUI.Typography (typography)
import OEQ.Data.Error (ErrorResponse)
import OEQ.Data.LegacyContent (LegacyURI)
import OEQ.MainUI.Routes (forcePushRoute, matchRoute)
import OEQ.MainUI.Template (refreshUser, template', templateDefaults)
import OEQ.UI.Common (withCurrentTarget)
import OEQ.UI.LegacyContent (PageContent, divWithHtml, emptyContent, legacyContent)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (div, text)
import React.DOM.Props as DP
import Web.HTML (HTMLElement, window)
import Web.HTML.Location (assign)
import Web.HTML.Window (location)

data Command = 
    OptionsAnchor (Maybe HTMLElement) 
  | ContentUpdated PageContent
  | Redirected {href::String, external::Boolean}
  | Errored {error::ErrorResponse, fullScreen::Boolean}

type State = {
  optionsAnchor::Maybe HTMLElement, 
  content :: PageContent,
  errored :: Maybe ErrorResponse, 
  fullscreenError :: Boolean
}

legacy :: {page :: LegacyURI} -> ReactElement
legacy = unsafeCreateLeafElement $ withStyles styles $ component "LegacyPage" $ \this -> do
  tempRef <- new Nothing
  let 
    d = eval >>> affAction this
  
    render {state:s@{content:c@{contentId}}, props:{page,classes}} = case s.errored, s.fullscreenError of
      Just {code,error,description}, true -> template' (templateDefaults error) [ 
          div [DP.className classes.errorPage] [
            card_ [
              cardContent_ $ catMaybes [
                Just $ typography {variant: display2, color: String.error} [ text $ show code <> " : " <> error], 
                toMaybe description <#> \desc -> typography {variant: headline} [ text desc ]
              ]
            ]
          ]
        ]
      _, _ ->
        let options optionsHtml = [ 
                  iconButton {onClick: withCurrentTarget $ d <<< OptionsAnchor <<< Just} [ icon_ [text "more_vert"] ],
                  popover { 
                        open: isJust s.optionsAnchor
                      , marginThreshold: 64
                      , anchorOrigin: {vertical:"bottom",horizontal:"left"}
                      , onClose: d $ OptionsAnchor Nothing
                      , anchorEl: toNullable s.optionsAnchor }
                  [ 
                      divWithHtml {
                        contentId, 
                        divProps:[DP.className $ classes.screenOptions], 
                        html: optionsHtml, 
                        script:Nothing, 
                        afterHtml: Nothing
                      }
                  ]
              ]
            
          in template' (templateDefaults c.title) {
                menuExtra = toNullable $ options <$> lookup "so" c.html, 
                innerRef = toNullable $ Just $ saveRef tempRef, 
                hideAppBar = c.hideAppBar, 
                menuMode = c.menuMode, 
                fullscreenMode = c.fullscreenMode,
                preventNavigation = toNullable $ Just c.preventUnload, 
                errorResponse = toNullable s.errored
              } [ legacyContent {
                    page, 
                    contentUpdated: d <<< ContentUpdated, 
                    userUpdated: withRef tempRef refreshUser, 
                    redirected: d <<< Redirected, 
                    onError: d <<< Errored 
                } 
              ] 

    eval = case _ of 
      (OptionsAnchor el) -> modifyState _ {optionsAnchor = el}
      ContentUpdated c -> modifyState _ {content = c}
      Errored {error,fullScreen} -> modifyState _ {errored = Just error, fullscreenError = fullScreen }
      Redirected {href,external} -> liftEffect $ if external 
        then window >>= location >>= assign href    
        else maybe (pure unit) forcePushRoute $ matchRoute href 

  pure {
    state:{ 
      optionsAnchor:Nothing, 
      content: emptyContent, 
      errored: Nothing, 
      fullscreenError: false
    } :: State, 
    render: renderer render this
  }

  where
  styles t = {
    screenOptions: {
        margin: 20
    },
    withPadding: {
      padding: t.spacing.unit * 2
    }, 
    progress: {
      display: "flex",
      justifyContent: "center"
    }, 
    errorPage: {
      display: "flex",
      justifyContent: "center", 
      marginTop: t.spacing.unit * 8
    }
  }
