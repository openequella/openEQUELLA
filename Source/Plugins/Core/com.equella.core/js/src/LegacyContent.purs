module LegacyContent where 

import Prelude

import AjaxRequests (ErrorResponse, decodeResponse)
import Control.Alt ((<|>))
import Control.Monad.Maybe.Trans (MaybeT(..), lift, runMaybeT)
import Data.Argonaut (class DecodeJson, Json, decodeJson, encodeJson, (.?), (.??))
import Data.Array (filter, length, mapWithIndex)
import Data.Bifunctor (lmap)
import Data.Either (Either(..))
import Data.Map as Map
import Data.Maybe (Maybe(..), fromJust, maybe, maybe')
import Data.Nullable (Nullable, toMaybe, toNullable)
import Data.Set (Set)
import Data.Set as Set
import Data.Traversable (sequence_, traverse, traverse_)
import Data.Tuple (Tuple(..))
import Dispatcher.React (propsRenderer, saveRef, withRef)
import EQUELLA.Environment (baseUrl)
import Effect (Effect)
import Effect.Aff (Aff, makeAff, nonCanceler, runAff_)
import Effect.Class (liftEffect)
import Effect.Ref as Ref
import Effect.Uncurried (EffectFn1, EffectFn2, mkEffectFn1, mkEffectFn2, runEffectFn1)
import Foreign.Object (Object)
import Network.HTTP.Affjax as Ajax
import Network.HTTP.Affjax.Request as Req
import Network.HTTP.Affjax.Response as Resp
import Network.HTTP.StatusCode (StatusCode(..))
import Partial.Unsafe (unsafePartial)
import QueryString (toTuples)
import React (ReactElement, ReactRef, component, unsafeCreateLeafElement)
import React as R
import React.DOM as D
import React.DOM.Props (Props, _type, onSubmit)
import React.DOM.Props as DP
import React.SyntheticEvent (preventDefault)
import Web.DOM.Document (createElement, getElementsByTagName, toNonElementParentNode)
import Web.DOM.Element as Elem
import Web.DOM.HTMLCollection (item, toArray)
import Web.DOM.Node (appendChild, insertBefore, removeChild)
import Web.DOM.NonDocumentTypeChildNode (previousElementSibling)
import Web.DOM.NonElementParentNode (getElementById)
import Web.Event.Event (EventType(..))
import Web.Event.EventTarget (addEventListener, eventListener)
import Web.HTML (window)
import Web.HTML.HTMLDocument (toDocument)
import Web.HTML.HTMLLinkElement as Link
import Web.HTML.HTMLScriptElement as Script
import Web.HTML.Window (document)

foreign import setInnerHtml :: {node :: ReactRef, html:: String, script::Nullable String } -> Effect Unit
foreign import clearInnerHtml :: ReactRef -> Effect Unit
foreign import globalEval :: String -> Effect Unit

type SubmitOptions = {vals::Object (Array String), callback :: Nullable (EffectFn1 Json Unit)} 

type FormUpdate = {state :: Object (Array String), partial :: Boolean}

foreign import setupLegacyHooks_ :: {
    submit:: EffectFn1 SubmitOptions Unit, 
    updateIncludes :: EffectFn2 {css :: Array String, js :: Array String, script::String} (Effect Unit) Unit,
    updateForm :: EffectFn1 {state :: Object (Array String), partial :: Boolean} Unit
  } -> Effect Unit

data ContentResponse = Redirect String | ChangeRoute String Boolean | LegacyContent LegacyContentR Boolean | Callback (Effect Unit)

type LegacyContentR = {
  html:: Object String, 
  state :: Object (Array String),
  css :: Array String,
  js :: Array String,
  script :: String, 
  title :: String, 
  fullscreenMode :: String, 
  menuMode :: String, 
  hideAppBar :: Boolean, 
  noForm :: Boolean
} 

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
  sequence_ $ mapWithIndex createLink newSheets
  if replace then traverse_ deleteSheet (Map.values toDelete) else pure unit
  if sheetCount == 0 then (cb $ Right unit) else pure unit
  pure nonCanceler

updateIncludes :: Boolean -> Array String -> Array String -> Aff Unit
updateIncludes replace css js = do 
    updateStylesheets replace css
    loadMissingScripts $ js

setupLegacyHooks :: (SubmitOptions -> Effect Unit) -> (FormUpdate -> Effect Unit) -> Effect Unit
setupLegacyHooks submit formUpdate = setupLegacyHooks_ { 
    submit: mkEffectFn1 $ submit, 
    updateIncludes: mkEffectFn2
          \{css,js,script} cb -> runAff_ (\_ -> cb) $ do 
            updateIncludes false css js
            liftEffect $ globalEval script, 
    updateForm: mkEffectFn1 $ formUpdate
}

writeForm :: Object (Array String) -> ReactElement -> ReactElement
writeForm state content = D.form [DP.name "eqForm", DP._id "eqpageForm", onSubmit preventDefault] [hiddenState, content]
  where 
    stateinp (Tuple name value) = D.input [_type "hidden", DP.name name, DP.value value ]
    hiddenState = D.div [DP.style {display: "none"}, DP.className "_hiddenstate"] $ (stateinp <$> toTuples state)

submitRequest :: String -> SubmitOptions -> Aff (Either ErrorResponse ContentResponse)
submitRequest path {vals,callback} = do 
    resp <- Ajax.post (Resp.json) (baseUrl <> "api/content/submit/" <> path)
                        (Req.json $ encodeJson vals) 
    let runcb cb json = pure $ Callback $ runEffectFn1 cb json
    pure $ decodeResponse (maybe decodeJson runcb $ toMaybe callback) resp 

instance decodeLC :: DecodeJson ContentResponse where 
  decodeJson v = do 
    o <- decodeJson v 
    let decodeChange = do 
          route <- o .? "route"
          userUpdated <- o .? "userUpdated"
          pure $ ChangeRoute route userUpdated
        decodeRedirect = do 
          href <- o .? "href"
          pure $ Redirect href
        decodeContent = do 
          html <- o .? "html"
          state <- o .? "state"
          css <- o .? "css"
          js <- o .? "js"
          script <- o .? "script"
          title <- o .? "title"
          fullscreenMode <- o .? "fullscreenMode"
          menuMode <- o .? "menuMode"
          hideAppBar <- o .? "hideAppBar"
          userUpdated <- o .? "userUpdated"
          noForm <- o .? "noForm"
          pure $ LegacyContent {html, state, css, js, script, title, fullscreenMode, menuMode, hideAppBar, noForm} userUpdated
    decodeChange <|> decodeRedirect <|> decodeContent
