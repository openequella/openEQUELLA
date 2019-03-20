module IntegTester where

import Prelude hiding (div)

import Control.Monad.Reader (runReaderT)
import Control.MonadZero (guard, (<|>))
import Data.Array (catMaybes, length, mapMaybe)
import Data.DateTime.Instant (Instant, unInstant)
import Data.Lens (lens, set, view)
import Data.Lens.Types (Lens')
import Data.Maybe (Maybe(..), fromMaybe)
import Data.Newtype (unwrap)
import Data.Number.Format (fixed, toStringWith)
import Data.Tuple (Tuple(..))
import Dispatcher.React (modifyState, stateRenderer)
import Effect (Effect)
import Effect.Now (now)
import Effect.Unsafe (unsafePerformEffect)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (a, div, div', form, input, label', option, select, text, textarea)
import React.DOM.Props (Props, _type, action, checked, className, href, method, name, onChange, onClick, value)
import Text.Parsing.Parser.String (oneOf)
import URI.Common (printEncoded, unreserved)
import URI.Extra.QueryPairs (QueryPairs(..), Value, keyFromString, unsafeValueFromString)
import URI.Extra.QueryPairs as QueryPairs
import URI.Query as Query
import Unsafe.Coerce (unsafeCoerce)
import Web.HTML (window)
import Web.HTML.Location (host, pathname)
import Web.HTML.Window (location)

foreign import md5AndBase64 :: String -> String

methods :: Array String
methods = [ "lms", "vista" ]
actions :: Array String
actions = ["contribute", "searchResources", "selectOrAdd", "searchThin", "structured"]

type State = {
    method :: String
  , action :: String
  , url :: String
  , options :: String
  , username :: String
  , sharedSecret :: String
  , sharedSecretId :: String
  , courseId :: String
  , itemonly :: Boolean
  , packageonly :: Boolean
  , attachmentonly :: Boolean
  , selectMultiple :: Boolean
  , useDownloadPrivilege :: Boolean
  , forcePost :: Boolean
  , cancelDisabled :: Boolean
  , attachmentUuidUrls :: Boolean
  , makeReturn :: Boolean
  , itemXml :: String
  , powerXml :: String
  , clickUrl :: Maybe String
}

type FormContext = {change :: (State -> State) -> Effect Unit, state::State}

changeStr :: ((State -> State) -> Effect Unit) -> Lens' State String -> Props
changeStr d l = onChange \e -> d $ set l (unsafeCoerce  e).target.value

changeChecked :: ((State -> State) -> Effect Unit) -> Lens' State Boolean -> Props
changeChecked d l = onChange \e -> d $ set l (unsafeCoerce  e).target.checked

selectList :: String -> Array String -> Lens' State String -> FormContext -> ReactElement
selectList n os l {change,state} =
  select [ name n, className "formcontrol", value (view l state), changeStr change l] (mkOption <$> os)
  where
    mkOption o = option [ value o ] [ text o ]

textBox :: String -> Lens' State String -> FormContext -> ReactElement
textBox n l {state,change} = input [ name n, className "formcontrol",
     _type "text", value (view l state), changeStr change l ]

textArea :: String -> Lens' State String -> FormContext -> ReactElement
textArea n l {state,change} = textarea [ value (view l state), name n, className "itemXml", changeStr change l ] []

checkBox :: String -> Lens' State Boolean -> FormContext -> ReactElement
checkBox n l {state,change} = input [ name n, _type "checkbox",
    checked (view l state), changeChecked change l]

controls :: Effect (Array
  { label :: String
  , control :: FormContext -> ReactElement
  })
controls = do

 l <- window >>= location
 h <- host l 
 pn <- pathname l
 let returnUrl = "http://" <> h <> pn <> "?method=showReturn"
 pure [
  {label:"Method:", control:selectList "method" methods (lens _.method _{method = _})}
, {label:"Action:", control:selectList "action" actions (lens _.action _{action = _})}
, {label:"Options:", control:textBox "options" (lens _.options _{options = _})}
, {label:"URL:", control:textBox "url" (lens _.url _{url = _})}
, {label:"Username:", control:textBox "username" (lens _.username _{username = _})}
, {label:"Shared Secret:", control:textBox "sharedSecret" (lens _.sharedSecret _{sharedSecret = _})}
, {label:"Shared Secret ID:", control:textBox "sharedSecretId" (lens _.sharedSecretId _{sharedSecretId = _})}
, {label:"Course ID:", control:textBox "courseId" (lens _.courseId _{courseId = _})}
, {label: "Select Items only:", control:checkBox "itemonly" (lens _.itemonly _{itemonly = _})}
, {label: "Select Packages only:", control:checkBox "packageonly" (lens _.packageonly _{packageonly = _})}
, {label: "Select Attachments only:", control:checkBox "attachmentonly" (lens _.attachmentonly _{attachmentonly = _})}
, {label: "Select multiple:", control:checkBox "selectMultiple" (lens _.selectMultiple _{selectMultiple = _})}
, {label: "Use download privilege:", control:checkBox "useDownloadPrivilege" (lens _.useDownloadPrivilege _{useDownloadPrivilege = _})}
, {label: "Force POST return:", control:checkBox "forcePost" (lens _.forcePost _{forcePost = _})}
, {label: "Disabling cancelling:", control:checkBox "cancelDisabled" (lens _.cancelDisabled _{cancelDisabled = _})}
, {label: "Generate ?attachment.uuid=abcd URLs:", control:checkBox "attachmentUuidUrls" (lens _.attachmentUuidUrls _{attachmentUuidUrls = _})}
, {label: "Generate Return URL:", control:checkBox "makeReturn" (lens _.makeReturn _{makeReturn = _})}
, {label: "Initial item XML:", control:textArea "itemXml" (lens _.itemXml _{itemXml = _})}
, {label: "Initial powersearch XML:", control:textArea "powerXml" (lens _.powerXml _{powerXml = _})}
, {label: "", control: (\{change:d} -> input [ _type "submit", onClick \_ -> d $ \s -> s{clickUrl=Just $ createUrl returnUrl s}  ]) }
]

-- 		<div class="formrow">
-- 			<label> Structure XML: </label>
-- 			<n:textarea property="structure" styleClass="itemXml" />
-- 			<n:notEmpty property="structure">
-- 				<n:define id="sx" property="structure" />
-- 			</n:notEmpty>
-- 		</div>

initialState :: State
initialState = {method:"lms",action:"searchResources", options:"", url:"", username:""
  , sharedSecret:"", sharedSecretId:"", courseId:""
  , itemonly: false, packageonly: false, attachmentonly: false
  , selectMultiple: false, useDownloadPrivilege: false, forcePost: false
  , cancelDisabled: false, attachmentUuidUrls: false, makeReturn: false
  , clickUrl : Nothing, itemXml : "", powerXml : ""
}

data Actions = Update (State -> State)


createToken :: {username::String, id::String, sharedSecret :: String, data :: Maybe String, curTime :: Instant} -> String
createToken s = let timeStr = toStringWith (fixed 0) (unwrap $ unInstant s.curTime)
    in s.username
    <> ":" <> s.id
    <> ":" <> timeStr
    <> ":" <> (md5AndBase64 $ s.username <> s.id <> timeStr <> s.sharedSecret)
    <> (fromMaybe "" $ (append ":") <$> s.data)

strToValue :: String -> Value
strToValue = unsafeValueFromString <<< printEncoded (unreserved
  <|> oneOf ['!', '$', '\'', '(', ')', '*', '=', ',', ':', '@', '/', '?'])

createUrl :: String -> State -> String
createUrl returnurl s@{username, sharedSecret, url} = url <> (Query.print $ QueryPairs.print keyFromString strToValue 
  (QueryPairs $
    [
      p "token" $ createToken {
                            username,
                            id: s.sharedSecretId,
                            sharedSecret,
                            data: Nothing,
                            curTime: unsafePerformEffect now }
    , p "method" s.method
    , p "action" s.action
    , p "returnprefix" ""
    , p "returnurl" returnurl 
    ] <> mapMaybe boolParam [
        Tuple "selectMultiple" s.selectMultiple
      , Tuple "itemonly" s.itemonly
      , Tuple "attachmentonly" s.attachmentonly
      , Tuple "packageonly" s.packageonly
      , Tuple "useDownloadPrivilege" s.useDownloadPrivilege
      , Tuple "forcePost" s.forcePost
      , Tuple "cancelDisabled" s.cancelDisabled
      , Tuple "attachmentUuidUrls" s.attachmentUuidUrls
    ] <> mapMaybe strParam [
        Tuple "options" s.options
      , Tuple "courseId" s.courseId
    ]))
  where
    boolParam (Tuple n b) = (Tuple n (Just "true")) <$ guard b
    strParam (Tuple n str) = (Tuple n (Just str)) <$ guard (str /= "")
    p n v = Tuple n (Just v)

integ :: Array (Tuple String String) -> ReactElement
integ postVals = flip unsafeCreateLeafElement {} $ component "IntegTester" $ \this -> do
  allControls <- controls
  let
    d :: Actions -> Effect Unit
    d = eval >>> flip runReaderT this
    eval (Update f) = modifyState f
    render s@{clickUrl} = div' $ catMaybes [
                                    Just (div' $ writeControl <$> allControls)
                                    , haveUrl <$> clickUrl
                                    , (div' (writeParam <$> postVals)) <$ guard (length postVals > 0)
                                ]
  		where
        haveUrl url = form [ method "POST", action url ] $ hiddenVals <> [
              a [ href url ] [ text url ]
            , div' [ input [ _type "submit", value "POST to this URL"] ]
            ]
        hiddenInput (Tuple n v) = input [ _type "hidden", name n, value v]
        hiddenVals = hiddenInput <$> [Tuple "itemXml" s.itemXml, Tuple "powerXml" s.powerXml]
        writeControl {label, control} = div [ className "formrow" ] [
          label' [ text label ]
        , control {state:s, change: d <<< Update}
        ]
        writeParam (Tuple n v) = writeControl {label:n <> ":", control: \_ -> textarea [ className "itemXml" ] [ text v ]}
  pure {render: stateRenderer render this, state:initialState}
-- MainForm form = (MainForm) formData;
-- 		String secretId = form.getSharedSecretId();
-- 		if( Check.isEmpty(secretId) )
-- 			secretId = null;
-- 		List<NameValue> nvs = new ArrayList<NameValue>();
-- 		String token = TokenGenerator.createSecureToken(form.getUsername(), secretId, form.getSharedSecret(), null);
-- 		String meths = form.getIntegrationMethod();
-- 		nvs.add(new NameValue("method", meths));
-- 		if( meths != null && meths.equals("vista") )
-- 		{
-- 			nvs.add(new NameValue("proxyToolCallbackGUID", "111"));
-- 			nvs.add(new NameValue("addtool", "a%26componentType%3Dx"));
-- 		}
-- 		nvs.add(new NameValue("template", form.getTemplate()));
-- 		if( form.getCourseId().length() > 0 )
-- 		{
-- 			nvs.add(new NameValue("courseId", form.getCourseId()));
-- 		}
-- 		if( form.getOptions().length() > 0 )
-- 		{
-- 			nvs.add(new NameValue("options", form.getOptions()));
-- 		}
-- 		nvs.add(new NameValue("returnprefix", ""));
-- 		if( form.isMakeReturn() )
-- 		{
-- 			nvs.add(new NameValue("returnurl", request.getRequestURL().toString() + '?'
-- 				+ getParameterString(Arrays.asList(new NameValue("method", "showReturn")))));
-- 		}
