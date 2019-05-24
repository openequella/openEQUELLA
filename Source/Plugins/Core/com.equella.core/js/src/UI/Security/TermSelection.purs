module OEQ.UI.Security.TermSelection where 

import Prelude hiding (div)

import Common.CommonStrings (commonAction, commonString)
import Common.Strings (languageStrings)
import Control.Monad.Trans.Class (lift)
import Data.Array (catMaybes)
import Data.Int (fromString)
import Data.Lens (Lens', Prism', over, prism', set)
import Data.Lens.Iso.Newtype (_Newtype)
import Data.Lens.Record (prop)
import Data.Maybe (Maybe(Just, Nothing), fromMaybe, maybe)
import Data.String (length)
import Data.Symbol (SProxy(..))
import Dispatcher (affAction)
import Dispatcher.React (getProps, getState, modifyState, renderer)
import Effect (Effect)
import Effect.Class (liftEffect)
import Effect.Uncurried (EffectFn1, mkEffectFn1, runEffectFn1)
import MaterialUI.Button (button)
import MaterialUI.Checkbox (checkbox')
import MaterialUI.Dialog (dialog)
import MaterialUI.DialogActions (dialogActions_)
import MaterialUI.DialogContent (dialogContent)
import MaterialUI.DialogTitle (dialogTitle_)
import MaterialUI.Enums (primary, secondary)
import MaterialUI.FormControlLabel (formControlLabel')
import MaterialUI.FormGroup (formGroup)
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Select (select)
import MaterialUI.Styles (withStyles)
import MaterialUI.TextField (textField')
import OEQ.API.User (listTokens)
import OEQ.Data.Security (ExpressionTerm(..), IpRange(..), ResolvedTerm(..), _ip1, _ip2, _ip3, _ip4, _ipm, validMasks, validRange)
import OEQ.Data.User (UserGroupRoles(..))
import OEQ.UI.Common (textChange)
import OEQ.UI.SearchUser (UGREnabled(..), userSearch)
import React (ReactElement, component, unsafeCreateLeafElement)
import React (getProps) as R
import React.DOM (div, span, text)
import React.DOM.Props as P

data DialogType = UserDialog UGREnabled | IpDialog IpRange | ReferrerDialog String | SecretDialog String

data TermCommand = Add | Change (DialogType -> DialogType) | Init

termDialog :: {open::Boolean, onAdd :: EffectFn1 (Array ResolvedTerm) Unit, cancel :: Effect Unit,  dt :: DialogType} -> ReactElement
termDialog = unsafeCreateLeafElement $ withStyles styles $ component "TermDialog" $ \this -> do
  let 
    d = eval >>> affAction this
    unsafeComponentWillReceiveProps {dt} = d $ Change $ const dt
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

    termStrings = languageStrings.aclterms
    titles = termStrings.title

    eval = case _ of 
      Init -> do 
          t <- lift $ listTokens
          modifyState _{tokens = Just t}
      Add -> do 
        {onAdd} <- getProps
        {dt} <- getState
        liftEffect $ runEffectFn1 onAdd $ pure $ Already case dt of 
          IpDialog range -> Ip range
          ReferrerDialog referrer -> Referrer referrer
          SecretDialog secret -> SharedSecretToken secret
          _ -> Everyone
      Change f -> modifyState \s -> s {dt = f s.dt}

    render {state:s, props:{classes,onAdd,cancel,open:o}} = 
      let {title,content,add} = dialogContents
          dialogStyle = case s.dt of 
            UserDialog _ -> classes.dialog
            _ -> classes.smallDialog
      in dialog {open: o, onClose: cancel} [ 
        dialogTitle_ [text title],
        dialogContent {className: dialogStyle} content,
        dialogActions_ $ catMaybes [
          (\e -> button {color: primary, onClick: d Add, disabled: not e} [text commonAction.add]) <$> add,
          Just $ button {color: secondary, onClick: cancel} [ text commonAction.cancel ]
        ]
      ]
      where
      stdText v l = textField' {value: v, onChange: textChange d $ Change <<< set l}
      ugrCheck lab l b = formControlLabel' {control: checkbox' {checked: b, onChange: 
                    d $ Change (over (_dialogUGR <<< _Newtype <<< l) not) }, label: lab}
      dialogContents = case s.dt of 
        UserDialog enabled@UGREnabled {users,groups,roles} -> {title: titles.ugr, add: Nothing, content: [
            formGroup {row: true} [ 
              ugrCheck commonString.users _users users,
              ugrCheck commonString.groups _groups groups,
              ugrCheck commonString.roles _roles roles
            ],
            userSearch {onSelect: mkEffectFn1 $ termsForUsers >>> runEffectFn1 onAdd, clickEntry:true, onCancel: cancel, enabled}
          ]}
        IpDialog r@(IpRange i1 i2 i3 i4 im) ->
            let ipField v l = textField' {className: classes.ipField, 
                            value: if v == -1 then "" else show v, 
                            onChange: textChange d $ \t -> Change $ (set $ _ipRange <<< l) $ fromMaybe (-1) $ fromString t}
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
                select {value: im, className: classes.ipMask,
                  onChange: textChange d $ \t -> Change $ set (_ipRange <<< _ipm) $ fromMaybe 8 $ fromString t
                 } $ 
                  (\m -> menuItem {value:m} [text $ show m]) <$> validMasks
            ]]}
        ReferrerDialog referrer -> {
          title: titles.referrer, 
          content:[ stdText referrer _dialogReferrer], 
          add:Just true
        }
        SecretDialog secret -> {
          title: titles.token, 
          content: [ select {value: secret, className: classes.secretField, onChange: textChange d $ Change <<< set _dialogSecret} $  
            maybe [] (map (\m -> menuItem {value:m} [text m])) s.tokens 
          ], 
          add:Just $ length secret > 0
        }
      termsForUsers (UserGroupRoles {users,groups,roles}) = 
        (ResolvedUser <$> users) <> (ResolvedGroup <$> groups) <> (ResolvedRole <$> roles)
  state <-R.getProps this <#> \{dt} -> {dt, tokens:Nothing}
  pure {state, render: renderer render this, componentDidMount: d Init, unsafeComponentWillReceiveProps}
  where 
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

    }, 
    secretField: {
      width: "10em",
      margin: theme.spacing.unit
    }
  }