module Security.TermSelection where 

import Prelude hiding (div)

import Common.CommonStrings (commonAction, commonString)
import Control.Monad.Eff (Eff)
import Control.Monad.IOEffFn (IOFn1, mkIOFn1, runIOFn1)
import Control.Monad.IOSync (IOSync, runIOSync)
import Control.Monad.State (modify)
import Control.Monad.Trans.Class (lift)
import Data.Array (catMaybes)
import Data.Int (fromString)
import Data.Lens (Lens', Prism', over, prism', set)
import Data.Lens.Iso.Newtype (_Newtype)
import Data.Lens.Record (prop)
import Data.Maybe (Maybe(Just, Nothing), fromMaybe)
import Data.Symbol (SProxy(..))
import Data.Tuple (Tuple(..))
import Dispatcher (DispatchEff(..), effEval)
import Dispatcher.React (ReactProps(ReactProps), ReactState(ReactState), createLifecycleComponent', getProps, getState, modifyState)
import EQUELLA.Environment (prepLangStrings)
import MaterialUI.Button (button, disabled)
import MaterialUI.Checkbox (checkbox)
import MaterialUI.Color as C
import MaterialUI.Dialog (dialog)
import MaterialUI.DialogActions (dialogActions_)
import MaterialUI.DialogContent (dialogContent) 
import MaterialUI.DialogTitle (dialogTitle_)
import MaterialUI.Event (Event)
import MaterialUI.FormControlLabel (control, formControlLabel, label)
import MaterialUI.FormGroup (formGroup, row)
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Modal (open)
import MaterialUI.PropTypes (EventHandler)
import MaterialUI.Properties (IProp, className, color, mkProp, onChange, onClick, onClose)
import MaterialUI.Select (select)
import MaterialUI.Styles (withStyles)
import MaterialUI.SwitchBase (checked)
import MaterialUI.TextField (textField, value)
import React (ReactElement, createFactory, writeState)
import React.DOM (div, span, text)
import React.DOM.Props as P
import Security.Expressions (ExpressionTerm(..), IpRange(..), _ip1, _ip2, _ip3, _ip4, _ipm, validMasks, validRange)
import Security.Resolved (ResolvedTerm(..))
import Users.SearchUser (UGREnabled(..), userSearch)
import Users.UserLookup (UserGroupRoles(..))

data DialogType = UserDialog UGREnabled | IpDialog IpRange | ReferrerDialog String | SecretDialog String

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
  _users :: forall r a. Lens' {users::a|r} a
  _users = prop (SProxy :: SProxy "users")
  _groups :: forall r a. Lens' {groups::a|r} a
  _groups = prop (SProxy :: SProxy "groups")
  _roles :: forall r a. Lens' {roles::a|r} a
  _roles = prop (SProxy :: SProxy "roles")

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
  _dialogUGR :: Prism' DialogType UGREnabled
  _dialogUGR = prism' UserDialog $ case _ of 
    UserDialog r -> Just r
    _ -> Nothing
  _UGREnabled :: Lens' UGREnabled {users::Boolean, groups::Boolean, roles::Boolean}
  _UGREnabled = _Newtype

  initialState (ReactProps {dt}) = ReactState dt

  termStrings = prepLangStrings termRawStrings
  titles = termStrings.title

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
          UserDialog _ -> classes.dialog
          _ -> classes.smallDialog
    in dialog [open o, onClose $ const $ runIOSync $ cancel] [ 
      dialogTitle_ [text title],
      dialogContent [className dialogStyle] content,
      dialogActions_ $ catMaybes [
        (\e -> button [color C.primary, onClick $ command Add, disabled $ not e ] [text commonAction.add]) <$> add,
        Just $ button [color C.secondary, onClick $ \_ -> runIOSync cancel] [ text commonAction.cancel ]
      ]
    ]
    where
    command :: forall a e. TermCommand -> a -> Eff e Unit
    command c = d \_ -> c
    onChangeStr :: forall r. (String -> TermCommand) -> IProp (onChange::EventHandler Event|r)
    onChangeStr f = onChange $ d $ \e -> f e.target.value
    stdText v l = textField [value v, onChangeStr $ Change <<< set l]
    ugrCheck lab l b = formControlLabel [control $ checkbox [checked b, onChange $ 
                  command $ Change (over (_dialogUGR <<< _Newtype <<< l) not) ], label lab]
    dialogContents = case _ of 
      UserDialog enabled@UGREnabled {users,groups,roles} -> {title: titles.ugr, add: Nothing, content: [
          formGroup [row true] [ 
            ugrCheck commonString.users _users users,
            ugrCheck commonString.groups _groups groups,
            ugrCheck commonString.roles _roles roles
          ],
          userSearch {onSelect: mkIOFn1 $ termsForUsers >>> runIOFn1 onAdd, onCancel: cancel, enabled}
        ]}
      IpDialog r@(IpRange i1 i2 i3 i4 im) ->
          let ipField v l = textField [className classes.ipField, value $ if v == -1 then "" else show v, onChangeStr $ 
                            \t -> Change $ (set $ _ipRange <<< l) $ fromMaybe (-1) $ fromString t]
              ipSepText t = span [P.className classes.ipSep] [ text t ]
              dot = ipSepText "."
              slash = ipSepText "/"
          in {
            title: titles.ip, 
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
        title: titles.referrer, 
        content:[ stdText referrer _dialogReferrer], 
        add:Just true
      }
      SecretDialog secret -> {
        title: titles.token, 
        content: [ stdText secret _dialogSecret], 
        add:Just true
      }
    termsForUsers (UserGroupRoles {users,groups,roles}) = 
      (ResolvedUser <$> users) <> (ResolvedGroup <$> groups) <> (ResolvedRole <$> roles)

termRawStrings = Tuple "aclterms" {
  title: {
    ugr: "Select User / Group / Role",
    ip: "Select IP range",
    referrer: "HTTP referrer",
    token: "Select shared secret"
  }
}