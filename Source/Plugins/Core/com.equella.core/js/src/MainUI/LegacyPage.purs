module OEQ.MainUI.LegacyPage where

import Prelude hiding (div)

import Control.Monad.Trans.Class (lift)
import Data.Array (catMaybes)
import Data.Either (Either(..))
import Data.Maybe (Maybe(..), isJust, maybe)
import Data.Nullable (toNullable)
import Data.String (joinWith)
import Dispatcher (affAction)
import Dispatcher.React (getProps, getState, modifyState, renderer, saveRef, withRef)
import OEQ.API.LegacyContent (submitRequest)
import OEQ.Data.Error (ErrorResponse)
import OEQ.UI.LegacyContent (FormUpdate, divWithHtml, setupLegacyHooks, updateIncludes, updateStylesheets, writeForm)
import OEQ.Data.LegacyContent (ContentResponse(..), SubmitOptions)
import Effect.Aff (runAff_)
import Effect.Class (liftEffect)
import Effect.Ref (new)
import Foreign.Object (Object, lookup)
import Foreign.Object as Object
import MaterialUI.Card (card)
import MaterialUI.CardContent (cardContent)
import MaterialUI.CircularProgress (circularProgress)
import MaterialUI.Color (inherit)
import MaterialUI.Icon (icon_)
import MaterialUI.IconButton (iconButton)
import MaterialUI.Popover (anchorEl, anchorOrigin, marginThreshold, popover)
import MaterialUI.Properties (color, onClick, onClose, open, variant)
import MaterialUI.Styles (withStyles)
import MaterialUI.TextStyle (display2, headline)
import MaterialUI.Typography (typography)
import MaterialUI.Typography as Color
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (div, text)
import React.DOM as D
import React.DOM.Props (_id)
import React.DOM.Props as DP
import OEQ.MainUI.Routes (LegacyURI(..), matchRoute, pushRoute)
import OEQ.MainUI.Template (refreshUser, template', templateDefaults)
import OEQ.UI.Common (scrollWindowToTop, withCurrentTarget)
import Web.HTML (HTMLElement, window)
import Web.HTML.Location (assign)
import Web.HTML.Window (location)

type PageContent = {
  html:: Object String,
  script :: String,
  title :: String, 
  fullscreenMode :: String, 
  menuMode :: String,
  hideAppBar :: Boolean
}

data Command = OptionsAnchor (Maybe HTMLElement) 
  | Submit SubmitOptions
  | LoadPage 
  | Updated LegacyURI
  | UpdateForm FormUpdate

type State = {
  optionsAnchor::Maybe HTMLElement, 
  content :: Maybe PageContent,
  errored :: Maybe ErrorResponse,
  state :: Object (Array String),
  pagePath :: String,
  noForm :: Boolean
}

legacy :: {page :: LegacyURI} -> ReactElement
legacy = unsafeCreateLeafElement $ withStyles styles $ component "LegacyPage" $ \this -> do
  tempRef <- new Nothing
  let 
    d = eval >>> affAction this
  
    render {state:s@{content,errored}, props:{classes}} = case content of 
        Just (c@{html,title,script}) -> 
          let extraClass = case c.fullscreenMode of 
                "YES" -> []
                "YES_WITH_TOOLBAR" -> []
                _ -> case c.menuMode of
                  "HIDDEN" -> [] 
                  _ -> [classes.withPadding]
              
              actualContent = D.div [DP.className $ joinWith " " $ ["content"] <> extraClass] $ catMaybes [ 
                  (divWithHtml <<< {divProps:[_id "breadcrumbs"], script:Nothing, html: _} <$> lookup "crumbs" html),
                  (divWithHtml <<< {divProps:[], script:Nothing, html: _} <$> lookup "upperbody" html),
                  (divWithHtml <<< {divProps:[], script:Just script, html: _} <$> lookup "body" html) ]
              mainContent = if s.noForm 
                then actualContent
                else writeForm s.state actualContent
          in template' (templateDefaults title) {
                                menuExtra = toNullable $ options <$> lookup "so" html, 
                                innerRef = toNullable $ Just $ saveRef tempRef, 
                                hideAppBar = c.hideAppBar, 
                                menuMode = c.menuMode, 
                                fullscreenMode = c.fullscreenMode, 
                                errorResponse = toNullable errored
                          } [ mainContent ] 
        Nothing | Just {code,error,description} <- errored -> template' (templateDefaults error) [ 
          div [DP.className classes.errorPage] [
            card [] [
              cardContent [] $ catMaybes [
                Just $ typography [variant display2, color Color.error] [ text $ show code <> " : " <> error], 
                description <#> \desc -> typography [variant headline] [ text desc ]
              ]
            ]
          ]
        ]
        Nothing -> D.div [ DP.className classes.progress ] [ circularProgress [] ]
      where
      options html = [ 
          iconButton [color inherit, onClick $ withCurrentTarget $ d <<< OptionsAnchor <<< Just] [ icon_ [text "more_vert"] ],
          popover [ open $ isJust s.optionsAnchor, marginThreshold 64
              , anchorOrigin {vertical:"bottom",horizontal:"left"}
              , onClose (\_ -> d $ OptionsAnchor Nothing)
              , anchorEl $ toNullable s.optionsAnchor ] 
          [ 
              divWithHtml {divProps:[DP.className $ classes.screenOptions], html, script:Nothing}
          ]
      ]


    submitWithPath fullError path opts = do 
        (lift $ submitRequest path opts) >>= case _ of 
          Left errorPage -> modifyState \s -> s {errored = Just errorPage, 
                        content = if fullError then Nothing else s.content}
          Right resp -> updateContent resp

    eval = case _ of 
      (OptionsAnchor el) -> modifyState _ {optionsAnchor = el}
      Updated oldPage -> do 
        {page} <- getProps
        if oldPage /= page then eval LoadPage else pure unit
      LoadPage -> do 
        {page: LegacyURI _pagePath params} <- getProps 
        let pagePath = case _pagePath of 
              "" -> "home.do"
              o -> o
        modifyState _ {pagePath = pagePath}
        submitWithPath true pagePath {vals: params, callback: toNullable Nothing}
        liftEffect $ scrollWindowToTop

      Submit s -> do 
        modifyState _ {optionsAnchor = Nothing}
        {pagePath} <- getState
        submitWithPath false pagePath s
      UpdateForm {state,partial} -> do 
        modifyState \s -> s {state = if partial then Object.union s.state state else state}

    doRefresh true = liftEffect $ withRef tempRef refreshUser
    doRefresh _ = pure unit

    updateContent (Callback cb) = liftEffect cb
    updateContent (Redirect href) = do 
      liftEffect $ window >>= location >>= assign href
    updateContent (ChangeRoute redir userUpdated) = do 
      doRefresh userUpdated
      liftEffect $ maybe (pure unit) pushRoute $ matchRoute redir
    updateContent (LegacyContent lc@{css, js, state, html,script, title, fullscreenMode, menuMode, hideAppBar} userUpdated) = do 
      doRefresh userUpdated
      lift $ updateIncludes true css js
      modifyState \s -> s {noForm = lc.noForm,
        content = Just {html, script, title, fullscreenMode, menuMode, hideAppBar}, state = state}

  pure {
    state:{ 
      optionsAnchor:Nothing, 
      content: Nothing, 
      pagePath: "", 
      errored: Nothing, 
      state: Object.empty, 
      noForm: false
    } :: State, 
    render: renderer render this, 
    componentDidMount: do 
      setupLegacyHooks (d <<< Submit) (d <<< UpdateForm)
      d $ LoadPage,
    componentDidUpdate: \{page} _ _ -> d $ Updated page,
    componentWillUnmount: runAff_ (const $ pure unit) $ updateStylesheets true []
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
