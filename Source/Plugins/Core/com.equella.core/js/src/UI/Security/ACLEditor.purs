module OEQ.UI.Security.ACLEditor where 


import Prelude hiding (div)

import Common.CommonStrings (commonAction, commonString)
import Control.Monad.Maybe.Trans (MaybeT(..), runMaybeT)
import Control.Monad.Reader (runReaderT)
import Control.Monad.State (State, execState, get, gets, modify, runState)
import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import Data.Argonaut (class EncodeJson, decodeJson, encodeJson, jsonEmptyObject, (:=), (~>))
import Data.Array (any, catMaybes, deleteAt, fold, foldr, fromFoldable, index, insertAt, last, length, mapMaybe, mapWithIndex, nubByEq, snoc, unionBy, updateAt, (!!))
import Data.Either (Either(..), either)
import Data.Lens (Lens', Prism', Traversal', _2, assign, filtered, foldMapOf, modifying, over, preview, previewOn, prism', set, traversed, use, view, (^?))
import Data.Lens.Index (ix)
import Data.Lens.Iso.Newtype (_Newtype)
import Data.Lens.Record (prop)
import Data.List (List(Cons, Nil), null, uncons)
import Data.Maybe (Maybe(..), fromMaybe, maybe)
import Data.Newtype (class Newtype)
import Data.Nullable (toMaybe, toNullable)
import Data.String (joinWith)
import Data.Symbol (SProxy(..))
import Data.Traversable (traverse, traverse_)
import Data.Tuple (Tuple(Tuple), fst, snd)
import Dispatcher (affAction)
import Dispatcher.React (getProps, getState, modifyState, renderer)
import Effect (Effect)
import Effect.Aff (forkAff)
import Effect.Aff.Class (liftAff)
import Effect.Class (liftEffect)
import Effect.Uncurried (EffectFn1, mkEffectFn1, runEffectFn1)
import ExtUI.DragNDrop.Beautiful (DropResult, dragDropContext, draggable, droppable)
import ExtUI.SpeedDial (speedDialActionU, speedDialIconU, speedDialU)
import MaterialUI.Button (button)
import MaterialUI.Checkbox (checkbox')
import MaterialUI.Dialog (dialog)
import MaterialUI.DialogActions (dialogActions_)
import MaterialUI.DialogContent (dialogContent)
import MaterialUI.Divider (divider')
import MaterialUI.Enums (body1, caption, headline, raised, secondary, subheading, textSecondary, title)
import MaterialUI.Enums as SEnum
import MaterialUI.FormControl (formControl)
import MaterialUI.FormControlLabel (formControlLabel')
import MaterialUI.Icon (icon_)
import MaterialUI.IconButton (iconButton)
import MaterialUI.InputLabel (inputLabel)
import MaterialUI.List (list, list_)
import MaterialUI.ListItem (listItem)
import MaterialUI.ListItemIcon (listItemIcon_)
import MaterialUI.ListItemText (listItemText')
import MaterialUI.Menu (menu)
import MaterialUI.MenuItem (menuItem, menuItem_)
import MaterialUI.Paper (paper)
import MaterialUI.Select (select)
import MaterialUI.Styles (withStyles)
import MaterialUI.Tooltip (tooltip)
import MaterialUI.Typography (typography)
import Network.HTTP.Affjax (get, post_) as AJ
import Network.HTTP.Affjax.Request (json)
import Network.HTTP.Affjax.Response (json) as Resp
import OEQ.API.User (lookupUsers)
import OEQ.Data.Security (AccessEntry(..), Expression(..), ExpressionTerm(..), IpRange(..), OpType(..), ParsedTerm(..), ResolvedExpression(..), ResolvedTerm(..), TargetListEntry(..), collapseZero, entryToTargetList, expressionText, findExprInsert, findExprModify, parseTerm, parseWho, resolvedToTerm, termToWho, textForExpression, textForTerm, traverseExpr)
import OEQ.Data.User (GroupDetails(GroupDetails), RoleDetails(RoleDetails), UserDetails(UserDetails), UserGroupRoles(UserGroupRoles))
import OEQ.Environment (baseUrl, prepLangStrings)
import OEQ.UI.Common (textChange)
import OEQ.UI.Icons (groupIconName, roleIconName, userIconName)
import OEQ.UI.SearchUser (UGREnabled(..))
import OEQ.UI.Security.TermSelection (DialogType(..), termDialog)
import React (ReactClass, ReactElement)
import React as R
import React.DOM (div, div', text)
import React.DOM.Props (className, style) as P
import React.DOM.Props (unsafeMkProps)
import React.SyntheticEvent (currentTarget, stopPropagation)
import Unsafe.Coerce (unsafeCoerce)
import Unsafe.Reference (unsafeRefEq)
import Web.DOM (Node)
import Web.DOM.Document (createElement, toParentNode)
import Web.DOM.Element (toNode)
import Web.DOM.Node (appendChild)
import Web.DOM.ParentNode (QuerySelector(..), querySelector)
import Web.HTML (HTMLElement, window)
import Web.HTML.HTMLDocument (toDocument)
import Web.HTML.Window (document)

foreign import renderToPortal :: Node -> ReactElement -> ReactElement

data ExprType = Unresolved Expression | Resolved ResolvedExpression | InvalidExpr String

data TermType = UnresolvedTerm ExpressionTerm | ResolvedTerm ResolvedTerm

exprTermOnly :: TermType -> ExpressionTerm
exprTermOnly (UnresolvedTerm t) = t
exprTermOnly (ResolvedTerm rt) = resolvedToTerm rt

newtype AddRemoveRecent = AddRemove {add :: Array String, remove :: Array String }

type TermListType = Tuple Boolean TermType

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
  | NewPriv | FinishNewPriv (Maybe String) | DialState (Boolean -> Boolean) | ToggleNot Int | Expand Int 
  | Updated (Array TargetListEntry) | EntryMenuAnchor (Maybe (Tuple Int HTMLElement))
  | ClearTarget Int

type MyState = {
  acls :: Array ResolvedEntry,
  selectedIndex :: Maybe Int,
  terms :: Array TermListType,
  undoList :: List (Tuple (Maybe Int) (Array ResolvedEntry)), 
  newPrivDialog :: Boolean,
  openDialog :: Maybe DialogType, 
  showDialog :: Boolean,
  dialOpen :: Boolean, 
  dialHidden :: Boolean, 
  dragPortal :: Maybe Node, 
  entryMenu :: Maybe (Tuple Int HTMLElement)
}

newtype EqMyState = EqMyState MyState

aclEditorClass :: ReactClass {
  acls :: Array TargetListEntry, 
  onChange :: EffectFn1 {canSave :: Boolean, getAcls :: Effect (Array TargetListEntry) } Unit,
  allowedPrivs :: Array String
}
aclEditorClass = withStyles styles $ R.component "AclEditor" $ \this -> do
  let
    d = eval >>> affAction this
    componentDidUpdate {acls:oldAcls} _ _ = d $ Updated oldAcls
    shouldComponentUpdate {acls} nextState = do 
      {acls:oldAcls} <- R.getProps this 
      oldState <- R.getState this 
      pure $ acls /= oldAcls || (not $ unsafeRefEq nextState oldState)
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


    convertToInternal a = 
      let acls = markForResolve <$> a
          sameTerm (Tuple _ t) (Tuple _ t2) = t == t2
          collectTerm (ResolvedEntry {expr:Unresolved e}) = 
                traverseExpr (\rt _ -> [deletable $ UnresolvedTerm rt]) (\{exprs} _ -> join exprs) e 
          collectTerm _ = []
      in {terms: unionBy sameTerm (notdeletable <<< UnresolvedTerm <$> commonTerms) $ nubByEq sameTerm (acls >>= collectTerm), acls}
      where 
      markForResolve (TargetListEntry {privilege:priv,granted,override,who}) = 
            ResolvedEntry {priv, granted, override, expr: either (InvalidExpr <<< show) Unresolved $ parseWho who}

    initialState {acls:a} = 
      let {acls,terms} = convertToInternal a
      in {
        acls, 
        terms,
        selectedIndex: Nothing, 
        undoList: Nil, 
        openDialog: Nothing, 
        showDialog: false,
        dialOpen: false, 
        dialHidden: false,
        dragPortal: Nothing, 
        newPrivDialog: false, 
        entryMenu: Nothing
      }
      

    render {state: s@{acls,terms,selectedIndex,openDialog,showDialog,undoList,dialOpen, dialHidden,dragPortal},
        props: {classes,allowedPrivs}} = 
      let expressionM = selectedIndex >>= \i -> Tuple i <$> previewOn acls (currentEntry i)
      in dragDropContext { onDragEnd: mkEffectFn1 (d <<< HandleDrop) } $ [ 
        div [P.className classes.overallPanel] [
          div [P.className classes.editorPanels] [
            paper {className: classes.entryList} [
              typography {variant: title, className: classes.flexCentered} [ text aclString.privileges],
              createNewPriv,
              div [ P.className classes.scrollable ] [aclEntries]
            ],
            paper {className: classes.currentEntryPanel } $ [ 
              typography {variant: title, className: classes.flexCentered } [ text aclString.expression]
            ] <> maybe placeholderExpr expressionContents expressionM,
            paper {className: classes.commonPanel} commonPanel 
          ]
        ]
      ] <> (catMaybes [ 
        map renderDialog openDialog, 
        Just renderNewPriv
      ])
      where 
      renderDialog dt = termDialog {open:showDialog, onAdd: mkEffectFn1 $ d <<< AddFromDialog, cancel: d CloseDialog, dt}
      renderNewPriv = dialog {open: s.newPrivDialog, onClose: close} [
          dialogContent {className: classes.privDialog} [
            typography {variant: headline} [ text aclString.selectpriv],
            list_ $ privItem <$> allowedPrivs
          ], 
          dialogActions_ [
            button {color: secondary, onClick: close} [ text commonAction.cancel ]
          ]
        ]
        where 
        close = d $ FinishNewPriv Nothing
        privItem i = listItem {button: true, onClick: d $ FinishNewPriv (Just i)} [
          listItemText' {primary: i}
        ]

      commonPanel = let 
        dialChange = DialState
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
          typography {variant: title, className: classes.flexCentered} [ text aclString.targets],
          div [P.className classes.scrollable ] [
            droppable {droppableId:"common"} \p _ -> 
              div [unsafeMkProps "ref" p.innerRef, p.droppableProps, P.style {width: "100%"}] $
                  mapWithIndex (\i (Tuple removable t) -> commonExpr "common" [] (guard removable $> targetActions i) false i t) 
                    terms
              
          ]
        ]
        where 
        action i title dt = speedDialActionU {icon: icon_ [text i], title, onClick: d $ OpenDialog dt }
        targetActions i = div [P.className classes.termActions] $ [
              tooltip { title: commonAction.clear } $ iconButton { onClick: d $ ClearTarget i } [ icon_ [ text "clear" ] ]
            ]


      placeholderExpr = [ typography {variant: caption, className: classes.flexCentered } [ text aclString.privplaceholder] ]
      createNewPriv = div' [ 
        button {variant: raised, className: classes.privSelect, onClick: d NewPriv } [text aclString.addpriv], 
        button {variant: raised, disabled: cantUndo, onClick: d Undo } [ text commonString.action.undo ]
      ]
      droppedOnClass snap = if snap.isDraggingOver then classes.beingDraggedOver else classes.notBeingDragged
      cantUndo = null undoList
 
      aclEntries = droppable {droppableId:"list", "type": "entry"} \p snap -> 
        div [unsafeMkProps "ref" p.innerRef, p.droppableProps, P.className $ droppedOnClass snap ] [
          list {disablePadding: true} $ mapWithIndex entryRow acls,
          p.placeholder
        ]

      expressionContents (Tuple i {expr:exprType, priv}) =
        let exprEntries = case exprType of 
              Resolved expression -> [ 
                list {disablePadding: true} $ 
                  mapWithIndex (#) $ fromFoldable $ makeExpression 0 false expression Nil 
              ]
              _ -> []
            dropText = typography {variant: caption, className: joinWith " " [classes.flexCentered, classes.dropText] } 
                          [ text aclString.dropplaceholder]
        in  [
          formControl {className: classes.privSelect} [ 
            inputLabel {"htmlFor": "privSelect"} [text aclString.privilege],
            select {value: priv, id: "privSelect", onChange: textChange d $ EditEntry i <<< set _priv } $ 
              (\p -> menuItem {value:p} [text p]) <$> allowedPrivs
          ],
          divider' {className: classes.divideEntry},
          div [ P.className classes.scrollable] $ (if length exprEntries == 0 then [dropText] else []) <> [
            droppable {droppableId:"currentEntry"} \p _ -> 
              div [unsafeMkProps "ref" p.innerRef, p.droppableProps, P.className classes.currentEntryDrop] exprEntries
          ]
        ]

      withPortal f p ds = let child = f p ds 
        in if ds.isDragging then maybe child (flip renderToPortal child) dragPortal else child

      makeExpression indent multi expr l = case expr of       
          RTerm t n -> let 
            termActions i = let 
              expander = tooltip {title: aclString.convertGroup} $
                iconButton { onClick: d $ Expand i } [ 
                  icon_ [ text "keyboard_arrow_right" ] 
                ]
              in div [P.className classes.termActions] $
              (guard multi $> expander) <> [
                formControlLabel' { control: checkbox' {checked: n, onChange: d $ ToggleNot i }, label: aclString.not },
                iconButton { onClick: d $ DeleteExpr i } [ icon_ [ text "delete" ] ] 
              ]
            in Cons (\i -> commonExpr "term" [P.style {paddingLeft: indentPixels}] [termActions i] n i (ResolvedTerm t)) l
          ROp op exprs n -> (Cons $ opEntry op n) (foldr (makeExpression (indent + 1) (length exprs > 1)) l exprs)
        where 
        opEntry op n i = draggable {draggableId: "op" <> show i, index:i} $ \p _ -> 
          div [unsafeMkProps "ref" p.innerRef, p.draggableProps] [
            div [ P.className classes.opDrop, P.style {marginLeft: indentPixels} ] [ 
                select {value: opValue op n, onChange: textChange d $ ChangeOp i } opItems,
                div [P.style {display:"none"}, p.dragHandleProps] []
            ]
          ]
        indentPixels = indent * 12
      
      commonExpr pfx props actions n i rt = draggable {draggableId: pfx <> show i, index:i} $ withPortal \p _ -> 
        div [unsafeMkProps "ref"  p.innerRef, p.draggableProps, p.dragHandleProps] $ [
          div ([P.className classes.termEntry] <> props) $ [ 
            listItemIcon_ $ icon_ [ text $ iconNameForTermType rt ], 
            listTextForTermType n rt ] <> actions
        ]

      entryRow i (ResolvedEntry {granted,override,priv,expr}) = 
        draggable { "type": "entry", draggableId: "entry" <> show i, index:i} $ withPortal \p _ -> 
        div [unsafeMkProps "ref" p.innerRef, p.draggableProps, p.dragHandleProps] [
          div' [
            let selected = eq i <$> selectedIndex # fromMaybe false
                menuOpen = (fst >>> eq i <$> s.entryMenu) # fromMaybe false
            in listItem {
                className: toNullable $ guard selected $> classes.selectedEntry, 
                button: true, 
                disableRipple: true, 
                onClick: d $ SelectEntry i} [
              div [P.className classes.exprLine ] [
                listItemText' {className: classes.entryText, disableTypography: true, 
                  primary: privText, secondary: secondLine },
                div [ P.className classes.termActions ] [ 
                  iconButton {onClick: d $ DeleteEntry i } [ icon_ [ text "delete" ] ], 
                  iconButton {onClick: mkEffectFn1 \e -> currentTarget e >>= \t -> d $ EntryMenuAnchor $ Just $ Tuple i (unsafeCoerce t) } [ icon_ [text "more_vert"]],
                  menu {open: menuOpen, anchorEl: toNullable $ snd <$> s.entryMenu, onClose: d $ EntryMenuAnchor Nothing} [
                    menuItem_ [ check (not granted) _granted aclString.revoked ],
                    menuItem_ [ check override _override aclString.override ]
                  ]
                ]
              ]
            ]
          ]
        ]
        where privText = firstLine $ case catMaybes [
                      (guard $ not granted) $> aclString.revoke, 
                      guard override $> aclString.override
                    ] of 
                    [] -> priv 
                    o -> joinWith ", " o <> " - " <> priv
              secondLine = textForExprType expr
              check o l t = formControlLabel' {control: checkbox' {checked: o, onClick: toggler l}, label: t}
              toggler l = mkEffectFn1 \e -> do 
                stopPropagation (unsafeCoerce e)
                d $ EditEntry i (over l not)

      firstLine t = typography {variant: subheading, className: classes.ellipsed} [text t]
      stdExprLine t = typography {variant: body1, className: classes.ellipsed, color: textSecondary } [ text t ] 

      textForExprType (Unresolved std) = stdExprLine $ textForExpression std 
      textForExprType (Resolved rexpr) = stdExprLine $ expressionText textForResolved rexpr
      textForExprType (InvalidExpr msg) = typography {component: "span", color: SEnum.error} [ text msg ]

      ellipsed a b = listItemText' {
        className: classes.entryText, 
        disableTypography: true,
        primary: firstLine a,
        secondary: toNullable $ stdExprLine <$> b
      }

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
    defaultOp e = ROp OR e false
    
    reorder :: forall a. Int -> Int -> Array a -> Array a 
    reorder sourceIndex destIndex a = fromMaybe a do
      let newdest = if destIndex < 1 then 0 else destIndex
      src <- index a sourceIndex
      dest <- deleteAt sourceIndex a
      insertAt newdest src dest

    appendExpr :: ResolvedExpression -> ResolvedExpression -> ResolvedExpression
    appendExpr e = atEnd
      where 
      atEnd = case _ of 
        t1@(RTerm _ _) -> defaultOp [t1, e]
        (ROp op exprs notted) | (Just lastop@(ROp _ _ _)) <- last exprs -> 
            ROp op (fromMaybe exprs $ updateAt (length exprs - 1) (atEnd lastop) exprs) notted
        (ROp op exprs notted) -> ROp op (snoc exprs e) notted
    
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
      Resolved r -> Resolved $ f (RTerm t false) r 
      InvalidExpr _ -> Resolved (RTerm t false)
      o -> o 

    insertInto :: Int -> ResolvedExpression -> ResolvedExpression -> ResolvedExpression
    insertInto i e re = either (const re) singleExpr $ map (\f -> f [e]) $ findExprInsert i re 
      where 
      singleExpr [se] = se 
      singleExpr exprs = defaultOp exprs

    copyToCurrent :: Int -> Int -> State MyState Boolean
    copyToCurrent srcIx destIx = do 
      ce <- use _terms
      case ce !! srcIx of 
        Just (Tuple _ (ResolvedTerm t)) -> modifyExpression $ setOrModifyExpr (insertInto destIx) t
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
      (Tuple changed {acls}) <- runState f <$> getState
      modifyState $ execState f
      if changed then do 
        {onChange:oc} <- getProps
        liftEffect $ runEffectFn1 oc {canSave: not $ any isInvalid acls, getAcls : liftEffect $ flip runReaderT this do 
              s <- getState
              pure $ (traverse backToAccessEntry s.acls) # maybe ([]) \entries -> do 
                entryToTargetList <$> entries
          }
        else pure unit

    deletable = Tuple true
    notdeletable = Tuple false 

    addRemoveRecent {add,remove} =  
      if length add + length remove > 0 
      then void $ forkAff $ AJ.post_ (baseUrl <> "api/acl/recent") $ json $ encodeJson (AddRemove {add,remove})
      else pure unit

    eval = case _ of 
      Init -> do 
        r <- liftAff $ AJ.get Resp.json (baseUrl <> "api/acl/recent")
        let parseRecent (Right (StdTerm t)) = Just $ deletable $ UnresolvedTerm t
            parseRecent _ = Nothing
        decodeJson r.response <#> mapMaybe (parseRecent <<< parseTerm) # 
          either (const $ pure unit) (\t -> modifyState \s -> s {terms = s.terms <> t})
        eval Resolve
      EntryMenuAnchor el -> 
        modifyState _{entryMenu = el}
      Updated oldAcls -> do
        {acls} <- getProps
        if (acls /= oldAcls) 
          then let {acls:nacls,terms} = convertToInternal acls
                in modifyState _{acls = nacls, terms = terms} *> eval Resolve
          else pure unit
      AddFromDialog dt -> do 
        liftAff $ addRemoveRecent {add: resolvedToTerm >>> termToWho <$> dt,remove:[]}
        runChange $ do 
          modifying _terms $ flip append (deletable <<< ResolvedTerm <$> dt)
          _ <- modify _ {showDialog=false,dialHidden=false}
          traverse_ (\t -> modifyExpression $ setOrModifyExpr appendExpr t) dt
          pure true

      DialState open -> modifyState \s -> s {dialOpen = open s.dialOpen}

      NewPriv -> modifyState _ {newPrivDialog = true}
      FinishNewPriv (Just priv) -> runChange $ do 
          _ <- modify _{newPrivDialog=false}
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
        let toggleNot e | Right {get:(RTerm t n),modify} <- findExprModify ind e = 
                fromMaybe e $ modify (Just $ RTerm t (not n))
            toggleNot e = e
        runChange $ modifyResolved toggleNot
        
      Expand ind -> do 
        let expandIt e | Right {get, modify} <- findExprModify ind e = fromMaybe e $ modify $ Just $ defaultOp [get]
            expandIt e = e
        runChange $ modifyResolved expandIt 
      ChangeOp ind op -> do 
        let changeOp e | Right {get:(ROp _ exprs _),modify} <- findExprModify ind e = 
                fromMaybe e $ modify $ (\(Tuple o n) -> ROp o exprs n) <$> valToOpType op
            changeOp e = e
        runChange $ modifyResolved changeOp 

      ClearTarget i -> do
        {terms} <- getState
        liftAff $ addRemoveRecent {add:[],remove: fromFoldable $ snd >>> exprTermOnly >>> termToWho <$> terms !! i }
        modifyState $ over _terms (\t -> fromMaybe t $ deleteAt i t)
      DeleteExpr i -> do 
        let delExpr e@(Resolved r) = either (const e) (\{modify} -> maybe emptyExpr Resolved $ modify Nothing) $ findExprModify i r
            delExpr o = o
        runChange $ modifyExpression delExpr
      
      Undo -> do 
        runChange $ do 
          undos <- use _undoList
          uncons undos # maybe (pure false) \{head:Tuple ix list, tail} -> do 
            assign _undoList tail
            assign _acls list
            assign _selectedIndex ix
            pure true

      SelectEntry entry -> do 
        modifyState $ set _selectedIndex $ Just entry

      Resolve -> do
        {terms,acls} <- getState
        e <- liftEffect $ do 
          w <- window
          doc <- toDocument <$> document w
          e <- toNode <$> (createElement "div" doc)
          mb <- querySelector (QuerySelector "body") (toParentNode doc)
          maybe (pure e) (appendChild e <<< toNode) mb
        modifyState _ {dragPortal=Just e}
        let ugr = foldMapOf (traversed <<< _ResolvedEntry <<< _expr <<< _unresolvedExpr) toQuery acls <> 
                  foldMapOf (traversed <<< _2 <<< _unresolvedTerm) termQuery terms
        (UserGroupRoles {users,groups,roles}) <- lift $ lookupUsers ugr
        let filterById :: forall a r. Newtype a {id::String|r} => Array a -> String -> Maybe a
            filterById arr uid = arr ^? (traversed <<< (filtered $ view ((_Newtype :: Lens' a {id::String|r}) <<< _id) >>> eq uid))
            resolveTerm = case _ of 
              (User uid) | Just ud <- filterById users uid -> ResolvedUser ud
              (Group gid) | Just gd <- filterById groups gid -> ResolvedGroup gd
              (Role rid) | Just rd <- filterById roles rid -> ResolvedRole rd
              (User uid) -> ResolvedUser (UserDetails {id:uid, username: "Unknown user with id " <> uid, firstName:"", lastName:"", email:Nothing})
              other -> Already other
            resolve = traverseExpr (\t n -> RTerm (resolveTerm t) n) \{op,exprs} n -> ROp op exprs n
            resolveAcls = over (_acls <<< traversed <<< _ResolvedEntry <<< _expr) case _ of 
              Unresolved u -> Resolved (resolve u)
              o -> o
            resolveTerms = over (_terms <<< traversed <<< _2) case _ of 
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
      mkOp (Tuple o n) = menuItem {value: opValue o n} [ text $ opName o n]
  initialProps <- R.getProps this
  pure {
    state:initialState initialProps, 
    render: renderer render this, 
    shouldComponentUpdate, 
    componentDidUpdate, 
    componentDidMount: d Init
  }
  where 
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
      gridTemplateRows: "100%", 
      "-ms-grid-columns": "37% 33% 30%",
      "-ms-grid-rows": "80vh"
    },
    entryList: {
      position: "relative",
      gridColumnStart: 1,
      "-ms-grid-column": 1,
      display: "flex", 
      flexDirection: "column"
    },
    currentEntryPanel: {
      position: "relative",
      display: "flex",
      flexDirection: "column",
      "-ms-grid-column": 2,
      gridColumnStart: 2, 
      marginLeft: theme.spacing.unit
    },
    commonPanel: {
      position: "relative",
      display: "flex",
      flexDirection: "column",
      "-ms-grid-column": 3,
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
    termActions : {
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
      paddingLeft: theme.spacing.unit * 2
    }, 
    privDialog: {
      width: "30em",
      minHeight: "20em"
    }
  }

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
  convertRE (RTerm rt b) = 
    let seterm = case rt of 
          Already t -> t 
          ResolvedUser (UserDetails {id}) -> User id  
          ResolvedGroup (GroupDetails {id}) -> Group id  
          ResolvedRole (RoleDetails {id}) -> Role id
    in Term seterm b
  convertRE (ROp o exprs n) = Op o (convertRE <$> exprs) n

isInvalid :: ResolvedEntry -> Boolean 
isInvalid (ResolvedEntry {priv,granted,override,expr:(InvalidExpr _)}) = true 
isInvalid _ = false

aclRawStrings :: { prefix :: String
, strings :: { privilege :: String
             , privileges :: String
             , selectpriv :: String
             , expression :: String
             , privplaceholder :: String
             , dropplaceholder :: String
             , addpriv :: String
             , addexpression :: String
             , targets :: String
             , new :: { ugr :: String
                      , ip :: String
                      , referrer :: String
                      , token :: String
                      }
             , notted :: String
             , not :: String
             , override :: String
             , revoked :: String
             , revoke :: String
             , required :: String
             , match :: { and :: String
                        , or :: String
                        , notand :: String
                        , notor :: String
                        }
             , convertGroup :: String
             }
}
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
      or: "At least one match",
      notand: "Not all match",
      notor: "None match"
    },
    convertGroup: "Convert to group"
  }
}

instance encAddRemove :: EncodeJson AddRemoveRecent where 
  encodeJson (AddRemove {add,remove}) = "add" := add ~> 
    "remove" := remove ~> jsonEmptyObject