module Security.TermSelection where 

import Prelude hiding (div)

import Control.Monad.IOEffFn (IOFn1, mkIOFn1, runIOFn1)
import Control.Monad.IOSync (IOSync, runIOSync)
import Control.Monad.State (modify)
import Control.Monad.Trans.Class (lift)
import Data.Array (catMaybes)
import Data.Int (fromString)
import Data.Lens (Prism', prism', set)
import Data.Maybe (Maybe(..), fromMaybe, maybe)
import Debug.Trace (traceAnyA)
import Dispatcher (DispatchEff(..), effEval)
import Dispatcher.React (ReactProps(..), ReactState(..), createComponent', createLifecycleComponent', didMount, getProps, getState, modifyState)
import MaterialUI.Button (button, disabled)
import MaterialUI.ButtonBase (onClick)
import MaterialUI.Dialog (dialog)
import MaterialUI.DialogActions (dialogActions_)
import MaterialUI.DialogContent (dialogContent)
import MaterialUI.DialogTitle (dialogTitle_)
import MaterialUI.ExpansionPanel (onChange)
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Modal (onClose, open)
import MaterialUI.PropTypes (Untyped, handle)
import MaterialUI.Properties (IProp, className, mkProp)
import MaterialUI.Select (select)
import MaterialUI.Styles (withStyles)
import MaterialUI.TextField (textField, value)
import React (ReactElement, createFactory, writeState)
import React.DOM (div, span, text)
import React.DOM.Props as P
import Security.Expressions (ExpressionTerm(..), IpRange(..), _ip1, _ip2, _ip3, _ip4, _ipm, validMasks, validRange)
import Security.Resolved (ResolvedTerm(..))
import Users.SearchUser (userSearch)
import Users.UserLookup (UserGroupRoles(..))

data DialogType = UserDialog | IpDialog IpRange | ReferrerDialog String | SecretDialog String

data TermCommand = Add | Change (DialogType -> DialogType)

termDialog :: {open::Boolean, onAdd :: IOFn1 (Array ResolvedTerm) Unit, cancel :: IOSync Unit,  dt :: DialogType} -> ReactElement
termDialog = createFactory (withStyles styles $ createLifecycleComponent' reRenderOnProps initialState render $ effEval eval)
  where 
  reRenderOnProps =  modify _ {componentWillReceiveProps = \this p -> void $ writeState this p.dt}
  styles theme = {
    dialog: {
      height: 600,
      width: 600
    }, 
    smallDialog: {
      width: "20em",
      height: "10em"
    },
    ipField: {
      width: "3em",
      margin: theme.spacing.unit
    }, 
    ipMask: {
      width: "4em",
      margin: theme.spacing.unit
    },
    rangeContainer: {
      display: "flex", 
      alignItems: "baseline"
    }, 
    ipSep: {

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

  render dt (ReactProps {classes,onAdd,cancel,open:o}) (DispatchEff d) = 
    let {title,content,add} = dialogContents dt 
        dialogStyle = case dt of 
          UserDialog -> classes.dialog
          _ -> classes.smallDialog
    in dialog [open o, onClose $ handle $ const $ runIOSync $ cancel] [ 
      dialogTitle_ [text title],
      dialogContent [className dialogStyle] content,
      dialogActions_ $ catMaybes [
        (\e -> button [ onClick $ command Add, disabled $ not e ] [text "Add"]) <$> add,
        Just $ button [ onClick $ handle $ \_ -> runIOSync cancel] [ text "Cancel" ]
      ]
    ]
    where
    command c = handle $ d \_ -> c
    onChangeStr :: forall r. (String -> TermCommand) -> IProp (onChange::Untyped|r)
    onChangeStr f = onChange $ handle $ d $ \e -> f e.target.value
    stdText v l = textField [value v, onChangeStr $ Change <<< set l]
    dialogContents = case _ of 
      UserDialog -> {title: "Select User / Group / Roles", add: Nothing, content: [
            userSearch {onSelect: mkIOFn1 $ termsForUsers >>> runIOFn1 onAdd, onCancel: cancel}
          ]}
      IpDialog r@(IpRange i1 i2 i3 i4 im) ->
          let ipField v l = textField [className classes.ipField, value $ if v == -1 then "" else show v, onChangeStr $ 
                            \t -> Change $ (set $ _ipRange <<< l) $ fromMaybe (-1) $ fromString t]
              ipSepText t = span [P.className classes.ipSep] [ text t ]
              dot = ipSepText "."
              slash = ipSepText "/"
          in {
            title: "Select IP range", 
            add: Just $ validRange r, 
            content: [ div [P.className classes.rangeContainer ] [ 
              ipField i1 _ip1, dot,
              ipField i2 _ip2, dot,
              ipField i3 _ip3, dot,
              ipField i4 _ip4, slash,
              select [value im, className classes.ipMask,
                onChangeStr $ \t -> Change $ set (_ipRange <<< _ipm) $ fromMaybe 8 $ fromString t
                ] $ 
                (\m -> menuItem [mkProp "value" m] [text $ show m]) <$> validMasks
          ]]}
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
