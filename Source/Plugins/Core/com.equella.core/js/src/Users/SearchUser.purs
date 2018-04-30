module Users.SearchUser where 

import Prelude

import Control.Monad.Eff.Class (liftEff)
import Control.Monad.IOEffFn (IOFn1(..), runIOFn1)
import Control.Monad.IOSync (IOSync(..), runIOSync)
import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import Control.Plus (empty)
import Data.Array (length)
import Data.Monoid (mempty)
import Dispatcher (DispatchEff(..))
import Dispatcher.React (ReactProps(..), createComponent, getProps, getState, modifyState)
import MaterialUI.Button (button, button_)
import MaterialUI.ButtonBase (onClick)
import MaterialUI.DialogActions (dialogActions_)
import MaterialUI.Icon (icon_)
import MaterialUI.IconButton (iconButton)
import MaterialUI.List (list, subheader)
import MaterialUI.ListItem (listItem)
import MaterialUI.ListItemIcon (listItemIcon_)
import MaterialUI.ListItemSecondaryAction (listItemSecondaryAction, listItemSecondaryAction_)
import MaterialUI.ListItemText (listItemText, primary, secondary)
import MaterialUI.ListSubheader (listSubheader, listSubheader_)
import MaterialUI.PropTypes (handle)
import MaterialUI.Properties (color)
import MaterialUI.Color as C
import MaterialUI.TextField (onChange, textField, value)
import React (ReactElement, createFactory)
import React.DOM (div', text)
import Users.UserLookup (class ToUGRDetail, GroupDetails(..), RoleDetails(..), UGRDetail, UserDetails(..), UserGroupRoles(..), searchUGR, toUGR)
import Utils.UI (textChange)

data Command = Search | UpdateQuery String | Select UGRDetail

type SearchState = {q::String, results :: UGRDetail}

userSearch :: forall eff. {onSelect :: IOFn1 UGRDetail Unit, onCancel :: IOSync Unit } -> ReactElement
userSearch = createFactory $ createComponent initial render eval 
  where 
  initial :: SearchState
  initial = {q:"", results: mempty}
  render {q, results: UserGroupRoles {users,groups,roles}} (ReactProps p) (DispatchEff d) = div' [ 
      textField [value q, textChange d UpdateQuery ],
      iconButton [onClick $ handle $ d \_ -> Search] [icon_ [ text "search"] ],
      div' $ doUsers <> doGroups <> doRoles
  ]
    where 
    doSection :: forall a. Array a -> String -> (a -> ReactElement) -> Array ReactElement
    doSection l subh f = guard (length l > 0) *> [list [subheader $ listSubheader_ [text subh]] $ f <$> l]
    doUsers = doSection users "Users" userEntry
    doGroups = doSection groups "Groups" groupEntry
    doRoles = doSection roles "Roles" roleEntry

    selectAction ugr = listItemSecondaryAction_ [ 
        iconButton [ color C.primary, onClick $ handle $ d \_ -> Select ugr ] [ icon_ [ text "add" ] ]
    ]
    userEntry u@(UserDetails ud) = listItem [] [ 
        listItemIcon_ [ icon_ [text "person"]],
        listItemText [primary ud.username, secondary $ ud.firstName <> " " <> ud.lastName],
        selectAction $ toUGR u
    ]
    groupEntry g@(GroupDetails {name}) = listItem [] [ 
        listItemIcon_ [ icon_ [text "people"]],
        listItemText [primary name],
        selectAction $ toUGR g
    ]
    roleEntry r@(RoleDetails {name}) = listItem [] [ 
        listItemIcon_ [ icon_ [text "people"]],
        listItemText [primary name ],
        selectAction $ toUGR r
    ]

  eval (UpdateQuery q) = modifyState _{q=q}
  eval (Select ugr) = getProps >>= \{onSelect} -> liftEff $ runIOFn1 onSelect ugr
  eval Search = do
    {q} <- getState
    r <- lift $ searchUGR q
    modifyState _ {results=r}
