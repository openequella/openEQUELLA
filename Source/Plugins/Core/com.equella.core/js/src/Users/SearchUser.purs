module Users.SearchUser where 

import Prelude hiding (div)

import Common.CommonStrings (commonString)
import Common.Icons (groupIcon, roleIcon, searchIcon, userIcon)
import Control.Monad.Eff.Class (liftEff)
import Control.Monad.IOEffFn (IOFn1(..), runIOFn1)
import Control.Monad.IOSync (IOSync(..), runIOSync)
import Control.Monad.State (modify)
import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import Control.Plus (empty)
import Data.Array (catMaybes, length)
import Data.Monoid (mempty)
import Data.Newtype (class Newtype)
import Dispatcher (DispatchEff(..), dispatch)
import Dispatcher.React (ReactProps(..), createComponent, createLifecycleComponent, getProps, getState, modifyState)
import MaterialUI.Button (fullWidth)
import MaterialUI.Color as C
import MaterialUI.DialogActions (dialogActions_)
import MaterialUI.FormControlLabel (label)
import MaterialUI.Icon (icon_)
import MaterialUI.IconButton (iconButton)
import MaterialUI.List (list, subheader)
import MaterialUI.ListItem (button, listItem)
import MaterialUI.ListItemIcon (listItemIcon_)
import MaterialUI.ListItemSecondaryAction (listItemSecondaryAction, listItemSecondaryAction_)
import MaterialUI.ListItemText (listItemText, primary, secondary)
import MaterialUI.ListSubheader (listSubheader, listSubheader_)
import MaterialUI.Properties (color, mkProp, onClick)
import MaterialUI.Styles (withStyles)
import MaterialUI.TextField (inputProps, textField, value)
import React (ReactElement, createFactory)
import React.DOM (div, div', text)
import React.DOM.Props as P
import Users.UserLookup (class ToUGRDetail, GroupDetails(..), RoleDetails(..), UGRDetail, UserDetails(..), UserGroupRoles(..), searchUGR, toUGR)
import Utils.UI (enterSubmit, textChange)

newtype UGREnabled = UGREnabled {users :: Boolean, groups :: Boolean, roles :: Boolean }
derive instance eqUGREn :: Eq UGREnabled
derive instance ntUGREn :: Newtype UGREnabled _

data Command = Search | MaybeSearch UGREnabled | UpdateQuery String | Select UGRDetail

type SearchState = {q::String, results :: UGRDetail, haveQueried :: Boolean}

userSearch :: forall eff. {onSelect :: IOFn1 UGRDetail Unit, onCancel :: IOSync Unit, enabled:: UGREnabled, clickEntry :: Boolean } -> ReactElement
userSearch = createFactory $ withStyles styles (createLifecycleComponent lc initial render eval)
  where 
  lc = modify _{componentDidUpdate = \this {enabled} s -> dispatch eval this $ MaybeSearch enabled }
  styles theme = {
      queryLine: {
          display: "flex"
      }
  }

  initial :: SearchState
  initial = {q:"", results: mempty, haveQueried:false}
  render {q, results: UserGroupRoles {users,groups,roles}} (ReactProps p@{classes, clickEntry}) (DispatchEff d) = div' [ 
      div [P.className classes.queryLine] [
        textField [value q, label "Enter query", fullWidth true, textChange d UpdateQuery, 
            mkProp "onKeyDown" $ enterSubmit $ d (\_ -> Search) unit ],
        iconButton [onClick $ d \_ -> Search] [ searchIcon ]
      ],
      div' $ doUsers <> doGroups <> doRoles
  ]
    where 
    doSection :: forall a. Array a -> String -> (a -> ReactElement) -> Array ReactElement
    doSection l subh f = guard (length l > 0) *> [list (
        catMaybes [guard moreThanOneType $> (subheader $ listSubheader_ [text subh])]) $ f <$> l]
    doUsers = doSection users commonString.users userEntry
    doGroups = doSection groups commonString.groups groupEntry
    doRoles = doSection roles commonString.roles roleEntry

    moreThanOneType = case p.enabled of 
        UGREnabled {users,groups,roles} -> (bcount users + bcount groups + bcount roles) > 1
      where bcount true = 1 
            bcount false = 0
    selectAction ugr = listItemSecondaryAction_ [ 
        iconButton [ color C.primary, onClick $ d \_ -> Select ugr ] [ icon_ [ text "add_circle" ] ]
    ]

    entry i textProps ugr = listItem (guard clickEntry *> [button true, clicked ]) [ 
        listItemIcon_ [ i ],
        listItemText textProps
    ]
      where 
      clicked = onClick $ d \_ -> Select ugr

    userEntry u@(UserDetails ud) = entry userIcon  [primary ud.username, 
        secondary $ ud.firstName <> " " <> ud.lastName] (toUGR u)
    
    groupEntry g@(GroupDetails {name}) = entry groupIcon [primary name] $ toUGR g

    roleEntry r@(RoleDetails {name}) = entry roleIcon [primary name ] $ toUGR r

  eval (UpdateQuery q) = modifyState _{q=q}
  eval (Select ugr) = getProps >>= \{onSelect} -> liftEff $ runIOFn1 onSelect ugr
  eval (MaybeSearch ugren) = do 
    {enabled} <- getProps 
    {haveQueried} <- getState
    if haveQueried && enabled /= ugren then eval Search else pure unit
  eval Search = do
    {q} <- getState
    {enabled: UGREnabled {users,groups,roles}} <- getProps
    r <- lift $ searchUGR q {users,groups,roles}
    modifyState _ {results=r, haveQueried=true}
