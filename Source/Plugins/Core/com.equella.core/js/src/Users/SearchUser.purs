module Users.SearchUser where 

import Prelude

import Common.CommonStrings (commonString)
import Common.Icons (groupIcon, roleIcon, userIcon)
import Control.Monad.Eff.Class (liftEff)
import Control.Monad.IOEffFn (IOFn1(..), runIOFn1)
import Control.Monad.IOSync (IOSync(..), runIOSync)
import Control.Monad.State (modify)
import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import Control.Plus (empty)
import Data.Array (length)
import Data.Monoid (mempty)
import Data.Newtype (class Newtype)
import Dispatcher (DispatchEff(..), dispatch)
import Dispatcher.React (ReactProps(..), createComponent, createLifecycleComponent, getProps, getState, modifyState)
import MaterialUI.Button (button, button_)
import MaterialUI.Color as C
import MaterialUI.DialogActions (dialogActions_)
import MaterialUI.Icon (icon_)
import MaterialUI.IconButton (iconButton)
import MaterialUI.List (list, subheader)
import MaterialUI.ListItem (listItem)
import MaterialUI.ListItemIcon (listItemIcon_)
import MaterialUI.ListItemSecondaryAction (listItemSecondaryAction, listItemSecondaryAction_)
import MaterialUI.ListItemText (listItemText, primary, secondary)
import MaterialUI.ListSubheader (listSubheader, listSubheader_)
import MaterialUI.Properties (color, onClick)
import MaterialUI.TextField (textField, value)
import React (ReactElement, createFactory)
import React.DOM (div', text)
import Users.UserLookup (class ToUGRDetail, GroupDetails(..), RoleDetails(..), UGRDetail, UserDetails(..), UserGroupRoles(..), searchUGR, toUGR)
import Utils.UI (textChange)

newtype UGREnabled = UGREnabled {users :: Boolean, groups :: Boolean, roles :: Boolean }
derive instance eqUGREn :: Eq UGREnabled
derive instance ntUGREn :: Newtype UGREnabled _

data Command = Search | MaybeSearch UGREnabled | UpdateQuery String | Select UGRDetail

type SearchState = {q::String, results :: UGRDetail, haveQueried :: Boolean}

userSearch :: forall eff. {onSelect :: IOFn1 UGRDetail Unit, onCancel :: IOSync Unit, enabled:: UGREnabled } -> ReactElement
userSearch = createFactory $ createLifecycleComponent lc initial render eval 
  where 
  lc = modify _{componentDidUpdate = \this {enabled} s -> dispatch eval this $ MaybeSearch enabled }

  initial :: SearchState
  initial = {q:"", results: mempty, haveQueried:false}
  render {q, results: UserGroupRoles {users,groups,roles}} (ReactProps p) (DispatchEff d) = div' [ 
      textField [value q, textChange d UpdateQuery ],
      iconButton [onClick $ d \_ -> Search] [icon_ [ text "search"] ],
      div' $ doUsers <> doGroups <> doRoles
  ]
    where 
    doSection :: forall a. Array a -> String -> (a -> ReactElement) -> Array ReactElement
    doSection l subh f = guard (length l > 0) *> [list [subheader $ listSubheader_ [text subh]] $ f <$> l]
    doUsers = doSection users commonString.users userEntry
    doGroups = doSection groups commonString.groups groupEntry
    doRoles = doSection roles commonString.roles roleEntry

    selectAction ugr = listItemSecondaryAction_ [ 
        iconButton [ color C.primary, onClick $ d \_ -> Select ugr ] [ icon_ [ text "add" ] ]
    ]
    userEntry u@(UserDetails ud) = listItem [] [ 
        listItemIcon_ [ userIcon ],
        listItemText [primary ud.username, secondary $ ud.firstName <> " " <> ud.lastName],
        selectAction $ toUGR u
    ]
    groupEntry g@(GroupDetails {name}) = listItem [] [ 
        listItemIcon_ [ groupIcon ],
        listItemText [primary name],
        selectAction $ toUGR g
    ]
    roleEntry r@(RoleDetails {name}) = listItem [] [ 
        listItemIcon_ [ roleIcon ],
        listItemText [primary name ],
        selectAction $ toUGR r
    ]

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
