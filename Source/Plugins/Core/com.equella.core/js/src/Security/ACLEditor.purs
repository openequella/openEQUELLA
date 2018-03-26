module Security.ACLEditor where 


import Prelude
import Security.Expressions

import Control.Monad.Aff.Class (liftAff)
import Control.Monad.IOSync (IOSync(..))
import Control.Monad.IOSync.Class (liftIOSync)
import Control.Monad.Trans.Class (lift)
import Data.Array (find, mapMaybe)
import Data.Either (Either(..), either)
import Data.Lens (Lens', Traversal, _Left, filtered, foldMapOf, over, preview, set, traversed, view, (^?))
import Data.Lens.Iso.Newtype (_Newtype)
import Data.Lens.Lens.Product (_1)
import Data.Lens.Record (prop)
import Data.Maybe (Maybe(..))
import Data.Newtype (class Newtype, unwrap)
import Data.Symbol (SProxy(..))
import Dispatcher (effEval)
import Dispatcher.React (ReactProps(..), ReactState(..), createComponent, createComponent', createLifecycleComponent', didMount, getState, modifyState)
import MaterialUI.Table (table, table_)
import MaterialUI.TableBody (tableBody, tableBody_)
import MaterialUI.TableCell (tableCell_)
import MaterialUI.TableRow (tableRow, tableRow_)
import React (ReactElement, createFactory)
import React.DOM (text)
import React.DOM.Dynamic (div')
import React.DOM.Props (ref)
import Template (renderData, template)
import UserLookup (GroupDetails(..), RoleDetails(..), UserDetails(..), UserGroupRoles(..), lookupUsers)

type ResolveEntryR = {priv::String, granted::Boolean, override::Boolean, term::Either ExpressionTerm ResolvedExpression}
newtype ResolvedEntry = ResolvedEntry ResolveEntryR

derive instance ntRE :: Newtype ResolvedEntry _

data ResolvedExpression = Already ExpressionTerm | ResolvedUser UserDetails | ResolvedGroup GroupDetails | ResolvedRole RoleDetails

data Command = Resolve 

aclEditor :: {acls :: Array AccessEntry, onChange :: Array AccessEntry -> IOSync Unit } -> ReactElement
aclEditor = createFactory $ createLifecycleComponent' (didMount Resolve) initialState render eval
  where 
  _id = prop (SProxy :: SProxy "id")
  _term = prop (SProxy :: SProxy "term")
  _acls = prop (SProxy :: SProxy "acls")
  _ResolvedEntry :: Lens' ResolvedEntry ResolveEntryR
  _ResolvedEntry = _Newtype

  initialState (ReactProps {acls}) = ReactState {acls:markForResolve <$> acls}
    where 
    markForResolve (AccessEntry {priv,granted,override,term}) = ResolvedEntry {priv,granted,override, term: Left term}

  render {acls} = table_ [ 
      tableBody_ $ entryRow <$> acls
  ]
    where 
    entryRow (ResolvedEntry {granted,priv,term}) = tableRow_ [
      tableCell_ [ text priv ],
      tableCell_ [ text $ show granted ],
      tableCell_ [ text $ textForTerm term]
    ]
  textForTerm (Left std) = textForNormalTerm std 
  textForTerm (Right r) = case r of 
    (Already std) -> textForNormalTerm std
    (ResolvedUser (UserDetails {username})) -> "USERNAME:" <> username
    (ResolvedGroup (GroupDetails {name})) -> "GROUP NAME:" <> name
    (ResolvedRole (RoleDetails {name})) -> "ROLE NAME:" <> name
  
  textForNormalTerm  = case _ of 
   Everyone -> "Everyone"
   LoggedInUsers -> "Logged in users"
   Owner -> "Owner"
   Guests -> "Guests"
   User uid -> "User with id " <> uid 
   Group gid -> "Group with id " <> gid
   Role rid -> "Role with id " <> rid 
   Ip ip -> "IP Address " <> ip 
   Referrer ref -> "Referrer " <> ref
   ShareSecretToken tokenId -> "Shared secret token " <> tokenId
    

  toQuery (User uid) = {users:[uid],groups:[], roles:[]}
  toQuery (Group gid) = {users:[], groups:[gid], roles:[]}
  toQuery (Role rid) = {users:[], groups:[], roles:[rid]}
  toQuery _ = {users:[],groups:[],roles:[]}

  eval Resolve = do
    {acls} <- getState
    let ugr = foldMapOf (traversed <<< (_ResolvedEntry <<< _term <<< _Left)) (toQuery >>> UserGroupRoles) acls
    (UserGroupRoles {users,groups,roles}) <- lift $ lookupUsers ugr
    let filterById :: forall a r. Newtype a {id::String|r} => Array a -> String -> Maybe a
        filterById arr uid = arr ^? (traversed <<< (filtered $ view ((_Newtype :: Lens' a {id::String|r}) <<< _id) >>> eq uid))
        resolve (User uid) | Just ud <- filterById users uid = ResolvedUser ud
        resolve (Group gid) | Just gd <- filterById groups gid = ResolvedGroup gd
        resolve (Role rid) | Just rd <- filterById roles rid = ResolvedRole rd
        resolve (User uid) = ResolvedUser (UserDetails {id:uid, username: "Unknown user with id " <> uid, firstName:"", lastName:"", email:Nothing})
        resolve other = Already other
    modifyState $ over (_acls <<< traversed <<< _ResolvedEntry <<< _term) (either (Right <<< resolve) Right)

sampleEntries = [
    AccessEntry {granted:true, priv: "DISCOVER_ITEM", override:false, term: User renderData.user.id},
    AccessEntry {granted:true, priv: "DISCOVER_ITEM", override:false, term: User "doolse"}
]

testEditor :: ReactElement
testEditor = template {mainContent: aclEditor {acls:sampleEntries, onChange: \_ -> liftIOSync $ pure unit}, title: "TEST", titleExtra:Nothing }
