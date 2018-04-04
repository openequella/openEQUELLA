module Security.ACLEditor where 


import Prelude hiding (div)
import Security.Expressions
import UUID

import Control.Monad.Aff.Class (liftAff)
import Control.Monad.Eff.Unsafe (unsafePerformEff)
import Control.Monad.IOEffFn (mkIOFn1)
import Control.Monad.IOSync (IOSync(..), runIOSync)
import Control.Monad.IOSync.Class (liftIOSync)
import Control.Monad.Trans.Class (lift)
import Data.Array (deleteAt, find, index, insertAt, mapMaybe, mapWithIndex)
import Data.Either (Either(..), either)
import Data.Lens (Lens', Traversal, _Left, filtered, foldMapOf, over, preview, set, traversed, view, (^?))
import Data.Lens.Iso.Newtype (_Newtype)
import Data.Lens.Lens.Product (_1)
import Data.Lens.Record (prop)
import Data.Maybe (Maybe(..), fromMaybe)
import Data.Newtype (class Newtype, unwrap)
import Data.Nullable (toMaybe)
import Data.Symbol (SProxy(..))
import Debug.Trace (spy, traceAny)
import Dispatcher (DispatchEff(..), effEval)
import Dispatcher.React (ReactProps(..), ReactState(..), createComponent, createComponent', createLifecycleComponent', didMount, getState, modifyState)
import DragNDrop.Beautiful (DropResult, dragDropContext, draggable, droppable)
import MaterialUI.List (disablePadding, list, list_)
import MaterialUI.ListItem (listItem_)
import MaterialUI.ListItemText (listItemText, primary, secondary)
import MaterialUI.Paper (paper_)
import MaterialUI.Properties (className, mkProp)
import MaterialUI.Styles (withStyles)
import MaterialUI.Table (table, table_)
import MaterialUI.TableBody (tableBody, tableBody_)
import MaterialUI.TableCell (tableCell_)
import MaterialUI.TableRow (tableRow, tableRow_)
import React (ReactElement, createFactory)
import React.DOM (div, div', h2, text)
import React.DOM.Dynamic (h2', h3')
import React.DOM.Props (className, ref) as P
import React.DOM.Props (style)
import Template (renderData, template)
import Unsafe.Coerce (unsafeCoerce)
import UserLookup (GroupDetails(..), RoleDetails(..), UserDetails(..), UserGroupRoles(..), lookupUsers)

type ResolveEntryR = {uuid::String, priv::String, granted::Boolean, override::Boolean, term::Either ExpressionTerm ResolvedExpression}
newtype ResolvedEntry = ResolvedEntry ResolveEntryR

derive instance ntRE :: Newtype ResolvedEntry _

data ResolvedExpression = Already ExpressionTerm | ResolvedUser UserDetails | ResolvedGroup GroupDetails | ResolvedRole RoleDetails

data Command = Resolve | HandleDrop DropResult

aclEditor :: {acls :: Array AccessEntry, onChange :: Array AccessEntry -> IOSync Unit } -> ReactElement
aclEditor = createFactory $ withStyles styles $ createLifecycleComponent' (didMount Resolve) initialState render eval
  where 
  _id = prop (SProxy :: SProxy "id")
  _term = prop (SProxy :: SProxy "term")
  _acls = prop (SProxy :: SProxy "acls")
  _ResolvedEntry :: Lens' ResolvedEntry ResolveEntryR
  _ResolvedEntry = _Newtype

  styles theme = {
    accessEntry: {
      display: "flex"
    }
  }

  initialState (ReactProps {acls}) = ReactState {acls:markForResolve <$> acls}
    where  
    markForResolve (AccessEntry {priv,granted,override,term}) = ResolvedEntry {uuid: unsafePerformEff $ runIOSync newUUID, priv,granted,override, term: Left term}

  render {acls} (ReactProps {classes}) (DispatchEff d) = dragDropContext { onDragEnd: mkIOFn1 (d HandleDrop) } [ 
    droppable {droppableId:"test"} \p _ ->
      div [P.ref p.innerRef, p.droppableProps] [
        paper_ [
          list [disablePadding true] $
            mapWithIndex entryRow acls <> [p.placeholder]
        ]
      ]
  ]
    where 
    entryRow i (ResolvedEntry {uuid,granted,priv,term}) = draggable {draggableId: show i, index:i} \p _ -> 
      div' [
        div [P.ref p.innerRef, p.draggableProps, p.dragHandleProps] [
          listItem_ [ 
            listItemText [ primary $ text priv, secondary $ textForTerm term ]
          ]
        ],
        p.placeholder
      ]
  textForTerm (Left std) = textForNormalTerm std 
  textForTerm (Right r) = case r of 
    (Already std) -> textForNormalTerm std
    (ResolvedUser (UserDetails {username})) -> "USERNAME:" <> username
    (ResolvedGroup (GroupDetails {name})) -> "GROUP NAME:" <> name
    (ResolvedRole (RoleDetails {name})) -> "ROLE NAME:" <> name
  
  textForNormalTerm = case _ of 
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
  eval (HandleDrop dr@{source:{index:sourceIndex}}) | Just {index:destIndex} <- toMaybe dr.destination = do 
    modifyState $ over _acls $ \a -> fromMaybe a do
     let newdest = if destIndex < 1 then 0 else destIndex
     o <- index a (traceAny {source: sourceIndex} \_ -> sourceIndex)
     d <- deleteAt sourceIndex a
     insertAt (traceAny {dest:destIndex} \_ -> newdest) o d
  eval (HandleDrop _) = pure unit

sampleEntries = [
    AccessEntry {granted:true, priv: "DISCOVER_ITEM", override:false, term: User "dasd"}, -- renderData.user.id},
    AccessEntry {granted:true, priv: "DISCOVER_ITEM", override:false, term: User "doolse"},
    AccessEntry {granted:true, priv: "DISCOVER_ITEM", override:false, term: Group "dff74147-98b4-34d3-e193-d3eeada6d836"},
    AccessEntry {granted:true, priv: "VIEW_ITEM", override:false, term: User "somebody"}
]

testEditor :: ReactElement
testEditor = template {mainContent: aclEditor {acls:sampleEntries, onChange: \_ -> liftIOSync $ pure unit}, title: "TEST", titleExtra:Nothing }
