module Security.TermSelection where 

import Prelude

import Control.Monad.IOEffFn (IOFn1, mkIOFn1, runIOFn1)
import Control.Monad.IOSync (IOSync, runIOSync)
import Control.Monad.Trans.Class (lift)
import Data.Array (catMaybes)
import Data.Int (fromString)
import Data.Lens (Prism', prism', set)
import Data.Maybe (Maybe(..), maybe)
import Dispatcher (DispatchEff(..), effEval)
import Dispatcher.React (ReactProps(..), ReactState(..), createComponent', getProps, getState, modifyState)
import MaterialUI.Button (button, disabled)
import MaterialUI.ButtonBase (onClick)
import MaterialUI.DialogActions (dialogActions_)
import MaterialUI.DialogContent (dialogContent)
import MaterialUI.DialogTitle (dialogTitle_)
import MaterialUI.ExpansionPanel (onChange)
import MaterialUI.PropTypes (handle)
import MaterialUI.Properties (className)
import MaterialUI.Styles (withStyles)
import MaterialUI.TextField (textField, value)
import React (ReactElement, createFactory)
import React.DOM (div', text)
import Security.Expressions (ExpressionTerm(..), IpRange(..), _ip1, _ip2, _ip3, _ip4, _ipm)
import Security.Resolved (ResolvedTerm(..))
import Users.SearchUser (userSearch)
import Users.UserLookup (UserGroupRoles(..))

data DialogType = UserDialog | IpDialog IpRange | ReferrerDialog String | SecretDialog String

data TermCommand = Add | Change (DialogType -> DialogType)

termDialog :: {onAdd :: IOFn1 (Array ResolvedTerm) Unit, cancel :: IOSync Unit,  dt :: DialogType} -> ReactElement
termDialog = createFactory (withStyles styles $ createComponent' initialState render $ effEval eval)
  where 
  styles theme = {
    dialog: {
      height: 600,
      width: 600
    }
  }
  _ipRange :: Prism' DialogType IpRange
  _ipRange = prism' IpDialog $ case _ of 
    IpDialog r -> Just r
    _ -> Nothing
  _dialogReferrer :: Prism' DialogType String
  _dialogReferrer = prism' ReferrerDialog $ case _ of 
    ReferrerDialog r -> Just r
    _ -> Nothing
  _dialogSecret :: Prism' DialogType String
  _dialogSecret = prism' SecretDialog $ case _ of 
    SecretDialog r -> Just r
    _ -> Nothing

  initialState (ReactProps {dt}) = ReactState dt

  eval = case _ of 
    Add -> do 
      {onAdd} <- getProps
      s <- getState
      lift $ runIOFn1 onAdd $ pure $ Already case s of 
        IpDialog range -> Ip range
        ReferrerDialog referrer -> Referrer referrer
        SecretDialog secret -> SharedSecretToken secret
        _ -> Everyone
    Change f -> modifyState f
  render dt (ReactProps {classes,onAdd,cancel}) (DispatchEff d) = 
    let {title,content,add} = dialogContents dt 
    in div' [ 
      dialogTitle_ [text title],
      dialogContent [className classes.dialog] content,
      dialogActions_ $ catMaybes [
        (\e -> button [ onClick $ command Add, disabled $ not e ] [text "Add"]) <$> add,
        Just $ button [ onClick $ handle $ \_ -> runIOSync cancel] [ text "Cancel" ]
      ]
    ]
    where
    command c = handle $ d \_ -> c
    onChangeStr f = onChange $ handle $ d $ \e -> f e.target.value
    stdText v l = textField [value v, onChangeStr $ Change <<< set l]
    dialogContents = case _ of 
      UserDialog -> {title: "Select User / Group / Roles", add: Nothing, content: [
            userSearch {onSelect: mkIOFn1 $ termsForUsers >>> runIOFn1 onAdd, onCancel: cancel}
          ]}
      IpDialog (IpRange i1 i2 i3 i4 im) -> let 
          ipField v l = textField [value $ show v, onChangeStr $ 
            \t -> Change $ fromString t # maybe id (set $ _ipRange <<< l)]
          in {
            title: "Select IP range", 
            add: Just true, 
            content:[ 
              ipField i1 _ip1, 
              ipField i2 _ip2, 
              ipField i3 _ip3, 
              ipField i4 _ip4, 
              ipField im _ipm
          ]}
      ReferrerDialog referrer -> {
        title: "HTTP referrer", 
        content:[ stdText referrer _dialogReferrer], 
        add:Just true
      }
      SecretDialog secret -> {
        title: "Select shared secret", 
        content: [ stdText secret _dialogSecret], 
        add:Just true
      }
    termsForUsers (UserGroupRoles {users,groups,roles}) = 
      (ResolvedUser <$> users) <> (ResolvedGroup <$> groups) <> (ResolvedRole <$> roles)
