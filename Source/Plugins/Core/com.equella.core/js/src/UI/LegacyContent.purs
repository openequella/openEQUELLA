module OEQ.UI.LegacyContent where 

import Prelude

import Control.Monad.Maybe.Trans (MaybeT(..), lift, runMaybeT)
import Data.Array (catMaybes, filter, length, mapWithIndex)
import Data.Either (Either(..))
import Data.Map as Map
import Data.Maybe (Maybe(..), fromJust)
import Data.Nullable (Nullable, toNullable)
import Data.Set (Set)
import Data.Set as Set
import Data.String (joinWith)
import Data.Traversable (sequence_, traverse, traverse_)
import Data.Tuple (Tuple(..))
import Dispatcher (affAction)
import Dispatcher.React (getProps, getState, modifyState, propsRenderer, renderer, saveRef, withRef)
import Effect (Effect)
import Effect.Aff (Aff, makeAff, nonCanceler, runAff_)
import Effect.Class (liftEffect)
import Effect.Ref as Ref
import Effect.Uncurried (EffectFn1, EffectFn2, mkEffectFn1, mkEffectFn2, runEffectFn1)
import Foreign.Object (Object, lookup)
import Foreign.Object as Object
import MaterialUI.Styles (withStyles)
import OEQ.API.LegacyContent (submitRequest)
import OEQ.Data.Error (ErrorResponse)
import OEQ.Data.LegacyContent (ContentResponse(..), LegacyURI(..), SubmitOptions)
import OEQ.Environment (baseUrl)
import OEQ.UI.Common (scrollWindowToTop)
import OEQ.Utils.QueryString (toTuples)
import OEQ.Utils.UUID (newUUID)
import Partial.Unsafe (unsafePartial)
import React (ReactElement, ReactRef, component, unsafeCreateLeafElement)
import React as R
import React.DOM (div')
import React.DOM as D
import React.DOM.Props (Props, _id, _type, onSubmit)
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

foreign import setInnerHtml :: {node :: ReactRef, html:: String, script::Nullable String, afterHtml :: Nullable (Effect Unit) } -> Effect Unit
foreign import clearInnerHtml :: ReactRef -> Effect Unit
foreign import globalEval :: String -> Effect Unit

type FormUpdate = {state :: Object (Array String), partial :: Boolean}

foreign import setupLegacyHooks_ :: {
    submit:: EffectFn1 SubmitOptions Unit, 
    updateIncludes :: EffectFn2 {css :: Array String, js :: Array String, script::String} (Effect Unit) Unit,
    updateForm :: EffectFn1 {state :: Object (Array String), partial :: Boolean} Unit
  } -> Effect Unit 

divWithHtml :: {divProps :: Array Props, html :: String, script :: Maybe String, afterHtml :: Maybe (Effect Unit), contentId::String} -> ReactElement
divWithHtml = unsafeCreateLeafElement $ component "JQueryDiv" $ \this -> do
  domNode <- Ref.new Nothing
  let
    render {divProps,html} = D.div (divProps <> [ DP.ref $ runEffectFn1 $ saveRef domNode ]) []
    updateHtml = do 
      {html, script, afterHtml} <- R.getProps this
      withRef domNode $ \node -> setInnerHtml {node,html,script: toNullable script, afterHtml: toNullable afterHtml}
  pure {
    render: propsRenderer render this,
    componentDidMount: updateHtml,
    componentWillUnmount: withRef domNode clearInnerHtml,
    componentDidUpdate: \_ _  _ -> updateHtml,
    shouldComponentUpdate: \{contentId} _ -> do 
      {contentId:newcontentId} <- R.getProps this
      pure $ contentId /= newcontentId
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


updateStylesheets :: Boolean -> Array String -> Aff (Effect Unit)
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
      toDelete = Map.filterKeys (not <<< flip Set.member $ Set.fromFoldable sheets) previous
      deleteEff = if replace then traverse_ deleteSheet (Map.values toDelete) else pure unit
      sheetCount = length newSheets
      createLink ind href = do 
        l <- fromJust <<< Link.fromElement <$> createElement "link" doc
        Link.setRel "stylesheet" l
        Link.setHref href l
        if sheetCount == ind + 1
          then do 
            el <- eventListener (\_ -> cb $ Right deleteEff)
            addEventListener (EventType "load") el false (Link.toEventTarget l)
          else pure unit
        insertBefore (Link.toNode l) (Elem.toNode insertPoint) head
      deleteSheet c = removeChild (Link.toNode c) head
  sequence_ $ mapWithIndex createLink newSheets
  if sheetCount == 0 then (cb $ Right deleteEff) else pure unit
  pure nonCanceler

updateIncludes :: Boolean -> Array String -> Array String -> Aff (Effect Unit)
updateIncludes replace css js = do 
    deleter <- updateStylesheets replace css
    loadMissingScripts $ js
    pure deleter

setupLegacyHooks :: (SubmitOptions -> Effect Unit) -> (FormUpdate -> Effect Unit) -> Effect Unit
setupLegacyHooks submit formUpdate = setupLegacyHooks_ { 
    submit: mkEffectFn1 $ submit, 
    updateIncludes: mkEffectFn2
          \{css,js,script} cb -> runAff_ (\_ -> cb) $ do 
            _ <- updateIncludes false css js
            liftEffect $ globalEval script, 
    updateForm: mkEffectFn1 $ formUpdate
}

writeForm :: Object (Array String) -> ReactElement -> ReactElement
writeForm state content = D.form [DP.name "eqForm", DP._id "eqpageForm", onSubmit preventDefault] [hiddenState, content]
  where 
    stateinp (Tuple name value) = D.input [_type "hidden", DP.name name, DP.value value ]
    hiddenState = D.div [DP.style {display: "none"}, DP.className "_hiddenstate"] $ (stateinp <$> toTuples state)

type LegacyContentProps = {
    page :: LegacyURI
  , contentUpdated :: PageContent -> Effect Unit
  , userUpdated :: Effect Unit
  , redirected :: {href :: String, external :: Boolean} -> Effect Unit
}

type PageContent = {
  html:: Object String,
  script :: String,
  title :: String, 
  contentId :: String,
  fullscreenMode :: String, 
  menuMode :: String,
  hideAppBar :: Boolean, 
  preventUnload :: Boolean,
  afterHtml :: Effect Unit
}

data Command = 
    Submit SubmitOptions
  | LoadPage 
  | Updated LegacyURI
  | UpdateForm FormUpdate

type State = {
  content :: Maybe PageContent,
  errored :: Maybe ErrorResponse,
  state :: Object (Array String),
  pagePath :: String,
  noForm :: Boolean
}
legacyContent :: LegacyContentProps -> ReactElement
legacyContent = unsafeCreateLeafElement $ withStyles styles $ component "LegacyContent" $ \this -> do
  let 
    d = eval >>> affAction this

    render {state:s@{content,errored}, props:{classes}} = case content of 
        Nothing -> div' []
        Just (c@{contentId, html,title,script, afterHtml}) -> 
          let extraClass = case c.fullscreenMode of 
                "YES" -> []
                "YES_WITH_TOOLBAR" -> []
                _ -> case c.menuMode of
                  "HIDDEN" -> [] 
                  _ -> [classes.withPadding]
              jqueryDiv f h = divWithHtml $ f {
                contentId, 
                divProps:[], 
                script:Nothing, 
                afterHtml: Nothing, 
                html:h }
              jqueryDiv_ = jqueryDiv identity

              actualContent = D.div [DP.className $ joinWith " " $ ["content"] <> extraClass] $ catMaybes [ 
                  (jqueryDiv (_ {divProps = [_id "breadcrumbs"]}) <$> lookup "crumbs" html),
                  jqueryDiv_  <$> lookup "upperbody" html,
                  (jqueryDiv _ {script = Just script, afterHtml = Just afterHtml}) <$> lookup "body" html ]
              mainContent = if s.noForm 
                then actualContent
                else writeForm s.state actualContent
          in mainContent

    submitWithPath fullError path opts = do 
        (lift $ submitRequest path opts) >>= case _ of 
          Left errorPage -> modifyState \s -> s {errored = Just errorPage, 
                        content = if fullError then Nothing else s.content}
          Right resp -> updateContent resp

    eval = case _ of 
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
        {pagePath} <- getState
        submitWithPath false pagePath s
      UpdateForm {state,partial} -> do 
        modifyState \s -> s {state = if partial then Object.union s.state state else state}

    doRefresh = if _ then do 
        {userUpdated} <- getProps 
        liftEffect $ userUpdated
      else pure unit

    doRedir href external = do 
        {redirected} <- getProps 
        liftEffect $ redirected {href,external}

    updateContent = case _ of 
      Callback cb -> liftEffect cb
      Redirect href -> do 
        doRedir href true
      ChangeRoute redir userUpdated -> do 
        doRefresh userUpdated
        doRedir redir false
      LegacyContent lc@{css, js, state, html,script, title, 
                      fullscreenMode, menuMode, hideAppBar, preventUnload} userUpdated -> do 
        doRefresh userUpdated
        deleteSheets <- lift $ updateIncludes true css js
        contentId <- liftEffect newUUID
        modifyState \s -> s {noForm = lc.noForm,
          content = Just {contentId,  html, script, title, fullscreenMode, menuMode, 
            hideAppBar, preventUnload, afterHtml: deleteSheets}, state = state}

  pure {
    state:{ 
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
    componentWillUnmount: runAff_ (const $ pure unit) $ do 
      deleteSheets <- updateStylesheets true []
      liftEffect deleteSheets

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