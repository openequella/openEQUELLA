module OEQ.UI.LegacyContent where 

import Prelude

import Control.Monad.Maybe.Trans (lift)
import Data.Array (catMaybes, filter)
import Data.Either (Either(..))
import Data.Maybe (Maybe(..))
import Data.Nullable (Nullable, toNullable)
import Data.Set (Set)
import Data.Set as Set
import Data.String (joinWith)
import Data.Tuple (Tuple(..))
import Dispatcher (affAction)
import Dispatcher.React (getProps, getState, modifyState, renderer)
import Effect (Effect)
import Effect.Aff (Aff, runAff_)
import Effect.Aff.Compat (EffectFnAff, fromEffectFnAff)
import Effect.Class (liftEffect)
import Effect.Uncurried (EffectFn1, EffectFn2, mkEffectFn1, mkEffectFn2, runEffectFn1)
import Foreign.Object (Object, lookup)
import Foreign.Object as Object
import MaterialUI.Styles (withStyles)
import OEQ.API.LegacyContent (submitRequest)
import OEQ.Data.Error (ErrorResponse)
import OEQ.Data.LegacyContent (ContentResponse(..), LegacyURI(..), SubmitOptions)
import OEQ.UI.Common (scrollWindowToTop)
import OEQ.Utils.Interop (nullAny)
import OEQ.Utils.QueryString (toTuples)
import OEQ.Utils.UUID (newUUID)
import React (ReactClass, ReactElement, ReactRef, component, unsafeCreateLeafElement)
import React.DOM (div')
import React.DOM as D
import React.DOM.Props (_type, onSubmit)
import React.DOM.Props as DP
import React.SyntheticEvent (preventDefault)
import Record.Unsafe.Union (unsafeUnion)
import TSComponents (JQueryDivProps, jqueryDivClass)
import Unsafe.Reference (unsafeRefEq)

foreign import setInnerHtml :: {node :: ReactRef, html:: String, script::Nullable String, afterHtml :: Nullable (Effect Unit) } -> Effect Unit
foreign import clearInnerHtml :: ReactRef -> Effect Unit
foreign import globalEval :: String -> Effect Unit

type FormUpdate = {state :: Object (Array String), partial :: Boolean}

foreign import setupLegacyHooks_ :: {
    submit:: EffectFn1 SubmitOptions Unit, 
    updateIncludes :: EffectFn2 {css :: Array String, js :: Array String, script::String} (Effect Unit) Unit,
    updateForm :: EffectFn1 {state :: Object (Array String), partial :: Boolean} Unit
  } -> Effect Unit 

foreign import resolveUrl :: String -> String 

filterUrls :: Set String -> Array String -> Array String 
filterUrls existing = filter (not <<< flip Set.member existing)

foreign import loadMissingScripts_ :: Array String -> EffectFnAff Unit

loadMissingScripts :: Array String -> Aff Unit
loadMissingScripts scripts = fromEffectFnAff $ loadMissingScripts_ scripts

foreign import updateStylesheets_ :: Boolean -> Array String -> EffectFnAff (Effect Unit)

updateStylesheets :: Boolean -> Array String -> Aff (Effect Unit)
updateStylesheets replace sheets = fromEffectFnAff $ updateStylesheets_ replace sheets

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
  , contentUpdated :: EffectFn1 PageContent  Unit
  , userUpdated :: Effect Unit
  , redirected :: EffectFn1 {href :: String, external :: Boolean} Unit
  , onError :: EffectFn1 {error::ErrorResponse, fullScreen :: Boolean} Unit
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
  state :: Object (Array String),
  pagePath :: String,
  noForm :: Boolean
}

divWithHtml :: forall r. JQueryDivProps r -> ReactElement
divWithHtml = unsafeCreateLeafElement jqueryDivClass

emptyContent :: PageContent 
emptyContent = {html:Object.empty, script:"", title:"", contentId: "0", fullscreenMode: "NO", menuMode:"NO", hideAppBar: false, preventUnload:false, afterHtml: pure unit}

legacyContent :: LegacyContentProps -> ReactElement
legacyContent = unsafeCreateLeafElement legacyContentClass

legacyContentClass :: ReactClass LegacyContentProps 
legacyContentClass = withStyles styles $ component "LegacyContent" $ \this -> do
  let 
    d = eval >>> affAction this

    render {state:s@{content}, props:{classes}} = case content of 
        Nothing -> div' []
        Just (c@{contentId, html,title,script, afterHtml}) -> 
          let extraClass = case c.fullscreenMode of 
                "YES" -> []
                "YES_WITH_TOOLBAR" -> []
                _ -> case c.menuMode of
                  "HIDDEN" -> [] 
                  _ -> [classes.withPadding]
              
              jqueryDiv :: forall r. (JQueryDivProps () -> JQueryDivProps r) -> String -> ReactElement
              jqueryDiv f h = divWithHtml $ f {
                script: nullAny, 
                afterHtml: nullAny,
                html:h }
              jqueryDiv_ = jqueryDiv identity

              actualContent = D.div [DP.className $ joinWith " " $ ["content"] <> extraClass] $ catMaybes [ 
                  (jqueryDiv (unsafeUnion {id: "breadcrumbs" }) <$> lookup "crumbs" html),
                  jqueryDiv_  <$> lookup "upperbody" html,
                  (jqueryDiv _ {script = toNullable $ Just script, 
                    afterHtml = toNullable $ Just afterHtml}) <$> lookup "body" html ]
              mainContent = if s.noForm 
                then actualContent
                else writeForm s.state actualContent
          in mainContent

    submitWithPath fullError path opts = do 
        (lift $ submitRequest path opts) >>= case _ of 
          Left error -> do 
            {onError} <- getProps
            liftEffect $ runEffectFn1 onError {error, fullScreen:fullError }
          Right resp -> updateContent resp

    eval = case _ of 
      Updated oldPage -> do 
        {page} <- getProps
        if not $ unsafeRefEq oldPage page then eval LoadPage else pure unit
      LoadPage -> do 
        {page: LegacyURI pagePath params} <- getProps 
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
        liftEffect $ runEffectFn1 redirected {href,external}

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
        let newContent = {contentId,  html, script, title, fullscreenMode, menuMode, 
                            hideAppBar, preventUnload, afterHtml: deleteSheets}
        {contentUpdated} <- getProps 
        liftEffect $ runEffectFn1 contentUpdated newContent                    
        modifyState \s -> s {noForm = lc.noForm,
          content = Just newContent, state = state}

  pure {
    state:{ 
      content: Nothing, 
      pagePath: "", 
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
