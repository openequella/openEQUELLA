module Main where

import React.DOM (div, div', input, label', option, select, text)
import Control.Monad.Eff (Eff)
import DOM (DOM)
import Data.Functor.Contravariant (cmap)
import Data.Lens (lens, view, set)
import Data.Lens.Types (Lens')
import Dispatcher (DispatchEff(..), effEval)
import Dispatcher.React (createComponent, modifyState, renderWithSelector)
import React (ReactElement, createFactory)
import React.DOM.Props (Props, _type, checked, className, onChange, value)
import Unsafe.Coerce (unsafeCoerce)
import Prelude hiding (div)


methods :: Array String
methods = [ "lms", "vista" ]
actions :: Array String
actions = ["contribute", "searchResources", "selectOrAdd", "searchThin",
			"structured"]

type State = {
    method :: String
  , action :: String
  , url :: String
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
}

type FormContext = {change :: DispatchEff (State -> State), state::State}

changeStr :: DispatchEff (State -> State) -> Lens' State String -> Props
changeStr (DispatchEff d) l = onChange $ d \e -> set l (unsafeCoerce  e).target.value

changeChecked :: DispatchEff (State -> State) -> Lens' State Boolean -> Props
changeChecked (DispatchEff d) l = onChange $ d \e -> set l (unsafeCoerce  e).target.checked

selectList :: Array String -> Lens' State String -> FormContext -> ReactElement
selectList os l {change,state} =
  select [ className "formcontrol", value (view l state), changeStr change l] $ mkOption <$> os
  where
    mkOption o = option [ value o ] [ text o ]

textBox :: Lens' State String -> FormContext -> ReactElement
textBox l {state,change} = input [ className "formcontrol", _type "text", value (view l state), changeStr change l ] []

checkBox :: Lens' State Boolean -> FormContext -> ReactElement
checkBox l {state,change} = input [ _type "checkbox", checked (view l state), changeChecked change l] []

controls :: Array
  { label :: String
  , control :: FormContext -> ReactElement
  }
controls = [
  {label:"Method:", control:selectList methods (lens _.method _{method = _})}
, {label:"Action:", control:selectList actions (lens _.action _{action = _})}
, {label:"URL:", control:textBox (lens _.url _{url = _})}
, {label:"Username:", control:textBox (lens _.username _{username = _})}
, {label:"Shared Secret:", control:textBox (lens _.sharedSecret _{sharedSecret = _})}
, {label:"Shared Secret ID:", control:textBox (lens _.sharedSecretId _{sharedSecretId = _})}
, {label:"Course ID:", control:textBox (lens _.courseId _{courseId = _})}
, {label: "Select Items only:", control:checkBox (lens _.itemonly _{itemonly = _})}
, {label: "Select Packages only:", control:checkBox (lens _.packageonly _{packageonly = _})}
, {label: "Select Attachments only:", control:checkBox (lens _.attachmentonly _{attachmentonly = _})}
, {label: "Select multiple:", control:checkBox (lens _.selectMultiple _{selectMultiple = _})}
, {label: "Use download privilege:", control:checkBox (lens _.useDownloadPrivilege _{useDownloadPrivilege = _})}
, {label: "Force POST return:", control:checkBox (lens _.forcePost _{forcePost = _})}
, {label: "Disabling cancelling:", control:checkBox (lens _.cancelDisabled _{cancelDisabled = _})}
, {label: "Generate ?attachment.uuid=abcd URLs:", control:checkBox (lens _.attachmentUuidUrls _{attachmentUuidUrls = _})}
, {label: "Generate Return URL:", control:checkBox (lens _.makeReturn _{makeReturn = _})}
]

initialState :: State
initialState = {method:"lms",action:"searchResources",url:"", username:""
  , sharedSecret:"", sharedSecretId:"", courseId:""
  , itemonly: false, packageonly: false, attachmentonly: false
  , selectMultiple: false, useDownloadPrivilege: false, forcePost: false
  , cancelDisabled: false, attachmentUuidUrls: false, makeReturn: false
}

data Actions = Update (State -> State)

integ :: ReactElement
integ = createFactory (createComponent initialState render (effEval eval)) {}
  where
  eval (Update f) = modifyState f
  render s d = div' $ writeControl <$> controls
    where
    writeControl {label,control} =  div [ className "formrow" ] [
      label' [ text label ]
    , control {state:s, change: cmap Update d}
    ]

main :: forall e. Eff (dom::DOM|e) Unit
main = void $ renderWithSelector "#app" integ
