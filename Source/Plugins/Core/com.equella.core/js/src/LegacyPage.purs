module LegacyPage where

import Prelude

import Control.Monad.Maybe.Trans (MaybeT(..), runMaybeT)
import Control.Monad.Reader (runReaderT)
import Control.Monad.Trans.Class (lift)
import Data.Argonaut (class DecodeJson, class EncodeJson, Json, decodeJson, encodeJson, (.?), (.??))
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
import Dispatcher.React (getProps, getState, modifyState, propsRenderer, renderer)
import EQUELLA.Environment (baseUrl)
import Effect (Effect)
import Effect.Aff (Aff, makeAff, nonCanceler, runAff_)
import Effect.Aff.Compat (EffectFn1, EffectFn2, mkEffectFn1, mkEffectFn2, runEffectFn1)
import Effect.Class (liftEffect)
import Effect.Class.Console (log)
import Foreign.Object (Object, lookup)
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
import React (ReactElement, ReactRef, component, unsafeCreateLeafElement)
import React as R
import React.DOM (text)
import React.DOM as D
import React.DOM.Props (Props, _id, _type)
import React.DOM.Props as DP
import Routes (LegacyURI(..), Route(..), pushRoute)
import Template (template, template', templateDefaults)
import Unsafe.Coerce (unsafeCoerce)
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

foreign import setInnerHtml :: {html:: String, script::Nullable String, node :: Nullable ReactRef } -> Effect Unit

type SubmitOptions = {vals::Array NameValue, callback :: Nullable (EffectFn1 Json Unit)} 

foreign import setupLegacyHooks :: {submit:: EffectFn1 SubmitOptions Unit, 
    updateIncludes :: EffectFn2 {css :: Array String, js :: Array String} (Effect Unit) Unit
  } -> Effect Unit

newtype NameValue = NameValue {name::String, value::String}

toNameValue :: Tuple String String -> NameValue
toNameValue (Tuple name value) = NameValue {name, value}

fromNameValue :: NameValue -> Tuple String String
fromNameValue (NameValue {name,value}) = Tuple name value

type LegacyContentR = {
  -- baseResources::String, 
  html:: Object String, 
  state :: Array NameValue,
  css :: Array String, 
  js :: Array String,
  script :: String, 
  title :: String, 
  fullscreenMode :: String, 
  menuMode :: String
  -- title::String, 
  -- menuItems :: Array (Array MenuItem), 
  -- menuMode :: String,
  -- fullscreenMode :: String,
  -- hideAppBar :: Boolean,
  -- newUI::Boolean, 
  -- user::UserData
} 

data ContentResponse = Redirect (Array NameValue) String | LegacyContent LegacyContentR

data RawHtml = DomNode (Nullable ReactRef)

divWithHtml :: {divProps :: Array Props, html :: String, script :: Maybe String} -> ReactElement
divWithHtml = unsafeCreateLeafElement $ component "JQueryDiv" $ \this -> do
  let
    d = eval >>> flip runReaderT this
    eval (DomNode r) = do
      {html, script} <- getProps
      lift $ setInnerHtml {html, script: toNullable script, node:r}
    render {divProps,html} = D.div (divProps <> [ DP.ref $ d <<< DomNode ]) []
  pure {
    render: propsRenderer render this, shouldComponentUpdate: \{html} _ -> do 
      {html:newhtml} <- R.getProps this
      pure $ html /= newhtml
  }

data Command = OptionsAnchor (Maybe HTMLElement) | Submit SubmitOptions | LoadPage | Updated LegacyURI

type State = {
  optionsAnchor::Maybe HTMLElement, 
  content :: Maybe LegacyContentR, 
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


updateStylesheets :: Boolean -> Array String -> Effect Unit
updateStylesheets replace _sheets = unsafePartial $ do 
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
  let createLink href = do 
        l <- fromJust <<< Link.fromElement <$> createElement "link" doc
        Link.setRel "stylesheet" l
        Link.setHref href l
        insertBefore (Link.toNode l) (Elem.toNode insertPoint) head
      deleteSheet c = removeChild (Link.toNode c) head
      toDelete = Map.filterKeys (not <<< flip Set.member $ Set.fromFoldable sheets) previous
  traverse_ createLink $ (filterUrls (Map.keys previous) sheets)
  if replace then traverse_ deleteSheet (Map.values toDelete) else pure unit

legacy :: {page :: LegacyURI} -> ReactElement
legacy = unsafeCreateLeafElement $ withStyles styles $ component "LegacyPage" $ \this -> do
  let 
    d = eval >>> affAction this
    stateinp (NameValue {name,value}) = D.input [_type "hidden", DP.name name, DP.value value ]
    render {state:s@{content}, props:{classes}} = case content of 
        Just (c@{html,state,title,script}) -> 
          let extraClass = case c.fullscreenMode of 
                "YES" -> []
                "YES_WITH_TOOLBAR" -> []
                _ -> case c.menuMode of
                  "HIDDEN" -> []
                  _ -> [classes.withPadding]
              mainContent = D.form [DP.name "eqForm", DP._id "eqpageForm"] $ (stateinp <$> state) <> [
                D.div [DP.className $ joinWith " " $ ["content"] <> extraClass] $ catMaybes [ 
                  (divWithHtml <<< {divProps:[_id "breadcrumbs"], script:Nothing, html: _} <$> lookup "crumbs" html),
                  (divWithHtml <<< {divProps:[], script:Nothing, html: _} <$> lookup "upperbody" html),
                  (divWithHtml <<< {divProps:[], script:Just script, html: _} <$> lookup "body" html)
                ]
          ] 
          in template' (templateDefaults title) {
                                menuExtra = toNullable $ options <$> lookup "so" html 
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
        submitWithPath pagePath {vals:toNameValue <$> params, callback: toNullable Nothing}
      Submit s -> do 
        {pagePath} <- getState
        submitWithPath pagePath s

    updateIncludes replace css js = do 
      liftEffect $ updateStylesheets replace css
      loadMissingScripts $ js

    updateContent (Redirect state redir) = do 
      liftEffect $ pushRoute $ LegacyPage $ LegacyURI redir $ fromNameValue <$> state
    updateContent (LegacyContent lc@{css, js, script}) = do 
      lift $ updateIncludes true css js
      modifyState \s -> s {content = Just lc}

  setupLegacyHooks {submit: mkEffectFn1 $ d <<< Submit, updateIncludes: mkEffectFn2 
      \{css,js} cb -> runAff_ (\_ -> cb) $ updateIncludes false css js
    }
  pure {
    state:{optionsAnchor:Nothing, content: Nothing, pagePath: "", stylesheets: []} :: State, 
    render: renderer render this, 
    componentDidMount: d $ LoadPage,
    componentDidUpdate: \{page} _ _ -> d $ Updated page,
    componentWillUnmount: updateStylesheets true []
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
instance decodeNV :: DecodeJson NameValue where 
 decodeJson v = do 
    o <- decodeJson v 
    name <- o .? "name"
    value <- o .? "value"
    pure $ NameValue {name, value}
 

instance encodeNV :: EncodeJson NameValue where 
  encodeJson (NameValue nv) = unsafeCoerce nv

instance decodeLC :: DecodeJson ContentResponse where 
  decodeJson v = do 
    o <- decodeJson v 
    state <- o .? "state"
    redirect <- o .?? "redirect"
    redirect # (flip maybe (pure <<< Redirect state) $ do
      html <- o .? "html"
      css <- o .? "css"
      js <- o .? "js"
      script <- o .? "script"
      title <- o .? "title"
      fullscreenMode <- o .? "fullscreenMode"
      menuMode <- o .? "menuMode"
      pure $ LegacyContent {html, state, css, js, script, title, fullscreenMode, menuMode})
