module Security.ACLEditor where 


import Prelude hiding (div)

import Control.Monad.Aff.Console (log)
import Control.Monad.Eff.Class (liftEff)
import Control.Monad.IOEffFn (IOFn1, mkIOFn1, runIOFn1)
import Control.Monad.Maybe.Trans (MaybeT(..), runMaybeT)
import Control.Monad.State (State, execState, get, gets)
import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import Control.Parallel (parallel, sequential)
import Data.Argonaut (decodeJson, encodeJson)
import Data.Array (deleteAt, find, fold, foldl, foldr, fromFoldable, index, insertAt, length, mapWithIndex, reverse, snoc, (!!))
import Data.Bifunctor (bimap)
import Data.Either (Either(..), either)
import Data.Lens (Lens', Prism', Traversal', assign, filtered, foldMapOf, modifying, over, preview, previewOn, prism', set, traversed, use, view, (^?))
import Data.Lens.Index (ix)
import Data.Lens.Iso.Newtype (_Newtype)
import Data.Lens.Record (prop)
import Data.List (List(..), null, uncons, (:))
import Data.Maybe (Maybe(..), fromMaybe, isJust, isNothing, maybe)
import Data.Newtype (class Newtype)
import Data.Nullable (toMaybe)
import Data.String (joinWith)
import Data.Symbol (SProxy(..))
import Data.Traversable (traverse)
import Data.Tuple (Tuple(Tuple), fst)
import Dispatcher (DispatchEff(DispatchEff))
import Dispatcher.React (ReactProps(ReactProps), ReactState(ReactState), createLifecycleComponent, createLifecycleComponent', didMount, getProps, getState, modifyState)
import DragNDrop.Beautiful (DropResult, dragDropContext, draggable, droppable)
import EQUELLA.Environment (baseUrl)
import MaterialUI.Button (button, disableRipple, raised)
import MaterialUI.ButtonBase (onClick)
import MaterialUI.Checkbox (checkbox)
import MaterialUI.Color (primary)
import MaterialUI.Dialog (dialog)
import MaterialUI.Divider (divider)
import MaterialUI.ExpansionPanelSummary (disabled)
import MaterialUI.FormControl (formControl)
import MaterialUI.FormControlLabel (control, formControlLabel, label)
import MaterialUI.Icon (icon_)
import MaterialUI.IconButton (iconButton)
import MaterialUI.Input (id) as P
import MaterialUI.Input (onChange)
import MaterialUI.InputLabel (inputLabel)
import MaterialUI.List (disablePadding, list)
import MaterialUI.ListItem (button) as P
import MaterialUI.ListItem (listItem)
import MaterialUI.ListItemIcon (listItemIcon_)
import MaterialUI.ListItemSecondaryAction (listItemSecondaryAction_)
import MaterialUI.ListItemText (disableTypography, listItemText)
import MaterialUI.ListItemText (primary, secondary) as P
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Modal (onClose, open)
import MaterialUI.Paper (paper)
import MaterialUI.PropTypes (Untyped, handle)
import MaterialUI.Properties (IProp, className, color, component, mkProp, style, variant)
import MaterialUI.Select (select, value)
import MaterialUI.Styles (withStyles)
import MaterialUI.SwitchBase (checked)
import MaterialUI.TextStyle (body1, subheading)
import MaterialUI.Typography (error, textSecondary, typography)
import Network.HTTP.Affjax (get, put_) as A
import React (ReactClass, ReactElement, createElement, createFactory)
import React.DOM (div, div', text)
import React.DOM.Props (className, ref, style) as P
import Security.Expressions (Expression(..)) as SE
import Security.Expressions (AccessEntry(AccessEntry), Expression, ExpressionTerm(Role, Group, User, SharedSecretToken, Referrer, Ip, Owner, Guests, LoggedInUsers, Everyone), IpRange(IpRange), OpType(OR, AND), TargetList(TargetList), TargetListEntry, collapseZero, entryToTargetList, expressionText, parseEntry, textForExpression, textForTerm, traverseExpr)
import Security.Resolved (ResolvedExpression(..), ResolvedTerm(..))
import Security.TermSelection (DialogType(..), termDialog)
import Template (template')
import UIComp.SpeedDial (speedDialActionU, speedDialIconU, speedDialU)
import Users.UserLookup (GroupDetails(GroupDetails), RoleDetails(RoleDetails), UserDetails(UserDetails), UserGroupRoles(UserGroupRoles), lookupUsers)

data ExprType = Unresolved Expression | Resolved ResolvedExpression | EmptyExpr

derive instance etEQ :: Eq ExprType

type ResolvedEntryR = {priv::String, granted::Boolean, override::Boolean, expr :: ExprType}
newtype ResolvedEntry = ResolvedEntry ResolvedEntryR

derive instance ntRE :: Newtype ResolvedEntry _
derive instance eqREnt :: Eq ResolvedEntry 

mapResolved :: (ResolvedExpression -> ExprType) -> ExprType -> ExprType 
mapResolved f (Resolved r) = f r
mapResolved _ o = o

data Command = Resolve | HandleDrop DropResult | SelectEntry Int | SaveChanges | Undo 
  | DeleteExpr Int | ChangeOp Int String | EditEntry Int (ResolvedEntryR -> ResolvedEntryR) | DeleteEntry Int
  | OpenDialog DialogType | AddFromDialog (Array ResolvedTerm) | CloseDialog 
  | NewPriv | DialState (Boolean -> Boolean)

type MyState = {
  acls :: Array ResolvedEntry,
  selectedIndex :: Maybe Int,
  terms :: Array ResolvedTerm,
  undoList :: List (Tuple (Maybe Int) (Array ResolvedEntry)), 
  openDialog :: Maybe DialogType, 
  changed :: Boolean, 
  dialOpen :: Boolean, 
  dialHidden :: Boolean
}

aclEditorClass :: ReactClass {
  acls :: Array TargetListEntry, 
  applyChanges :: IOFn1 (Array TargetListEntry) Unit,
  allowedPrivs :: Array String
}
aclEditorClass = withStyles styles $ createLifecycleComponent' (didMount Resolve) initialState render eval
  where 
  _resolvedExpr :: Prism' ExprType ResolvedExpression
  _resolvedExpr = prism' Resolved $ case _ of 
    Resolved r -> Just r
    _ -> Nothing
  _unresolvedExpr :: Prism' ExprType Expression
  _unresolvedExpr = prism' Unresolved $ case _ of 
    Unresolved r -> Just r
    _ -> Nothing
  _id = prop (SProxy :: SProxy "id")
  _changed = prop (SProxy :: SProxy "changed")
  _expr = prop (SProxy :: SProxy "expr")
  _priv = prop (SProxy :: SProxy "priv")
  _terms = prop (SProxy :: SProxy "terms")
  _openDialog = prop (SProxy :: SProxy "openDialog")
  _acls = prop (SProxy :: SProxy "acls")
  _undoList = prop (SProxy :: SProxy "undoList")
  _selectedIndex = prop (SProxy :: SProxy "selectedIndex")
  _granted = prop (SProxy :: SProxy "granted")
  _override = prop (SProxy :: SProxy "override")
  _ResolvedEntry :: Lens' ResolvedEntry ResolvedEntryR
  _ResolvedEntry = _Newtype
  currentEntry :: Int -> Traversal' (Array ResolvedEntry) ResolvedEntryR
  currentEntry ind = ix ind <<< _ResolvedEntry 

  styles theme = {
    buttons: {
      margin: theme.spacing.unit
    },
    accessEntry: {
      display: "flex"
    },
    scrollable: {
      overflow: "auto",
      height: "100%"
    },
    overallPanel: {
      display: "flex",
      flexDirection: "column",
      height: "100%", 
      width: "100%"
    }, 
    editorPanels: {
      display: "flex",
      height: "100%", 
      width: "100%"
    },
    entryList: {
      position: "relative",
      width: "35vw",
      flex: "0 0 auto"
    },
    currentEntryPanel: {
      position: "relative",
      display: "flex",
      flexDirection: "column",
      height: "100%",
      flexBasis: "50%", 
      marginLeft: theme.spacing.unit
    },
    commonPanel: {
      position: "relative",
      height: "100%",
      flexBasis: "45%",
      marginLeft: theme.spacing.unit
    },
    currentEntryDrop: {
      padding: theme.spacing.unit, 
      flexGrow: 1
    }, 
    opDrop: {
      height: 48
    }, 
    selectedEntry: {
      backgroundColor: theme.palette.action.selected
    }, 
    addTerm: {
      zIndex: 2000,
      position: "absolute",
      bottom: - theme.spacing.unit * 2,
      right: theme.spacing.unit * 1
    }, 
    dialog: {
      width: 600,
      height: 600
    }, 
    notBeingDragged: {

    }, 
    beingDraggedOver: {

    },
    entrySelected: {
      "& $hoverActions": {
        display: "block"
      }
    },
    aclEntry: {
      "$notBeingDragged &:hover $hoverActions": {
        display: "block"
      }
    },
    hoverActions : {
      display: "none",
      flexShrink: 0
    },
    exprLine: {
      display: "flex",
      justifyContent: "flex-end",
      alignItems: "flex-start", 
      width: "100%",
      height: 48
    },
    entryText: {
      flexGrow: 1
    }, 
    ellipsed: {
      whiteSpace: "nowrap",
      overflow: "hidden", 
      textOverflow: "ellipsis"
    }, 
    privSelect: {
      margin: theme.spacing.unit * 2
    }, 
    divideEntry: {
      marginTop: theme.spacing.unit * 2,
      marginBottom: theme.spacing.unit
    }
  }

  initialState (ReactProps {acls}) = ReactState {
      acls: either (const []) (map markForResolve) $ traverse parseEntry acls, 
      terms: commonTerms,
      selectedIndex: Nothing, 
      undoList: Nil, 
      openDialog: Nothing, 
      changed: false, 
      dialOpen: false, 
      dialHidden: false
    }
    where  
    markForResolve (AccessEntry {priv,granted,override,expr}) = 
      ResolvedEntry {priv,granted,override, expr: Unresolved expr}

  render {acls,terms,selectedIndex,openDialog,undoList,changed, dialOpen, dialHidden} 
      (ReactProps {classes,allowedPrivs}) (DispatchEff d) = 
    let expressionM = selectedIndex >>= \i -> Tuple i <$> previewOn acls (currentEntry i)
    in dragDropContext { onDragEnd: mkIOFn1 (d HandleDrop) } [ 
      div [P.className classes.overallPanel] [
        div [P.className classes.editorPanels] [
          div [P.className classes.entryList] [
            paper [className classes.scrollable ] [
              droppable {droppableId:"list", "type": "entry"} \p snap -> 
                div [P.ref p.innerRef, p.droppableProps, P.className $ droppedOnClass snap ] [
                  list [disablePadding true] $ mapWithIndex entryRow acls,
                  p.placeholder
                ]
            ]
          ],
          paper [className classes.currentEntryPanel ] $ maybe createNewPriv expressionContents expressionM,
          div [P.className classes.commonPanel] commonPanel 
        ],
        div [P.className classes.buttons] [ 
          button [variant raised, disabled cantSave, color primary, onClick $ command SaveChanges ] [ text "Save" ],
          button [variant raised, disabled cantUndo, onClick $ command Undo ] [ text "Undo" ]
        ],
        dialog [open $ isJust openDialog, onClose $ command CloseDialog] $ 
          maybe [] (\dt -> [ termDialog {onAdd: mkIOFn1 $ d AddFromDialog, 
            cancel: liftEff $ (d \_ -> CloseDialog) unit, dt} ]) openDialog
    ]
  ]
    where 
    onChangeStr :: forall r. (String -> Command) -> IProp (onChange::Untyped|r)
    onChangeStr f = onChange $ handle $ d $ \e -> f e.target.value
    commonPanel = let 
      dialChange = command <<< DialState
      closeDial = dialChange $ const false
      openDial = dialChange $ const true
      in [ 
        speedDialU {
          className: classes.addTerm, 
          icon: speedDialIconU {openIcon: icon_ [text "add"]}[], 
          ariaLabel: "Add expression", 
          open:dialOpen, hidden: dialHidden,
          onClick: dialChange not, onClose: closeDial,
          onMouseEnter: openDial, onMouseLeave: closeDial } 
        [
          action "people" "User/Group/Role" UserDialog,
          action "dns" "IP Range" (IpDialog $ IpRange 0 0 0 0 32),
          action "http" "HTTP Referrer" $ ReferrerDialog "http://*",
          action "apps" "Shared secret" $ SecretDialog "moodle"
        ],
        paper [className classes.scrollable ] [
          droppable {droppableId:"common"} \p _ -> 
            div [P.ref p.innerRef, p.droppableProps] [
              list [disablePadding true] $
                mapWithIndex (commonExpr "common" [] []) terms,
              p.placeholder
            ]
        ]
      ]
      where action i title dt = speedDialActionU {icon: icon_ [text i], title, onClick: command $ OpenDialog dt }

    command c = handle $ d \_ -> c
    createNewPriv = [ button [variant raised, className classes.privSelect, 
          onClick $ command NewPriv ] [text "Add Privilege"] ]
    droppedOnClass snap = if snap.isDraggingOver then classes.beingDraggedOver else classes.notBeingDragged
    cantUndo = null undoList
    cantSave = changed && (isJust $ find (isNothing <<< backToAccessEntry) acls)

    expressionContents (Tuple i {expr:exprType, priv}) =
      let exprEntries = case exprType of 
            Resolved expression -> [ 
              list [disablePadding true] $ 
                mapWithIndex (#) $ fromFoldable $ makeExpression 0 expression Nil 
            ]
            _ -> []
      in  [
        formControl [className classes.privSelect] [ 
          inputLabel [mkProp "htmlFor" "privSelect"] [text "Privilege"],
          select [value priv, P.id "privSelect", onChangeStr $ EditEntry i <<< set _priv] $ 
            (\p -> menuItem [mkProp "value" p] [text p]) <$> allowedPrivs
        ],
        divider [className classes.divideEntry],
        droppable {droppableId:"currentEntry"} \p _ -> 
          div [P.ref p.innerRef, p.droppableProps, P.className classes.currentEntryDrop] $ 
              snoc exprEntries p.placeholder
      ]

    stdDrag pfx content i = draggable {draggableId: pfx <> show i, index:i} \p _ -> 
      div' [
        div [P.ref p.innerRef, p.draggableProps, p.dragHandleProps] [
          content i
        ], 
        p.placeholder
      ]
    makeExpression indent expr l = case expr of 
      Term t _ -> Cons (\i -> commonExpr "term" [style { marginLeft: indentPixels }] [
        listItemSecondaryAction_ [ 
            iconButton [ onClick $ handle $ d \_ -> DeleteExpr i ] [ 
              icon_ [ text "delete" ] 
            ] 
        ]
      ] i t) l
      Op op exprs _ -> (Cons $ opEntry op ) (foldr (makeExpression (indent + 1)) l exprs)
      where 
      opEntry op = stdDrag "op" $ \i -> div [ P.className classes.opDrop, P.style {marginLeft: indentPixels} ] [ 
        select [value $ opValue op, onChangeStr $ ChangeOp i ] opItems
      ]
      indentPixels = indent * 16
    

    commonExpr pfx props actions i rt = stdDrag pfx (\_ -> 
      listItem props $ [ 
        listItemIcon_ [ 
          icon_ [ 
            text $ iconNameForResolved rt 
          ] 
        ], 
        listTextForResolved rt
      ] <> actions) i

    entryRow i (ResolvedEntry {granted,override,priv,expr}) = draggable { "type": "entry", draggableId: "entry" <> show i, index:i} \p _ -> 
      div [P.className $ joinWith " " $ (guard (selectedIndex == Just i) $> classes.entrySelected) <> [classes.aclEntry]] [
        div [P.ref p.innerRef, p.draggableProps, p.dragHandleProps] [
          let p = guard (eq i <$> selectedIndex # fromMaybe false) $> className classes.selectedEntry
          in listItem (p <> [ P.button true, disableRipple true, onClick $ handle $ d \_ -> SelectEntry i]) [ 
            div [P.className classes.exprLine ] [
              listItemText [ className classes.entryText, disableTypography true, P.primary $ privText, P.secondary secondLine ],
              div [ P.className classes.hoverActions ] [ 
                check override _override "Override",
                check (not granted) _granted "Revoked",
                iconButton [ onClick $ command $ DeleteEntry i ] [ icon_ [ text "delete" ] ] 
              ]
            ]
          ]
        ],
        p.placeholder
      ]
      where privText = typography [ variant subheading, className classes.ellipsed ] [ text $ if granted then priv else "Revoke - " <> priv ] 
            secondLine = textForExprType expr
            check o l t = formControlLabel [control $ checkbox [checked o, toggler i l ], label t ]
            toggler i l = onChange $ handle $ d \_ -> EditEntry i (over l not)

    stdExprLine t = typography [variant body1, className classes.ellipsed, color textSecondary ] [ text t ] 

    textForExprType (Unresolved std) = stdExprLine $ textForExpression std 
    textForExprType (Resolved rexpr) = stdExprLine $ expressionText textForResolved rexpr
    textForExprType EmptyExpr = typography [component "span", color error] [ text "* Required" ]
  
  listTextForResolved = case _ of 
    Already std -> listItemText [P.primary $ textForTerm std ] 
    ResolvedUser (UserDetails {username, firstName, lastName}) -> 
      listItemText [P.primary $ username, P.secondary $ firstName <> " " <> lastName ]
    ResolvedGroup (GroupDetails {name}) -> listItemText [ P.primary name ]
    ResolvedRole (RoleDetails {name}) -> listItemText [ P.primary name ]


  textForResolved :: ResolvedTerm -> String
  textForResolved = case _ of 
    Already std -> textForTerm std
    ResolvedUser (UserDetails {username}) ->  username
    ResolvedGroup (GroupDetails {name}) -> name
    ResolvedRole (RoleDetails {name}) -> name

  iconNameForResolved = case _ of 
    Already std -> iconNameForTerm std
    ResolvedUser (UserDetails _) -> iconNameForTerm (User "")
    ResolvedGroup (GroupDetails _) -> iconNameForTerm (Group "")
    ResolvedRole (RoleDetails _) -> iconNameForTerm (Role "")

  iconNameForTerm = case _ of 
   Group _ -> "people"
   Role _ -> "people"
   User _ -> "person"
   Everyone -> "public"
   LoggedInUsers -> "face"
   Ip _ -> "dns"
   Guests -> "person_outline"
   Owner -> "account_box"
   Referrer _ -> "http"
   SharedSecretToken _ -> "apps"
    
  toQuery :: Expression -> UserGroupRoles String String String
  toQuery = traverseExpr (\t _ -> UserGroupRoles (termQuery t)) \{exprs} _ -> fold exprs
    where
    termQuery (User uid) = {users:[uid],groups:[], roles:[]}
    termQuery (Group gid) = {users:[], groups:[gid], roles:[]}
    termQuery (Role rid) = {users:[], groups:[], roles:[rid]}
    termQuery _ = {users:[],groups:[],roles:[]}

  reorder :: forall a. Int -> Int -> Array a -> Array a 
  reorder sourceIndex destIndex a = fromMaybe a do
     let newdest = if destIndex < 1 then 0 else destIndex
     o <- index a sourceIndex
     d <- deleteAt sourceIndex a
     insertAt newdest o d

  deleteFrom :: Int -> ResolvedExpression -> Either Int (Tuple (Maybe ResolvedExpression) ResolvedExpression)
  deleteFrom i e = if i == 0 then pure (Tuple Nothing e) else case e of 
    Term _ _ -> Left $ (i - 1)
    origOp@(Op op exprs notted) -> 
      let foldOp (Left {i,l}) e = deleteFrom i e # bimap {i: _, l: Cons e l} (\(Tuple m e) -> Tuple (maybe l (flip Cons l) m) e)
          foldOp (Right (Tuple l me)) e = Right $ Tuple (Cons e l) me
          mkOp l e = Right $ Tuple (Just $ (Op op (reverse $ fromFoldable $ l) notted)) e
      in case foldl foldOp (Left {i: i - 1, l:Nil}) exprs of 
        Left {i:0,l} -> mkOp l origOp
        Left {i} -> Left $ i - 1
        Right (Tuple r me) -> mkOp r me

  insertInto :: ResolvedExpression -> Int -> ResolvedExpression -> ResolvedExpression
  insertInto nt i e = let 
    insertOrPos :: Int -> ResolvedExpression -> Either Int (List ResolvedExpression)
    insertOrPos 0 e = pure $ e : nt : Nil
    insertOrPos i e = case e of  
        t1@(Term _ _) -> Left (i - 1)
        (Op op exprs notted) -> 
          let foldOp (Left {i,l}) e = insertOrPos i e # bimap {i: _, l: Cons e l} (\nl -> nl <> l)
              foldOp o e = map (Cons e) o
              mkOp l = Right $ (Op op (reverse $ fromFoldable $ l) notted) : Nil
          in case foldl foldOp (Left {i:i - 1,l:Nil}) exprs of  
              Left {i:0,l} -> mkOp $ nt : l
              Left {i} -> Left $ i - 1
              Right r -> mkOp r
    in case insertOrPos i e of 
      Left 0 -> Op AND [e, nt] false
      Right (r : n : Nil) -> Op AND [n, r] false
      Right (r : Nil) -> r
      a -> e
  
  addUndo :: (Array ResolvedEntry -> Array ResolvedEntry) -> State MyState Unit
  addUndo f = do 
    oldEntries <- use _acls
    indx <- use _selectedIndex
    let newEntries = f oldEntries
    if newEntries == oldEntries then pure unit else do 
      assign _changed true
      modifying _undoList $ Cons (Tuple indx oldEntries)
      assign _acls newEntries

  modifyResolved :: (ResolvedExpression -> ResolvedExpression) -> State MyState Unit
  modifyResolved f = modifyExpression (mapResolved (f >>> Resolved))

  modifyExpression :: (ExprType -> ExprType) -> State MyState Unit
  modifyExpression f = void $ runMaybeT do 
    selectedIndex <- MaybeT (gets _.selectedIndex)
    let curExpr :: Traversal' (Array ResolvedEntry) ExprType
        curExpr = ix selectedIndex <<< _ResolvedEntry <<< _expr
    oldEx <- MaybeT $ (preview (_acls <<< curExpr) <$> get)
    let newEx = mapResolved (collapseZero >>> maybe EmptyExpr Resolved) $ f oldEx
    guard (oldEx /= newEx)
    lift $ addUndo $ set curExpr newEx

  copyToCurrent :: Int -> Int -> State MyState Unit
  copyToCurrent srcIx destIx = do 
    let insertNew t = case _ of 
         Resolved r -> Resolved $ insertInto (Term t false) destIx r
         EmptyExpr -> Resolved (Term t false)
         o -> o 
    ce <- use _terms
    ce !! srcIx # maybe (pure unit) (modifyExpression <<< insertNew)  

  eval (AddFromDialog dt) = 
    modifyState \s -> s {terms = append s.terms dt, openDialog=Nothing,dialHidden=false}
      
  eval (DialState open) = modifyState \s -> s {dialOpen = open s.dialOpen}
  eval NewPriv = modifyState $ execState $ do 
    oldEntries <- use _acls
    addUndo (flip snoc $ ResolvedEntry {priv:"", granted:true, override:false, expr: EmptyExpr})
    assign _selectedIndex $ Just $ length oldEntries
  eval CloseDialog = modifyState _{openDialog=Nothing,dialHidden=false}
  eval (OpenDialog dt) = modifyState _{openDialog=Just dt,dialOpen=false,dialHidden=true}
  eval (DeleteEntry ind) = do 
    modifyState $ execState $ do 
        addUndo \l -> fromMaybe l $ deleteAt ind l  
  eval (EditEntry ind f) = do
    modifyState $ execState $ do 
        addUndo $ over (ix ind <<< _Newtype) f
  eval (ChangeOp ind op) = do 
    let editOp (Op _ exprs n) | Just newOp <- valToOpType op = Op newOp exprs n 
        editOp o = o
        changeOp r (Right (Tuple _ e)) = editOp e
        changeOp r _ = r
    modifyState $ execState $ modifyResolved (\r -> changeOp r $ deleteFrom ind r)
  eval (DeleteExpr i) = do 
    let delExpr e@(Resolved r) = either (const e) (fst >>> maybe EmptyExpr Resolved) $ deleteFrom i r
        delExpr o = o
    modifyState $ execState $ modifyExpression delExpr
  eval Undo = do 
    modifyState $ execState do 
      undos <- use _undoList
      uncons undos # maybe (pure unit) \{head:Tuple ix list, tail} -> do 
        assign _undoList tail
        assign _acls list
        assign _selectedIndex ix

  eval SaveChanges = do 
    {applyChanges} <- getProps
    {acls} <- getState
    (traverse backToAccessEntry acls) # maybe (pure unit) \entries -> do 
      liftEff $ runIOFn1 applyChanges $ entryToTargetList <$> entries
      modifyState _{changed=false}
    

  eval (SelectEntry entry) = do 
    modifyState $ over _selectedIndex case _ of 
      Just e | e == entry -> Nothing
      _ -> Just entry

  eval Resolve = do
    {acls} <- getState
    let ugr = foldMapOf (traversed <<< (_ResolvedEntry <<< _expr <<< _unresolvedExpr)) toQuery acls
    (UserGroupRoles {users,groups,roles}) <- lift $ lookupUsers ugr
    let filterById :: forall a r. Newtype a {id::String|r} => Array a -> String -> Maybe a
        filterById arr uid = arr ^? (traversed <<< (filtered $ view ((_Newtype :: Lens' a {id::String|r}) <<< _id) >>> eq uid))
        resolveTerm = case _ of 
          (User uid) | Just ud <- filterById users uid -> ResolvedUser ud
          (Group gid) | Just gd <- filterById groups gid -> ResolvedGroup gd
          (Role rid) | Just rd <- filterById roles rid -> ResolvedRole rd
          (User uid) -> ResolvedUser (UserDetails {id:uid, username: "Unknown user with id " <> uid, firstName:"", lastName:"", email:Nothing})
          other -> Already other
        resolve = traverseExpr (\t n -> Term (resolveTerm t) n) \{op,exprs} n -> Op op exprs n
    modifyState $ over (_acls <<< traversed <<< _ResolvedEntry <<< _expr) case _ of 
      Unresolved u -> Resolved (resolve u)
      o -> o

  eval (HandleDrop dr@{source:{index:sourceIndex, droppableId:sourceId}}) | Just {index:destIndex,droppableId:destId} <- toMaybe dr.destination = 
    let handleDrop sId dId | sId == dId && sourceIndex /= destIndex = 
          let reorderCurrent = modifyResolved $ (\e -> case deleteFrom sourceIndex e of 
                  Right (Tuple (Just ne) re)-> insertInto re destIndex ne
                  _ -> e
                )
              l = case sId of 
                    "list" -> addUndo (reorder sourceIndex destIndex)
                    "currentEntry" -> reorderCurrent
                    _       -> modifying _terms (reorder sourceIndex destIndex)
          in modifyState $ execState l
        handleDrop "common" "currentEntry" = do 
          modifyState $ execState $ copyToCurrent sourceIndex destIndex 
        handleDrop _ _ = pure unit
        
    in handleDrop sourceId destId
  eval (HandleDrop _) = pure unit

commonTerms :: Array ResolvedTerm
commonTerms = [
  Already Everyone,
  Already LoggedInUsers, 
  Already Guests,
  Already Owner,
  Already (Role "TestRole"),
  Already (Group "TestGroup"),
  Already (User "TestUser"),
  Already (Ip $ IpRange 1 2 3 5 24),
  Already (Referrer "http://*"),
  Already (SharedSecretToken "moodle")
]

opValue :: OpType -> String 
opValue AND = "and"
opValue OR = "or"

valToOpType :: String -> Maybe OpType 
valToOpType = case _ of 
  "and" -> Just AND 
  "or" -> Just OR 
  _ -> Nothing 

opName :: OpType -> String
opName AND = "Match all"
opName OR = "Match one"

opItems :: Array ReactElement
opItems = mkOp <$> [ AND, OR ]
  where 
  mkOp o = menuItem [mkProp "value" $ opValue o] [ text $ opName o ]

backToAccessEntry :: ResolvedEntry -> Maybe AccessEntry
backToAccessEntry (ResolvedEntry {priv,granted,override,expr}) = 
  AccessEntry <<< {priv,granted,override,expr: _} <$> convertExpr expr
  where 
  convertExpr (Unresolved e) = pure $ e
  convertExpr (Resolved re) = pure $ convertRE re
  convertExpr _ = Nothing
  convertRE (Term rt b) = 
    let seterm = case rt of 
          Already t -> t 
          ResolvedUser (UserDetails {id}) -> User id  
          ResolvedGroup (GroupDetails {id}) -> Group id  
          ResolvedRole (RoleDetails {id}) -> Role id
    in SE.Term seterm b
  convertRE (Op o exprs n) = SE.Op o (convertRE <$> exprs) n

data TestCommand = Init | SaveIt (Array TargetListEntry)

testEditor :: ReactElement
testEditor = createFactory (createLifecycleComponent (didMount Init) {s:Nothing} render eval) {}
  where 
  render {s} (DispatchEff d) = 
    s # maybe (div' []) \{entries,allowedPrivs} -> 
      template' {fixedViewPort:true, menuExtra: [], 
        mainContent: div [P.style {width: "100%", height: "100%"}] [
          createElement aclEditorClass {acls:entries, allowedPrivs, applyChanges: mkIOFn1 $ d SaveIt} []
        ], 
        title: "TEST", titleExtra:Nothing }
  eval Init = do 
    Tuple r1 r2 <- lift $ sequential $ Tuple <$> 
      parallel (A.get $ baseUrl <> "api/acl") <*> 
      parallel (A.get $ baseUrl <> "api/acl/privileges?node=INSTITUTION")
    either (lift <<< log) (\e -> modifyState _ {s=Just e}) do 
      TargetList {entries} <- decodeJson r1.response
      allowedPrivs <- decodeJson r2.response
      pure $ { entries, allowedPrivs}
    pure unit
  eval (SaveIt entries) = do 
    r <- lift $ A.put_ (baseUrl <> "api/acl") (encodeJson (TargetList {entries}))
    pure unit
