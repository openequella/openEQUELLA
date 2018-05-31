module Security.ACLEditor where 


import Prelude hiding (div)

import Common.CommonStrings (commonAction, commonString)
import Common.Icons (groupIconName, roleIconName, userIconName)
import Control.Monad.Aff (forkAff)
import Control.Monad.Aff.Class (liftAff)
import Control.Monad.Eff (Eff)
import Control.Monad.Eff.Class (liftEff)
import Control.Monad.IOEffFn (IOFn1, mkIOFn1, runIOFn1)
import Control.Monad.IOSync (IOSync)
import Control.Monad.Maybe.Trans (MaybeT(..), runMaybeT)
import Control.Monad.Reader (ask, runReaderT)
import Control.Monad.State (State, execState, get, gets, modify, runState)
import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import DOM.HTML (window)
import DOM.HTML.Types (htmlDocumentToDocument)
import DOM.HTML.Window (document)
import DOM.Node.Document as D
import DOM.Node.Node (appendChild)
import DOM.Node.ParentNode (QuerySelector(..), querySelector)
import DOM.Node.Types (Node, documentToParentNode, elementToNode)
import Data.Argonaut (decodeJson)
import Data.Array (any, catMaybes, deleteAt, fold, foldr, fromFoldable, index, insertAt, last, length, mapMaybe, mapWithIndex, nub, snoc, union, updateAt, (!!))
import Data.Either (Either(..), either)
import Data.Lens (Lens', Prism', Traversal', assign, filtered, foldMapOf, modifying, over, preview, previewOn, prism', set, traversed, use, view, (^?))
import Data.Lens.Index (ix)
import Data.Lens.Iso.Newtype (_Newtype)
import Data.Lens.Record (prop)
import Data.List (List(Cons, Nil), null, uncons)
import Data.Maybe (Maybe(..), fromMaybe, maybe)
import Data.Newtype (class Newtype)
import Data.Nullable (toMaybe)
import Data.String (joinWith)
import Data.Symbol (SProxy(..))
import Data.Traversable (traverse, traverse_)
import Data.Tuple (Tuple(Tuple))
import Dispatcher (DispatchEff(DispatchEff), dispatch)
import Dispatcher.React (ReactProps(ReactProps), ReactState(ReactState), createLifecycleComponent', didMount, getProps, getState, modifyState)
import DragNDrop.Beautiful (DropResult, dragDropContext, draggable, droppable)
import EQUELLA.Environment (baseUrl, prepLangStrings)
import Global (encodeURIComponent)
import MaterialUI.Button (button, disableRipple, raised)
import MaterialUI.Checkbox (checkbox)
import MaterialUI.Color as C
import MaterialUI.Dialog (dialog)
import MaterialUI.DialogActions (dialogActions_)
import MaterialUI.DialogContent (dialogContent)
import MaterialUI.Divider (divider)
import MaterialUI.Event (Event)
import MaterialUI.ExpansionPanelSummary (disabled)
import MaterialUI.FormControl (formControl)
import MaterialUI.FormControlLabel (control, formControlLabel, label)
import MaterialUI.FormHelperText (formHelperText, formHelperText_)
import MaterialUI.Icon (icon_)
import MaterialUI.IconButton (iconButton)
import MaterialUI.Input (id) as P
import MaterialUI.InputLabel (inputLabel)
import MaterialUI.List (disablePadding, list, list_)
import MaterialUI.ListItem (button) as P
import MaterialUI.ListItem (listItem)
import MaterialUI.ListItemIcon (listItemIcon_)
import MaterialUI.ListItemText (disableTypography, listItemText)
import MaterialUI.ListItemText (primary, secondary) as P
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Modal (open)
import MaterialUI.Paper (paper)
import MaterialUI.PropTypes (EventHandler, toHandler)
import MaterialUI.Properties (IProp, className, color, component, mkProp, onChange, onClick, onClose, variant)
import MaterialUI.Select (select, value)
import MaterialUI.Styles (withStyles)
import MaterialUI.SwitchBase (checked)
import MaterialUI.TextStyle (body1, caption, headline, subheading, title) as TS
import MaterialUI.Tooltip (tooltip, title)
import MaterialUI.Typography (error, textSecondary, typography)
import Network.HTTP.Affjax (get, post_, post_') as AJ
import React (ReactClass, ReactElement, stopPropagation)
import React.DOM (div, div', text)
import React.DOM.Props (className, ref, style) as P
import Security.Expressions (AccessEntry(AccessEntry), Expression, ExpressionTerm(Role, Group, User, Owner, Guests, LoggedInUsers, Everyone, SharedSecretToken, Referrer, Ip), IpRange(IpRange), OpType(OR, AND), ParsedTerm(..), TargetListEntry(TargetListEntry), collapseZero, entryToTargetList, expressionText, parseTerm, parseWho, termToWho, textForExpression, textForTerm, traverseExpr)
import Security.Expressions (Expression(..)) as SE
import Security.Resolved (ResolvedExpression(..), ResolvedTerm(..), findExprInsert, findExprModify, resolvedToTerm)
import Security.TermSelection (DialogType(..), termDialog)
import UIComp.SpeedDial (speedDialActionU, speedDialIconU, speedDialU)
import Unsafe.Coerce (unsafeCoerce)
import Users.SearchUser (UGREnabled(..))
import Users.UserLookup (GroupDetails(GroupDetails), RoleDetails(RoleDetails), UserDetails(UserDetails), UserGroupRoles(UserGroupRoles), lookupUsers)

foreign import renderToPortal :: Node -> ReactElement -> ReactElement

data ExprType = Unresolved Expression | Resolved ResolvedExpression | InvalidExpr String

data TermType = UnresolvedTerm ExpressionTerm | ResolvedTerm ResolvedTerm

derive instance ttEQ :: Eq TermType 

derive instance etEQ :: Eq ExprType

type ResolvedEntryR = {priv::String, granted::Boolean, override::Boolean, expr :: ExprType}
newtype ResolvedEntry = ResolvedEntry ResolvedEntryR

derive instance ntRE :: Newtype ResolvedEntry _
derive instance eqREnt :: Eq ResolvedEntry 

mapResolved :: (ResolvedExpression -> ExprType) -> ExprType -> ExprType 
mapResolved f (Resolved r) = f r
mapResolved _ o = o

data Command = Init | Resolve | HandleDrop DropResult | SelectEntry Int | Undo 
  | DeleteExpr Int | ChangeOp Int String | EditEntry Int (ResolvedEntryR -> ResolvedEntryR) | DeleteEntry Int
  | OpenDialog DialogType | AddFromDialog (Array ResolvedTerm) | CloseDialog 
  | NewPriv | FinishNewPriv (Maybe String) | DialState (Boolean -> Boolean) | ToggleNot Int | Expand Int | Updated (Array TargetListEntry)

type MyState = {
  acls :: Array ResolvedEntry,
  selectedIndex :: Maybe Int,
  terms :: Array TermType,
  undoList :: List (Tuple (Maybe Int) (Array ResolvedEntry)), 
  newPrivDialog :: Boolean,
  openDialog :: Maybe DialogType, 
  showDialog :: Boolean,
  dialOpen :: Boolean, 
  dialHidden :: Boolean, 
  dragPortal :: Maybe Node
}

aclEditorClass :: ReactClass {
  acls :: Array TargetListEntry, 
  onChange :: IOFn1 {canSave :: Boolean, getAcls :: IOSync (Array TargetListEntry) } Unit,
  allowedPrivs :: Array String
}
aclEditorClass = withStyles styles $ createLifecycleComponent' lifeCycle initialState render eval
  where 
  lifeCycle = do 
    didMount Init
    modify _ {componentDidUpdate = \this {acls:oldAcls} _ -> dispatch eval this (Updated oldAcls)}
    
  aclString = prepLangStrings aclRawStrings
  _resolvedExpr :: Prism' ExprType ResolvedExpression
  _resolvedExpr = prism' Resolved $ case _ of 
    Resolved r -> Just r
    _ -> Nothing
  _unresolvedExpr :: Prism' ExprType Expression
  _unresolvedExpr = prism' Unresolved $ case _ of 
    Unresolved r -> Just r
    _ -> Nothing
  _unresolvedTerm :: Prism' TermType ExpressionTerm
  _unresolvedTerm = prism' UnresolvedTerm $ case _ of 
    UnresolvedTerm r -> Just r
    _ -> Nothing
  
  _id = prop (SProxy :: SProxy "id")
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
      display: "flex",
      height: "100%",
      flexDirection: "column"
    },
    overallPanel: {
      display: "flex",
      flexDirection: "column",
      height: "100%"
    },
    editorPanels: {
      display: "grid",
      height: "100%", 
      width: "100%", 
      gridTemplateColumns: "37% 33% 30%",
      gridTemplateRows: "100%"
    },
    entryList: {
      position: "relative",
      gridColumnStart: 1,
      display: "flex", 
      flexDirection: "column"
    },
    currentEntryPanel: {
      position: "relative",
      display: "flex",
      flexDirection: "column",
      gridColumnStart: 2, 
      marginLeft: theme.spacing.unit
    },
    commonPanel: {
      position: "relative",
      display: "flex",
      flexDirection: "column",
      gridColumnStart: 3, 
      marginLeft: theme.spacing.unit
    },
    currentEntryDrop: {
      padding: theme.spacing.unit, 
      flexGrow: 1, 
      width: "100%"
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
      bottom: theme.spacing.unit,
      right: theme.spacing.unit
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
      margin: theme.spacing.unit * 2,
      flexShrink: 0
    }, 
    divideEntry: {
      marginTop: theme.spacing.unit * 2,
      marginBottom: theme.spacing.unit
    }, 
    flexCentered: {
      alignSelf: "center",
      margin: theme.spacing.unit
    },
    dropText: {
      position: "absolute"
    }, 
    termEntry: {
      width: "100%",
      display: "flex",
      position: "relative",
      textAlign: "left",
      alignItems: "center",
      justifyContent: "flex-start",
      textDecoration: "none", 
      height: 48, 
      paddingLeft: theme.spacing.unit * 2,
      "&:hover $hoverActions": {
        display: "block"
      }      
    }, 
    privDialog: {
      width: "30em",
      minHeight: "20em"
    }
  }

  convertToInternal a = 
    let acls = markForResolve <$> a
        collectTerm (ResolvedEntry {expr:Unresolved e}) = 
              traverseExpr (\rt _ -> [UnresolvedTerm rt]) (\{exprs} _ -> join exprs) e 
        collectTerm _ = []
    in {terms: union (UnresolvedTerm <$> commonTerms) $ nub (acls >>= collectTerm), acls}
    where 
    markForResolve (TargetListEntry {privilege:priv,granted,override,who}) = 
          ResolvedEntry {priv,granted,override, expr: either (InvalidExpr <<< show) Unresolved $ parseWho who}

  initialState (ReactProps {acls:a}) = 
    let {acls,terms} = convertToInternal a
    in ReactState {
      acls, 
      terms,
      selectedIndex: Nothing, 
      undoList: Nil, 
      openDialog: Nothing, 
      showDialog: false,
      dialOpen: false, 
      dialHidden: false,
      dragPortal: Nothing, 
      newPrivDialog: false
    }
    

  render s@{acls,terms,selectedIndex,openDialog,showDialog,undoList,dialOpen, dialHidden,dragPortal} 
      (ReactProps {classes,allowedPrivs}) (DispatchEff d) = 
    let expressionM = selectedIndex >>= \i -> Tuple i <$> previewOn acls (currentEntry i)
    in dragDropContext { onDragEnd: mkIOFn1 (d HandleDrop) } $ [ 
      div [P.className classes.overallPanel] [
        div [P.className classes.editorPanels] [
          paper [className classes.entryList] [
            typography [variant TS.title, className classes.flexCentered] [ text aclString.privileges],
            createNewPriv,
            div [ P.className classes.scrollable ] [aclEntries]
          ],
          paper [className classes.currentEntryPanel ] $ [ 
            typography [variant TS.title, className classes.flexCentered] [ text aclString.expression]
          ] <> maybe placeholderExpr expressionContents expressionM,
          paper [className classes.commonPanel] commonPanel 
        ]
      ]
    ] <> (catMaybes [ 
      map renderDialog openDialog, 
      Just renderNewPriv
    ])
    where 
    renderDialog dt = termDialog {open:showDialog, onAdd: mkIOFn1 $ d AddFromDialog, cancel: liftEff $ (d \_ -> CloseDialog) unit, dt}
    renderNewPriv = dialog [open s.newPrivDialog, onClose close] [
        dialogContent [className classes.privDialog] [
          typography [variant TS.headline] [ text aclString.selectpriv],
          list_ $ privItem <$> allowedPrivs
        ], 
        dialogActions_ [
          button [color C.secondary, onClick $ close] [ text commonAction.cancel ]
        ]
      ]
      where 
      close = d \_ -> FinishNewPriv Nothing
      privItem i = listItem [P.button true, onClick $ d \_ -> FinishNewPriv (Just i)] [
        listItemText [P.primary i]
      ]


    onChangeStr :: forall r. (String -> Command) -> IProp (onChange::EventHandler Event|r)
    onChangeStr f = onChange $ d $ \e -> f e.target.value

    commonPanel = let 
      dialChange = toHandler <<< command <<< DialState
      closeDial = dialChange $ const false
      openDial = dialChange $ const true
      in [ 
        speedDialU {
          className: classes.addTerm, 
          icon: speedDialIconU {openIcon: icon_ [text "add"]}[], 
          ariaLabel: aclString.addexpression, 
          open:dialOpen, hidden: dialHidden,
          onClose: closeDial,
          onMouseEnter: openDial, onMouseLeave: closeDial } 
        [
          action "people" aclString.new.ugr $ UserDialog $ UGREnabled {users:true, groups:true, roles:true},
          action "dns" aclString.new.ip (IpDialog $ IpRange 0 0 0 0 32),
          action "http" aclString.new.referrer $ ReferrerDialog "http://*",
          action "apps" aclString.new.token $ SecretDialog ""
        ],
        typography [variant TS.title, className classes.flexCentered] [ text aclString.targets],
        div [P.className classes.scrollable ] [
          droppable {droppableId:"common"} \p _ -> 
            div [P.ref p.innerRef, p.droppableProps, P.style {width: "100%"}] $
                mapWithIndex (commonExpr "common" [] [] false) terms
            
        ]
      ]
      where action i title dt = speedDialActionU {icon: icon_ [text i], title, onClick: toHandler $ command $ OpenDialog dt }

    command :: forall a e. Command -> (a -> Eff e Unit)
    command c = d \_ -> c
    placeholderExpr = [ typography [ variant TS.caption, className classes.flexCentered ] [ text aclString.privplaceholder] ]
    createNewPriv = div' [ 
      button [variant raised, className classes.privSelect, onClick $ command NewPriv ] [text aclString.addpriv], 
      button [variant raised, disabled cantUndo, onClick $ command Undo ] [ text commonString.action.undo ]
    ]
    droppedOnClass snap = if snap.isDraggingOver then classes.beingDraggedOver else classes.notBeingDragged
    cantUndo = null undoList

    aclEntries = droppable {droppableId:"list", "type": "entry"} \p snap -> 
      div [P.ref p.innerRef, p.droppableProps, P.className $ droppedOnClass snap ] [
        list [disablePadding true] $ mapWithIndex entryRow acls,
        p.placeholder
      ]

    expressionContents (Tuple i {expr:exprType, priv}) =
      let exprEntries = case exprType of 
            Resolved expression -> [ 
              list [disablePadding true] $ 
                mapWithIndex (#) $ fromFoldable $ makeExpression 0 false expression Nil 
            ]
            _ -> []
          dropText = typography [ variant TS.caption, className $ joinWith " " [classes.flexCentered, classes.dropText] ] 
                        [ text aclString.dropplaceholder]
      in  [
        formControl [className classes.privSelect] [ 
          inputLabel [mkProp "htmlFor" "privSelect"] [text aclString.privilege],
          select [value priv, P.id "privSelect", onChangeStr $ EditEntry i <<< set _priv] $ 
            (\p -> menuItem [mkProp "value" p] [text p]) <$> allowedPrivs
        ],
        divider [className classes.divideEntry],
        div [ P.className classes.scrollable] $ (if length exprEntries == 0 then [dropText] else []) <> [
          droppable {droppableId:"currentEntry"} \p _ -> 
            div [P.ref p.innerRef, p.droppableProps, P.className classes.currentEntryDrop] exprEntries
        ]
      ]

    withPortal f p s = let child = f p s 
      in if s.isDragging then maybe child (flip renderToPortal child) dragPortal else child

    makeExpression indent multi expr l = case expr of       
        Term t n -> 
          let termActions i = div [P.className classes.hoverActions] $
            (guard  multi $> tooltip [title aclString.convertGroup] [ iconButton [ onClick $ command $ Expand i ] [ icon_ [ text "keyboard_arrow_right" ] ] ]) <> [
            formControlLabel [control $ checkbox [checked n, onChange $ command $ ToggleNot i ], label aclString.not ],
            iconButton [ onClick $ command $ DeleteExpr i ] [ icon_ [ text "delete" ] ] 
          ]
          in Cons (\i -> commonExpr "term" [P.style {paddingLeft: indentPixels}] [termActions i] n i (ResolvedTerm t)) l
        Op op exprs n -> (Cons $ opEntry op n) (foldr (makeExpression (indent + 1) (length exprs > 1)) l exprs)
      where 
      opEntry op n i = draggable {draggableId: "op" <> show i, index:i} $ \p s -> 
        div [P.ref p.innerRef, p.draggableProps] [
          div [ P.className classes.opDrop, P.style {marginLeft: indentPixels} ] [ 
              select [value $ opValue op n, onChangeStr $ ChangeOp i ] opItems,
              div [P.style {display:"none"}, p.dragHandleProps] []
          ]
        ]
      indentPixels = indent * 12
    
    commonExpr pfx props actions n i rt = draggable {draggableId: pfx <> show i, index:i} $ withPortal \p s -> 
      div [P.ref p.innerRef, p.draggableProps, p.dragHandleProps] $ [
        div ([P.className classes.termEntry] <> props) $ [ 
          listItemIcon_ [ icon_ [ text $ iconNameForTermType rt ] ], 
          listTextForTermType n rt ] <> actions
      ]

    entryRow i (ResolvedEntry {granted,override,priv,expr}) = draggable { "type": "entry", draggableId: "entry" <> show i, index:i} $ withPortal \p _ -> 
      div [P.ref p.innerRef, p.draggableProps, p.dragHandleProps] [
        div [P.className $ joinWith " " $ (guard (selectedIndex == Just i) $> classes.entrySelected) <> [classes.aclEntry]] [
          let p = guard (eq i <$> selectedIndex # fromMaybe false) $> className classes.selectedEntry
          in listItem (p <> [ P.button true, disableRipple true, onClick $ d \_ -> SelectEntry i]) [ 
            div [P.className classes.exprLine ] [
              listItemText [ className classes.entryText, disableTypography true, P.primary $ privText, P.secondary secondLine ],
              div [ P.className classes.hoverActions ] [ 
                check override _override aclString.override,
                check (not granted) _granted aclString.revoked,
                iconButton [ onClick $ command $ DeleteEntry i ] [ icon_ [ text "delete" ] ] 
              ]
            ]
          ]
        ]
      ]
      where privText = firstLine $ if granted then priv else aclString.revoke <> " - " <> priv
            secondLine = textForExprType expr
            check o l t = formControlLabel [control $ checkbox [checked o, toggler i l ], label t ]
            toggler i l = mkProp "onClick" $ toHandler $ \e -> do 
              stopPropagation (unsafeCoerce e)
              d (\_ -> EditEntry i (over l not)) unit

    firstLine t = typography [variant TS.subheading, className classes.ellipsed] [text t]
    stdExprLine t = typography [variant TS.body1, className classes.ellipsed, color textSecondary ] [ text t ] 

    textForExprType (Unresolved std) = stdExprLine $ textForExpression std 
    textForExprType (Resolved rexpr) = stdExprLine $ expressionText textForResolved rexpr
    textForExprType (InvalidExpr msg) = typography [component "span", color error] [ text msg ]

    ellipsed a b = listItemText $ [
      className classes.entryText, 
      disableTypography true,
      P.primary $ firstLine a
    ] <> catMaybes [ 
      (P.secondary <<< stdExprLine) <$> b
    ]

    listTextForResolved n = case _ of 
      Already std -> txt (textForTerm std) Nothing
      ResolvedUser (UserDetails {username, firstName, lastName}) -> txt username $ Just $ firstName <> " " <> lastName
      ResolvedGroup (GroupDetails {name}) -> txt name Nothing
      ResolvedRole (RoleDetails {name}) -> txt name Nothing
      where txt a = ellipsed (if n then aclString.notted <> a else a)

    listTextForTermType n = case _ of 
      ResolvedTerm t -> listTextForResolved n t
      UnresolvedTerm un -> listTextForResolved n (Already un)

  textForResolved :: ResolvedTerm -> String
  textForResolved = case _ of 
    Already std -> textForTerm std
    ResolvedUser (UserDetails {username}) ->  username
    ResolvedGroup (GroupDetails {name}) -> name
    ResolvedRole (RoleDetails {name}) -> name

  iconNameForTermType = case _ of 
    ResolvedTerm t -> iconNameForResolved t
    UnresolvedTerm un -> iconNameForResolved (Already un)
    
  iconNameForResolved = case _ of 
    Already std -> iconNameForTerm std
    ResolvedUser (UserDetails _) -> iconNameForTerm (User "")
    ResolvedGroup (GroupDetails _) -> iconNameForTerm (Group "")
    ResolvedRole (RoleDetails _) -> iconNameForTerm (Role "")

  iconNameForTerm = case _ of 
   Group _ -> groupIconName
   Role _ -> roleIconName
   User _ -> userIconName
   Everyone -> "public"
   LoggedInUsers -> "face"
   Ip _ -> "dns"
   Guests -> "person_outline"
   Owner -> "account_box"
   Referrer _ -> "http"
   SharedSecretToken _ -> "apps"

  defaultOp :: Array ResolvedExpression -> ResolvedExpression
  defaultOp e = Op OR e false
  
  reorder :: forall a. Int -> Int -> Array a -> Array a 
  reorder sourceIndex destIndex a = fromMaybe a do
     let newdest = if destIndex < 1 then 0 else destIndex
     o <- index a sourceIndex
     d <- deleteAt sourceIndex a
     insertAt newdest o d

  appendExpr :: ResolvedExpression -> ResolvedExpression -> ResolvedExpression
  appendExpr e = atEnd
    where 
    atEnd = case _ of 
      t1@(Term _ _) -> defaultOp [t1, e]
      (Op op exprs notted) | (Just lastop@(Op _ _ _)) <- last exprs -> 
          Op op (fromMaybe exprs $ updateAt (length exprs - 1) (atEnd lastop) exprs) notted
      (Op op exprs notted) -> Op op (snoc exprs e) notted
  
  addUndo :: (Array ResolvedEntry -> Array ResolvedEntry) -> State MyState Boolean
  addUndo f = do 
    oldEntries <- use _acls
    indx <- use _selectedIndex
    let newEntries = f oldEntries
    if newEntries == oldEntries then pure false else do 
      modifying _undoList $ Cons (Tuple indx oldEntries)
      assign _acls newEntries
      pure true

  modifyResolved :: (ResolvedExpression -> ResolvedExpression) -> State MyState Boolean
  modifyResolved f = modifyExpression (mapResolved (f >>> Resolved))

  emptyExpr :: ExprType
  emptyExpr = InvalidExpr aclString.required 

  modifyExpression :: (ExprType -> ExprType) -> State MyState Boolean
  modifyExpression f = fromMaybe false <$> runMaybeT do 
    selectedIndex <- MaybeT (gets _.selectedIndex)
    let curExpr :: Traversal' (Array ResolvedEntry) ExprType
        curExpr = ix selectedIndex <<< _ResolvedEntry <<< _expr
    oldEx <- MaybeT $ (preview (_acls <<< curExpr) <$> get)
    let newEx = mapResolved (collapseZero >>> maybe emptyExpr Resolved) $ f oldEx
    guard (oldEx /= newEx)
    lift $ addUndo $ set curExpr newEx

  setOrModifyExpr :: (ResolvedExpression -> ResolvedExpression -> ResolvedExpression) -> ResolvedTerm -> ExprType -> ExprType 
  setOrModifyExpr f t = case _ of 
    Resolved r -> Resolved $ f (Term t false) r 
    InvalidExpr _ -> Resolved (Term t false)
    o -> o 

  insertInto :: Int -> ResolvedExpression -> ResolvedExpression -> ResolvedExpression
  insertInto i e re = either (const re) singleExpr $ map (\f -> f [e]) $ findExprInsert i re 
    where 
    singleExpr [e] = e 
    singleExpr exprs = defaultOp exprs

  copyToCurrent :: Int -> Int -> State MyState Boolean
  copyToCurrent srcIx destIx = do 
    ce <- use _terms
    case ce !! srcIx of 
      Just (ResolvedTerm t) -> modifyExpression $ setOrModifyExpr (insertInto destIx) t
      _ -> pure false

  termQuery :: ExpressionTerm -> UserGroupRoles String String String
  termQuery = UserGroupRoles <<< case _ of 
    User uid -> {users:[uid],groups:[], roles:[]}
    Group gid -> {users:[], groups:[gid], roles:[]}
    Role rid -> {users:[], groups:[], roles:[rid]}
    _ -> {users:[],groups:[],roles:[]}

  toQuery :: Expression -> UserGroupRoles String String String
  toQuery = traverseExpr (\t _ -> termQuery t) \{exprs} _ -> fold exprs

  runChange f = do 
    (Tuple c {acls}) <- runState f <$> getState
    modifyState $ execState f
    if c then do 
      {onChange:oc} <- getProps
      this <- ask 
      liftEff $ runIOFn1 oc {canSave: not $ any isInvalid acls, getAcls : liftEff $ flip runReaderT this do 
            {acls} <- getState
            pure $ (traverse backToAccessEntry acls) # maybe ([]) \entries -> do 
              entryToTargetList <$> entries
        }
      else pure unit

  eval = case _ of 
    Init -> do 
      r <- liftAff $ AJ.get (baseUrl <> "api/acl/recent")
      let parseRecent (Right (StdTerm t)) = Just $ UnresolvedTerm t
          parseRecent _ = Nothing
      let ok = decodeJson r.response <#> mapMaybe (parseRecent <<< parseTerm)
      either (const $ pure unit) (\t -> modifyState \s -> s {terms = s.terms <> t}) ok
      eval Resolve
    Updated oldAcls -> do
      {acls} <- getProps
      if (acls /= oldAcls) 
        then let {acls:nacls,terms} = convertToInternal acls
              in modifyState _{acls = nacls, terms = terms} *> eval Resolve
        else pure unit
    AddFromDialog dt -> do 
      _ <- case dt of 
            [k] -> void $ liftAff $ forkAff $ 
                AJ.post_' (baseUrl <> "api/acl/recent/add?target=" <> 
                  (encodeURIComponent $ termToWho $ resolvedToTerm $ k) ) (Nothing :: Maybe Unit)
            _ -> pure unit
      runChange $ do 
        modifying _terms $ flip append (ResolvedTerm <$> dt)
        modify _{showDialog=false,dialHidden=false}
        traverse_ (\t -> modifyExpression $ setOrModifyExpr appendExpr t) dt
        pure true

    DialState open -> modifyState \s -> s {dialOpen = open s.dialOpen}

    NewPriv -> modifyState _ {newPrivDialog = true}
    FinishNewPriv (Just priv) -> runChange $ do 
        modify _{newPrivDialog=false}
        oldEntries <- use _acls
        changed <- addUndo (flip snoc $ ResolvedEntry {priv, granted:true, override:false, expr: emptyExpr})
        assign _selectedIndex $ Just $ length oldEntries
        pure changed

    FinishNewPriv _ -> modifyState _ {newPrivDialog = false}
    
    CloseDialog -> modifyState _{showDialog=false,dialHidden=false}
    OpenDialog dt -> modifyState _{openDialog=Just dt,showDialog=true, dialOpen=false,dialHidden=true}
    
    DeleteEntry ind ->  
      runChange $ addUndo \l -> fromMaybe l $ deleteAt ind l  
    EditEntry ind f -> 
      runChange $ addUndo $ over (ix ind <<< _Newtype) f

    ToggleNot ind -> do 
      let toggleNot e | Right {get:(Term t n),modify} <- findExprModify ind e = 
              fromMaybe e $ modify (Just $ Term t (not n))
          toggleNot e = e
      runChange $ modifyResolved toggleNot
      
    Expand ind -> do 
      let expandIt e | Right {get, modify} <- findExprModify ind e = fromMaybe e $ modify $ Just $ defaultOp [get]
          expandIt e = e
      runChange $ modifyResolved expandIt 
    ChangeOp ind op -> do 
      let changeOp e | Right {get:(Op _ exprs _),modify} <- findExprModify ind e = 
              fromMaybe e $ modify $ (\(Tuple o n) -> Op o exprs n) <$> valToOpType op
          changeOp e = e
      runChange $ modifyResolved changeOp 

    DeleteExpr i -> do 
      let delExpr e@(Resolved r) = either (const e) (\{modify} -> maybe emptyExpr Resolved $ modify Nothing) $ findExprModify i r
          delExpr o = o
      modifyState $ execState $ modifyExpression delExpr
    
    Undo -> do 
      runChange $ do 
        undos <- use _undoList
        uncons undos # maybe (pure false) \{head:Tuple ix list, tail} -> do 
          assign _undoList tail
          assign _acls list
          assign _selectedIndex ix
          pure true

    SelectEntry entry -> do 
      modifyState $ over _selectedIndex case _ of 
        Just e | e == entry -> Nothing
        _ -> Just entry

    Resolve -> do
      {terms,acls} <- getState
      e <- liftEff $ do 
        w <- window
        d <- htmlDocumentToDocument <$> document w
        e <- elementToNode <$> (D.createElement "div" d)
        mb <- querySelector (QuerySelector "body") (documentToParentNode d)
        maybe (pure e) (appendChild e <<< elementToNode) mb
      modifyState _ {dragPortal=Just e}
      let ugr = foldMapOf (traversed <<< _ResolvedEntry <<< _expr <<< _unresolvedExpr) toQuery acls <> 
                foldMapOf (traversed <<< _unresolvedTerm) termQuery terms
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
          resolveAcls = over (_acls <<< traversed <<< _ResolvedEntry <<< _expr) case _ of 
            Unresolved u -> Resolved (resolve u)
            o -> o
          resolveTerms = over (_terms <<< traversed) case _ of 
            UnresolvedTerm u -> ResolvedTerm (resolveTerm u)
            o -> o
      modifyState $ resolveAcls <<< resolveTerms
      

    HandleDrop dr@{source:{index:sourceIndex, droppableId:sourceId}} | Just {index:destIndex,droppableId:destId} <- toMaybe dr.destination -> 
      let handleDrop sId dId | sId == dId && sourceIndex /= destIndex = 
            let reorderCurrent = modifyResolved $ \e ->                 
                case findExprModify sourceIndex e of 
                    Right {get,modify} -> fromMaybe e $ insertInto destIndex get <$> modify Nothing
                    _ -> e
                l = case sId of 
                      "list" -> addUndo (reorder sourceIndex destIndex)
                      "currentEntry" -> reorderCurrent
                      _       -> modifying _terms (reorder sourceIndex destIndex) *> pure false
            in runChange l
          handleDrop "common" "currentEntry" = do 
            runChange $ copyToCurrent sourceIndex destIndex 
          handleDrop _ _ = pure unit
          
      in handleDrop sourceId destId
    HandleDrop _ -> pure unit

  matchStr = aclString.match

  opName :: OpType -> Boolean -> String
  opName AND false = matchStr.and 
  opName OR false = matchStr.or
  opName AND true = matchStr.notand
  opName OR true = matchStr.notor

  opItems :: Array ReactElement
  opItems = mkOp <$> [ Tuple OR false, Tuple AND false, Tuple OR true, Tuple AND true ]
    where 
    mkOp (Tuple o n) = menuItem [mkProp "value" $ opValue o n] [ text $ opName o n]

commonTerms :: Array ExpressionTerm
commonTerms = [
  Everyone,
  LoggedInUsers, 
  Guests,
  Owner
]

opValue :: OpType -> Boolean -> String 
opValue AND false = "and"
opValue OR false = "or"
opValue AND true = "notand"
opValue OR true = "notor"

valToOpType :: String -> Maybe (Tuple OpType Boolean)
valToOpType = case _ of 
  "and" -> Just $ Tuple AND false
  "or" -> Just $ Tuple OR false
  "notand" -> Just $ Tuple AND true
  "notor" -> Just $ Tuple OR true
  _ -> Nothing 

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

isInvalid :: ResolvedEntry -> Boolean 
isInvalid (ResolvedEntry {priv,granted,override,expr:(InvalidExpr _)}) = true 
isInvalid _ = false

aclRawStrings = {prefix:"acleditor",
  strings: {
    privilege: "Privilege",
    privileges: "Privileges", 
    selectpriv: "Select privilege",
    expression: "Expression", 
    privplaceholder: "Please select or add a privilege", 
    dropplaceholder: "Drop targets here",
    addpriv: "Add Privilege",
    addexpression: "Add expression", 
    targets: "Targets",
    new: {
      ugr: "User, Group or Role", 
      ip: "IP Range",
      referrer: "HTTP Referrer",
      token: "Shared secret"
    }, 
    notted: "NOT - ",
    not: "Not", 
    override: "Override", 
    revoked: "Revoked",
    revoke: "Revoke", 
    required: "* Required",
    match: {
      and: "All match",
      or: "At least one matches",
      notand: "Not all match",
      notor: "None match"
    },
    convertGroup: "Convert to group"
  }
}