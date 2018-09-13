module OEQ.UI.SearchUser where 

import Prelude hiding (div)

import Common.CommonStrings (commonString)
import OEQ.UI.Icons (groupIcon, roleIcon, searchIcon, userIcon)
import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import Data.Array (catMaybes, length)
import Data.Newtype (class Newtype)
import Dispatcher (affAction)
import Dispatcher.React (getProps, getState, modifyState, renderer)
import OEQ.API.User (searchUGR)
import OEQ.Data.User (GroupDetails(..), RoleDetails(..), UGRDetail, UserDetails(..), UserGroupRoles(..), toUGR)
import Effect (Effect)
import Effect.Class (liftEffect)
import Effect.Uncurried (EffectFn1, runEffectFn1)
import MaterialUI.Button (fullWidth)
import MaterialUI.FormControlLabel (label)
import MaterialUI.IconButton (iconButton)
import MaterialUI.List (list, subheader)
import MaterialUI.ListItem (button, listItem)
import MaterialUI.ListItemIcon (listItemIcon_)
import MaterialUI.ListItemText (listItemText, primary, secondary)
import MaterialUI.ListSubheader (listSubheader_)
import MaterialUI.Properties (mkProp, onClick)
import MaterialUI.Styles (withStyles)
import MaterialUI.TextField (textField, value)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (div, div', text)
import React.DOM.Props as P
import OEQ.UI.Common (enterSubmit, textChange)

newtype UGREnabled = UGREnabled {users :: Boolean, groups :: Boolean, roles :: Boolean }
derive instance eqUGREn :: Eq UGREnabled
derive instance ntUGREn :: Newtype UGREnabled _

data Command = Search | MaybeSearch UGREnabled | UpdateQuery String | Select UGRDetail

type SearchState = {q::String, results :: UGRDetail, haveQueried :: Boolean} 

userSearch :: {onSelect :: EffectFn1 UGRDetail Unit, onCancel :: Effect Unit, enabled:: UGREnabled, clickEntry :: Boolean } -> ReactElement
userSearch = unsafeCreateLeafElement $ withStyles styles $ component "UserSearch" $ \this -> do
  let 
    d = eval >>> affAction this
    
    componentDidUpdate {enabled} _ _ = d $ MaybeSearch enabled

    render {state: {q, results: UserGroupRoles res},props: p@{classes, clickEntry}} = div' [ 
        div [P.className classes.queryLine] [
            textField [value q, label "Enter query", fullWidth true, textChange d UpdateQuery, 
                mkProp "onKeyDown" $ enterSubmit $ d Search ],
            iconButton [onClick $ \_ -> d Search] [ searchIcon ]
        ],
        div' $ doUsers <> doGroups <> doRoles
    ]
      where 
        doSection :: forall a. Array a -> String -> (a -> ReactElement) -> Array ReactElement
        doSection l subh f = guard (length l > 0) *> [list (
            catMaybes [guard moreThanOneType $> (subheader $ listSubheader_ [text subh])]) $ f <$> l]
        doUsers = doSection res.users commonString.users userEntry
        doGroups = doSection res.groups commonString.groups groupEntry
        doRoles = doSection res.roles commonString.roles roleEntry

        moreThanOneType = case p.enabled of 
            UGREnabled {users,groups,roles} -> (bcount users + bcount groups + bcount roles) > 1
          where bcount true = 1 
                bcount false = 0

        -- selectAction ugr = listItemSecondaryAction_ [ 
        --     iconButton [ color C.primary, onClick $ \_ -> d $ Select ugr ] [ icon_ [ text "add_circle" ] ]
        -- ]

        entry i textProps ugr = listItem (guard clickEntry *> [button true, clicked ]) [ 
            listItemIcon_ [ i ],
            listItemText textProps
        ]
          where 
          clicked = onClick $ \_ -> d $ Select ugr

        userEntry u@(UserDetails ud) = entry userIcon  [primary ud.username, 
            secondary $ ud.firstName <> " " <> ud.lastName] (toUGR u)
        
        groupEntry g@(GroupDetails {name}) = entry groupIcon [primary name] $ toUGR g

        roleEntry r@(RoleDetails {name}) = entry roleIcon [primary name ] $ toUGR r

    eval (UpdateQuery q) = modifyState _{q=q}
    eval (Select ugr) = getProps >>= \{onSelect} -> liftEffect $ runEffectFn1 onSelect ugr
    eval (MaybeSearch ugren) = do 
        {enabled} <- getProps 
        {haveQueried} <- getState
        if haveQueried && enabled /= ugren then eval Search else pure unit
    eval Search = do
        {q} <- getState
        {enabled: UGREnabled {users,groups,roles}} <- getProps
        r <- lift $ searchUGR q {users,groups,roles}
        modifyState _ {results=r, haveQueried=true}
  pure {state:{q:"", results: mempty, haveQueried:false}, render: renderer render this}
  where 
    styles theme = {
        queryLine: {
            display: "flex"
        }
    }
