module LegacyPage where

import Prelude

import Control.Monad.Maybe.Trans (MaybeT(..), runMaybeT)
import Control.Monad.Trans.Class (lift)
import Data.Argonaut (class DecodeJson, Json, decodeJson, encodeJson, (.?), (.??))
import Data.Array (catMaybes, filter, length)
import Data.Either (Either(..), either)
import Data.Foldable (sequence_, traverse_)
import Data.FunctorWithIndex (mapWithIndex)
import Data.Map as Map
import Data.Maybe (Maybe(..), fromJust, isJust, maybe)
import Data.Nullable (Nullable, toMaybe, toNullable)
import Data.Set (Set)
import Data.Set as Set
import Data.String (joinWith)
import Data.Traversable (traverse)
import Data.Tuple (Tuple(..))
import Dispatcher (affAction)
import Dispatcher.React (getProps, getState, modifyState, propsRenderer, renderer, saveRef, withRef)
import EQUELLA.Environment (baseUrl)
import Effect (Effect)
import Effect.Aff (Aff, makeAff, nonCanceler, runAff_)
import Effect.Aff.Compat (EffectFn1, EffectFn2, mkEffectFn1, mkEffectFn2, runEffectFn1)
import Effect.Class (liftEffect)
import Effect.Class.Console (log)
import Effect.Ref (new)
import Effect.Ref as Ref
import Foreign.Object (Object, lookup)
import Foreign.Object as Object
import MaterialUI.Color (inherit)
import MaterialUI.Icon (icon_)
import MaterialUI.IconButton (iconButton)
import MaterialUI.Modal (open)
import MaterialUI.Popover (anchorEl, anchorOrigin, marginThreshold, popover)
import MaterialUI.Properties (color, onClick, onClose)
import MaterialUI.Styles (withStyles)
import Network.HTTP.Affjax (post)
import Network.HTTP.Affjax.Request as Req
import Network.HTTP.Affjax.Response as Resp
import Partial.Unsafe (unsafePartial)
import QueryString (toTuples)
import React (ReactElement, ReactRef, component, unsafeCreateLeafElement)
import React as R
import React.DOM (text)
import React.DOM as D
import React.DOM.Props (Props, _id, _type)
import React.DOM.Props as DP
import Record (merge)
import Routes (LegacyURI(..), Route(..), pushRoute)
import Template (refreshUser, template, template', templateDefaults)
import Utils.UI (withCurrentTarget)
import Web.DOM.Document (createElement, getElementsByTagName, toNonElementParentNode)
import Web.DOM.Element as Elem
import Web.DOM.HTMLCollection (item, toArray)
import Web.DOM.Node (appendChild, insertBefore, removeChild)
import Web.DOM.NonDocumentTypeChildNode (previousElementSibling)
import Web.DOM.NonElementParentNode (getElementById)
import Web.Event.Event (EventType(..))
import Web.Event.EventTarget (addEventListener, eventListener)
import Web.HTML (HTMLElement, window)
import Web.HTML.HTMLDocument (toDocument)
import Web.HTML.HTMLLinkElement as Link
import Web.HTML.HTMLScriptElement as Script
import Web.HTML.Window (document)

foreign import setInnerHtml :: {node :: ReactRef, html:: String, script::Nullable String } -> Effect Unit
foreign import clearInnerHtml :: ReactRef -> Effect Unit
foreign import globalEval :: String -> Effect Unit

type SubmitOptions = {vals::Object (Array String), callback :: Nullable (EffectFn1 Json Unit)} 

foreign import setupLegacyHooks :: {
    submit:: EffectFn1 SubmitOptions Unit, 
    updateIncludes :: EffectFn2 {css :: Array String, js :: Array String, script::String} (Effect Unit) Unit,
    updateForm :: EffectFn1 {state :: Object (Array String), partial :: Boolean} Unit
  } -> Effect Unit

type LegacyContentR = {
  -- baseResources::String, 
  html:: Object String, 
  state :: Object (Array String),
  css :: Array String, 
  js :: Array String,
  script :: String, 
  title :: String, 
  fullscreenMode :: String, 
  menuMode :: String
  -- menuItems :: Array (Array MenuItem), 
  -- hideAppBar :: Boolean,
  -- newUI::Boolean, 
  -- user::UserData
} 

type PageContent = {
  html:: Object String,
  script :: String,
  title :: String, 
  fullscreenMode :: String, 
  menuMode :: String
}

data ContentResponse = Redirect (Object (Array String)) String Boolean | LegacyContent LegacyContentR Boolean

divWithHtml :: {divProps :: Array Props, html :: String, script :: Maybe String} -> ReactElement
divWithHtml = unsafeCreateLeafElement $ component "JQueryDiv" $ \this -> do
  domNode <- Ref.new Nothing
  let
    render {divProps,html} = D.div (divProps <> [ DP.ref $ runEffectFn1 $ saveRef domNode ]) []
    updateHtml = do 
      {html, script} <- R.getProps this
      withRef domNode $ \node -> setInnerHtml {node,html,script: toNullable script}
  pure {
    render: propsRenderer render this,
    componentDidMount: updateHtml,
    componentWillUnmount: withRef domNode clearInnerHtml,
    componentDidUpdate: \_ _  _ -> updateHtml,
    shouldComponentUpdate: \{html} _ -> do 
      {html:newhtml} <- R.getProps this
      pure $ html /= newhtml
  }

data Command = OptionsAnchor (Maybe HTMLElement) 
  | Submit SubmitOptions 
  | LoadPage 
  | Updated LegacyURI
  | UpdateForm {state :: Object (Array String), partial :: Boolean}

type State = {
  optionsAnchor::Maybe HTMLElement, 
  content :: Maybe PageContent,
  state :: Object (Array String),
  pagePath :: String,
  stylesheets :: Array String
}

resolveUrl :: String -> String 
resolveUrl u = baseUrl <> u

filterUrls :: Set String -> Array String -> Array String 
filterUrls existing = filter (not <<< flip Set.member existing)

loadMissingScripts :: Array String -> Aff Unit
loadMissingScripts _scripts =  unsafePartial $ makeAff $ \cb -> do 
  let scripts = resolveUrl <$> _scripts
  w <- window
  htmldoc <- document w
  let doc = toDocument htmldoc
      
  head <- fromJust <$> (getElementsByTagName "head" doc >>= item 0)
  loadedScripts <- getElementsByTagName "script" doc >>= toArray
  let getSrc elem = Script.src $ fromJust $ Script.fromElement elem
  ex <- Set.fromFoldable <$> traverse getSrc loadedScripts
  let toLoad = filterUrls ex scripts
      scriptCount = length toLoad
  let createScript ind src = do 
        tag <- fromJust <<< Script.fromElement <$> createElement "script" doc
        Script.setSrc src tag
        Script.setAsync false tag
        if scriptCount == ind + 1
          then do 
            el <- eventListener (\_ -> cb $ Right unit)
            addEventListener (EventType "load") el false (Script.toEventTarget tag)
          else pure unit
        appendChild (Script.toNode tag) (Elem.toNode head)
  sequence_ $ mapWithIndex createScript toLoad
  if scriptCount == 0 then (cb $ Right unit) else pure unit
  pure nonCanceler


updateStylesheets :: Boolean -> Array String -> Aff Unit
updateStylesheets replace _sheets = unsafePartial $ makeAff $ \cb -> do 
  let sheets = resolveUrl <$> _sheets
  w <- window
  htmldoc <- document w
  let doc = toDocument htmldoc
      findPreviousLinks e = previousElementSibling (Elem.toNonDocumentTypeChildNode e) >>= case _ of 
        Just prevElem | Just l <- Link.fromElement prevElem -> do 
          href <- Link.href l 
          map (append [Tuple href l]) $ findPreviousLinks $ prevElem
        _ -> pure []

  {head, insertPoint, previous} <- map fromJust $ runMaybeT $ do 
    head <- Elem.toNode <$> MaybeT (getElementsByTagName "head" doc >>= item 0)
    insertPoint <- MaybeT $ getElementById "_dynamicInsert" (toNonElementParentNode doc)
    previous <- lift $ Map.fromFoldable <$> findPreviousLinks insertPoint
    pure $ {head, insertPoint, previous}
  let newSheets = (filterUrls (Map.keys previous) sheets)
      sheetCount = length newSheets
      createLink ind href = do 
        l <- fromJust <<< Link.fromElement <$> createElement "link" doc
        Link.setRel "stylesheet" l
        Link.setHref href l
        if sheetCount == ind + 1
          then do 
            el <- eventListener (\_ -> cb $ Right unit)
            addEventListener (EventType "load") el false (Link.toEventTarget l)
          else pure unit
        insertBefore (Link.toNode l) (Elem.toNode insertPoint) head
      deleteSheet c = removeChild (Link.toNode c) head
      toDelete = Map.filterKeys (not <<< flip Set.member $ Set.fromFoldable sheets) previous
  if replace then traverse_ deleteSheet (Map.values toDelete) else pure unit
  sequence_ $ mapWithIndex createLink newSheets
  if sheetCount == 0 then (cb $ Right unit) else pure unit
  pure nonCanceler

legacy :: {page :: LegacyURI} -> ReactElement
legacy = unsafeCreateLeafElement $ withStyles styles $ component "LegacyPage" $ \this -> do
  tempRef <- new Nothing
  let 
    d = eval >>> affAction this
    stateinp (Tuple name value) = D.input [_type "hidden", DP.name name, DP.value value ]
    render {state:s@{content}, props:{classes}} = case content of 
        Just (c@{html,title,script}) -> 
          let extraClass = case c.fullscreenMode of 
                "YES" -> []
                "YES_WITH_TOOLBAR" -> []
                _ -> case c.menuMode of
                  "HIDDEN" -> [] 
                  _ -> [classes.withPadding]
              hiddenState = D.div [DP.style {display: "none"}, DP.className "_hiddenstate"] $ (stateinp <$> toTuples s.state)
              mainContent = D.form [DP.name "eqForm", DP._id "eqpageForm"] $ [
                hiddenState,
                D.div [DP.className $ joinWith " " $ ["content"] <> extraClass] $ catMaybes [ 
                  (divWithHtml <<< {divProps:[_id "breadcrumbs"], script:Nothing, html: _} <$> lookup "crumbs" html),
                  (divWithHtml <<< {divProps:[], script:Nothing, html: _} <$> lookup "upperbody" html),
                  (divWithHtml <<< {divProps:[], script:Just script, html: _} <$> lookup "body" html)
                ]
          ] 
          in template' (templateDefaults title) {
                                menuExtra = toNullable $ options <$> lookup "so" html, 
                                innerRef = toNullable $ Just $ saveRef tempRef
                          } [ mainContent ] 
        Nothing ->  template "Loading" []
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


    submitWithPath path {vals,callback} = do 
        let cb = toMaybe callback
            ajax = isJust cb
        {response} <- lift $ post (Resp.json) (baseUrl <> "api/content/submit/" <> path)
                        (Req.json $ encodeJson vals)
        cb # maybe (either log updateContent $ decodeJson response) 
                  (liftEffect <<< flip runEffectFn1 response)

    eval = case _ of 
      (OptionsAnchor el) -> modifyState _ {optionsAnchor = el}
      Updated oldPage -> do 
        {page} <- getProps
        if oldPage /= page then eval LoadPage else pure unit
      LoadPage -> do 
        {page: LegacyURI pagePath params} <- getProps 
        modifyState _ {pagePath = pagePath}
        submitWithPath pagePath {vals: params, callback: toNullable Nothing}
      Submit s -> do 
        {pagePath} <- getState
        submitWithPath pagePath s
      UpdateForm {state,partial} -> do 
        modifyState \s -> s {state = if partial then Object.union s.state state else state}

    updateIncludes replace css js = do 
      updateStylesheets replace css
      loadMissingScripts $ js

    doRefresh true = liftEffect $ withRef tempRef refreshUser
    doRefresh _ = pure unit

    updateContent (Redirect state redir userUpdated) = do 
      doRefresh userUpdated
      liftEffect $ pushRoute $ LegacyPage $ LegacyURI redir state
    updateContent (LegacyContent lc@{css, js, state, html,script, title, fullscreenMode, menuMode} userUpdated) = do 
      doRefresh userUpdated
      lift $ updateIncludes true css js
      modifyState \s -> s {content = Just {html, script, title, fullscreenMode, menuMode}, state = state}

  setupLegacyHooks {
      submit: mkEffectFn1 $ d <<< Submit, 
      updateIncludes: mkEffectFn2 
        \{css,js,script} cb -> runAff_ (\_ -> cb) $ do 
          updateIncludes false css js
          liftEffect $ globalEval script, 
      updateForm: mkEffectFn1 $ d <<< UpdateForm
    }
  pure {
    state:{optionsAnchor:Nothing, content: Nothing, pagePath: "", stylesheets: [], state: Object.empty} :: State, 
    render: renderer render this, 
    componentDidMount: d $ LoadPage,
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
    }
  }

instance decodeLC :: DecodeJson ContentResponse where 
  decodeJson v = do 
    o <- decodeJson v 
    state <- o .? "state"
    redirect <- o .?? "redirect"
    userUpdated <- o .? "userUpdated"
    redirect # (flip maybe (\u -> pure $ Redirect state u userUpdated) $ do
      html <- o .? "html"
      css <- o .? "css"
      js <- o .? "js"
      script <- o .? "script"
      title <- o .? "title"
      fullscreenMode <- o .? "fullscreenMode"
      menuMode <- o .? "menuMode"
      pure $ LegacyContent {html, state, css, js, script, title, fullscreenMode, menuMode} userUpdated)
